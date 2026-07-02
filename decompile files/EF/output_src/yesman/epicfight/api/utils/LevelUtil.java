package yesman.epicfight.api.utils;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPFracture;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.level.block.FractureBlock;
import yesman.epicfight.world.level.block.FractureBlockState;

public class LevelUtil {
   private static final Vec3 IMPACT_DIRECTION = new Vec3(0.0, -1.0, 0.0);
   private static final LevelUtil INSTANCE = EpicFightSharedConstants.isPhysicalClient() ? new LevelUtil.ClientLevelUtil() : new LevelUtil();

   public static int calculateLivingEntityFallDamage(LivingEntity livingEntity, float distance, float modifier) {
      if (livingEntity.m_6095_().m_204039_(EntityTypeTags.f_273841_)) {
         return 0;
      }

      MobEffectInstance mobeffectinstance = livingEntity.m_21124_(MobEffects.f_19603_);
      float f = mobeffectinstance == null ? 0.0F : mobeffectinstance.m_19564_() + 1;
      return Mth.m_14167_((distance - 3.0F - f) * modifier);
   }

   public static void spreadShockwave(Level level, Vec3 center, Vec3 direction, double length, int edgeX, int edgeZ, List<Entity> entityBeingHit) {
      Vec3 edgeOfShockwave = center.m_82549_(direction.m_82541_().m_82490_((float)length));
      int xFrom = (int)Math.min(Math.floor(center.f_82479_), edgeX);
      int xTo = (int)Math.max(Math.floor(center.f_82479_), edgeX);
      int zFrom = (int)Math.min(Math.floor(center.f_82481_), edgeZ);
      int zTo = (int)Math.max(Math.floor(center.f_82481_), edgeZ);
      List<Vec2i> affectedBlocks = Lists.newArrayList();
      List<Entity> entitiesInArea = level.f_46443_
         ? null
         : level.m_45933_(null, new AABB(xFrom, center.f_82480_ - length, zFrom, xTo, center.f_82480_ + length, zTo));
      double bounceExponentCoef = Math.min(1.0 / (length * length), 0.1);

      for (int k = zFrom; k <= zTo; k++) {
         for (int l = xFrom; l <= xTo; l++) {
            Vec2i blockCoord = new Vec2i(l, k);
            if (isBlockOverlapLine(blockCoord, center, edgeOfShockwave)) {
               affectedBlocks.add(blockCoord);
            }
         }
      }

      affectedBlocks.sort((v1, v2) -> {
         double v1DistSqr = Math.pow(v1.x - center.f_82479_, 2.0) + Math.pow(v1.y - center.f_82481_, 2.0);
         double v2DistSqr = Math.pow(v2.x - center.f_82479_, 2.0) + Math.pow(v2.y - center.f_82481_, 2.0);
         if (v1DistSqr > v2DistSqr) {
            return 1;
         } else {
            return v1DistSqr == v2DistSqr ? 0 : -1;
         }
      });
      double y = center.f_82480_;

      for (Vec2i block : affectedBlocks) {
         BlockPos bp = new MutableBlockPos(block.x, y, block.y);
         BlockState bs = level.m_8055_(bp);
         BlockPos aboveBp = bp.m_7494_();
         BlockState aboveState = level.m_8055_(aboveBp);
         if (canTransferShockWave(level, aboveBp, aboveState)) {
            BlockPos aboveTwoBp = aboveBp.m_7494_();
            BlockState aboveTwoState = level.m_8055_(aboveTwoBp);
            if (canTransferShockWave(level, aboveTwoBp, aboveTwoState)) {
               break;
            }

            y++;
            bp = aboveBp;
            bs = aboveState;
         } else if (!level.f_46443_
            && aboveState.m_60742_(level, aboveBp, CollisionContext.m_82749_()).m_83281_()
            && level.m_46469_().m_46207_(GameRules.f_46132_)) {
            level.m_46961_(aboveBp, level.m_46469_().m_46207_(GameRules.f_46136_));
         }

         if (!canTransferShockWave(level, bp, bs)) {
            BlockPos belowBp = bp.m_7495_();
            BlockState belowState = level.m_8055_(belowBp);
            if (!canTransferShockWave(level, belowBp, belowState)) {
               break;
            }

            y--;
            bp = belowBp;
            bs = belowState;
         }

         Vec3 blockCenter = new Vec3(bp.m_123341_() + 0.5, bp.m_123342_(), bp.m_123343_() + 0.5);
         Vec3 centerToBlock = blockCenter.m_82546_(center);
         double distance = centerToBlock.m_165924_();
         if (!(length < distance)) {
            if (level.f_46443_) {
               if (ClientConfig.groundSlams
                  && canTransferShockWave(level, bp, bs)
                  && !(bs instanceof FractureBlockState)
                  && !(bs.m_60734_() instanceof EntityBlock)) {
                  Vec3 rotAxis = IMPACT_DIRECTION.m_82537_(centerToBlock).m_82541_();
                  Vector3f axis = new Vector3f((float)rotAxis.f_82479_, (float)rotAxis.f_82480_, (float)rotAxis.f_82481_);
                  Vector3f translator = new Vector3f(0.0F, Math.max(0.0F, (float)(distance / length) - 0.5F) * 0.5F, 0.0F);
                  Quaternionf rotator = QuaternionUtils.rotationDegrees(axis, (float)(distance / length) * 15.0F + level.f_46441_.m_188501_() * 10.0F - 5.0F);
                  rotator.mul(QuaternionUtils.XP.rotationDegrees(level.f_46441_.m_188501_() * 15.0F - 7.5F));
                  rotator.mul(QuaternionUtils.YP.rotationDegrees(level.f_46441_.m_188501_() * 40.0F - 20.0F));
                  rotator.mul(QuaternionUtils.ZP.rotationDegrees(level.f_46441_.m_188501_() * 15.0F - 7.5F));
                  int lifeTime = 30 + level.f_46441_.m_188503_((int)length * 80);
                  double bouncing = Math.pow(distance, 2.0) * bounceExponentCoef;
                  FractureBlockState fractureBlockState = FractureBlock.getDefaultFractureBlockState(null);
                  fractureBlockState.setFractureInfo(bp, bs, translator, rotator, bouncing, lifeTime);
                  level.m_7731_(bp, fractureBlockState, 0);
                  if (bs.m_245147_()) {
                     createParticle(level, bp, bs);
                  }
               }
            } else {
               for (Entity entity : entitiesInArea) {
                  boolean inSameY = bp.m_123342_() + 1 >= entity.m_20186_() && bp.m_123342_() <= entity.m_20186_();
                  if (bp.m_123341_() == entity.m_146903_() && inSameY && bp.m_123343_() == entity.m_146907_() && !entityBeingHit.contains(entity)) {
                     entityBeingHit.add(entity);
                  }
               }
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void createParticle(Level level, BlockPos bp, BlockState bs) {
      int i = 0;

      while (i < 4) {
         double x = bp.m_123341_() + i % 2;
         double z = bp.m_123343_() + 1 - i % 2;
         TerrainParticle blockParticle = new TerrainParticle((ClientLevel)level, x, bp.m_123342_() + 1, z, 0.0, 0.0, 0.0, bs, bp);
         blockParticle.m_172260_((Math.random() - 0.5) * 0.3, Math.random() * 0.5, (Math.random() - 0.5) * 0.3);
         blockParticle.m_107257_(10 + new Random().nextInt(60));
         Minecraft mc = Minecraft.m_91087_();
         mc.f_91061_.m_107344_(blockParticle);
         i += level.m_213780_().m_188503_(4);
      }
   }

   public static boolean circleSlamFracture(@Nullable LivingEntity caster, Level level, Vec3 center, double radius) {
      return circleSlamFracture(caster, level, center, radius, false, false, true);
   }

   public static boolean circleSlamFracture(@Nullable LivingEntity caster, Level level, Vec3 center, double radius, boolean hurtEntities) {
      return circleSlamFracture(caster, level, center, radius, false, false, hurtEntities);
   }

   public static boolean circleSlamFracture(@Nullable LivingEntity caster, Level level, Vec3 center, double radius, boolean noSound, boolean noParticle) {
      return circleSlamFracture(caster, level, center, radius, noSound, noParticle, true);
   }

   public static boolean circleSlamFracture(
      @Nullable LivingEntity caster, Level level, Vec3 center, double radius, boolean noSound, boolean noParticle, boolean hurtEntities
   ) {
      Vec3 closestEdge = new Vec3(Math.round(center.f_82479_), Math.floor(center.f_82480_), Math.round(center.f_82481_));
      Vec3 centerOfBlock = new Vec3(Math.floor(center.f_82479_) + 0.5, Math.floor(center.f_82480_), Math.floor(center.f_82481_) + 0.5);
      if (closestEdge.m_82557_(center) < centerOfBlock.m_82557_(center)) {
         center = closestEdge;
      } else {
         center = centerOfBlock;
      }

      BlockPos blockPos = new MutableBlockPos(center.f_82479_, center.f_82480_, center.f_82481_);
      BlockState originBlockState = level.m_8055_(blockPos);
      if (!canTransferShockWave(level, blockPos, originBlockState)) {
         return false;
      }

      radius = Math.max(0.5, radius);
      if (!level.f_46443_) {
         EpicFightNetworkManager.sendToAllPlayerTrackingThisChunkWithSelf(new SPFracture(center, radius, noSound, noParticle), level.m_46745_(blockPos));
      }

      int xFrom = (int)Math.floor(center.f_82479_ - radius);
      int xTo = (int)Math.ceil(center.f_82479_ + radius);
      int zFrom = (int)Math.floor(center.f_82481_ - radius);
      int zTo = (int)Math.ceil(center.f_82481_ + radius);
      List<Entity> entityBeingHit = Lists.newArrayList();

      for (int i = zFrom; i <= zTo; i++) {
         for (int j = xFrom; j <= xTo; j += i != zFrom && i != zTo ? xTo - xFrom : 1) {
            Vec3 direction = new Vec3(j - center.f_82479_ + 0.1, 0.0, i - center.f_82481_);
            spreadShockwave(level, center, direction, radius, j, i, entityBeingHit);
         }
      }

      if (!level.f_46443_ && hurtEntities) {
         for (Entity entity : entityBeingHit) {
            if (!entity.m_7306_(caster)) {
               double damageInflict = 1.0 - (entity.m_20182_().m_82554_(center) - radius * 0.5) / radius;
               float damage = (float)(radius * 2.0 * Math.min(damageInflict, 1.0));
               entity.m_6469_(
                  EpicFightDamageSources.shockwave(caster)
                     .setAnimation(Animations.EMPTY_ANIMATION)
                     .setInitialPosition(center)
                     .addRuntimeTag(EpicFightDamageTypeTags.FINISHER)
                     .addRuntimeTag(DamageTypeTags.f_268415_)
                     .setStunType(StunType.KNOCKDOWN),
                  damage
               );
            }
         }
      } else {
         boolean smallSlam = radius < 1.5;
         if (!noSound) {
            level.m_7785_(
               center.f_82479_,
               center.f_82480_,
               center.f_82481_,
               smallSlam ? (SoundEvent)EpicFightSounds.SLAM_LIGHT.get() : (SoundEvent)EpicFightSounds.SLAM_HEAVY.get(),
               SoundSource.BLOCKS,
               1.0F,
               1.0F,
               false
            );
         }

         if (!smallSlam && !noParticle) {
            level.m_7106_((ParticleOptions)EpicFightParticles.GROUND_SLAM.get(), center.f_82479_, center.f_82480_, center.f_82481_, 1.0, radius * 10.0, 0.5);
         }
      }

      return true;
   }

   @Internal
   public void handlePacket(SPFracture packet) {
   }

   public static LevelUtil getInstance() {
      return INSTANCE;
   }

   private LevelUtil() {
   }

   public static boolean canTransferShockWave(Level level, BlockPos blockPos, BlockState blockState) {
      return Block.m_49918_(blockState.m_60742_(level, blockPos, CollisionContext.m_82749_()), Direction.DOWN) || blockState instanceof FractureBlockState;
   }

   private static boolean isBlockOverlapLine(Vec2i vec2, Vec3 from, Vec3 to) {
      return isLinesCross(vec2.x, vec2.y, vec2.x + 1, vec2.y, from.f_82479_, from.f_82481_, to.f_82479_, to.f_82481_)
         || isLinesCross(vec2.x, vec2.y, vec2.x, vec2.y + 1, from.f_82479_, from.f_82481_, to.f_82479_, to.f_82481_)
         || isLinesCross(vec2.x + 1, vec2.y, vec2.x + 1, vec2.y + 1, from.f_82479_, from.f_82481_, to.f_82479_, to.f_82481_)
         || isLinesCross(vec2.x, vec2.y + 1, vec2.x + 1, vec2.y + 1, from.f_82479_, from.f_82481_, to.f_82479_, to.f_82481_);
   }

   private static boolean isLinesCross(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
      double u = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((x2 - x1) * (y4 - y3) - (x4 - x3) * (y2 - y1));
      double v = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((x2 - x1) * (y4 - y3) - (x4 - x3) * (y2 - y1));
      return 0.0 < u && u < 1.0 && 0.0 < v && v < 1.0;
   }

   public static class ClientLevelUtil extends LevelUtil {
      @Internal
      @Override
      public void handlePacket(SPFracture msg) {
         LevelUtil.circleSlamFracture(null, Minecraft.m_91087_().f_91073_, msg.location(), msg.radius(), msg.noSound(), msg.noParticle());
      }
   }
}
