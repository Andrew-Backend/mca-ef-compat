package yesman.epicfight.world.capabilities.entitypatch.boss;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import org.joml.Quaternionf;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightDataSerializers;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.DroppedNetherStar;
import yesman.epicfight.world.entity.WitherGhostClone;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class WitherPatch extends MobPatch<WitherBoss> implements BossPatch<WitherBoss> {
   private static final EntityDataAccessor<Boolean> DATA_ARMOR_ACTIVED = SynchedEntityData.m_135353_(WitherBoss.class, EntityDataSerializers.f_135035_);
   private static final EntityDataAccessor<Boolean> DATA_GHOST = SynchedEntityData.m_135353_(WitherBoss.class, EntityDataSerializers.f_135035_);
   private static final EntityDataAccessor<Integer> DATA_TRANSPARENCY = SynchedEntityData.m_135353_(WitherBoss.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Vec3> DATA_LASER_DESTINATION_A = SynchedEntityData.m_135353_(
      WitherBoss.class, (EntityDataSerializer)EpicFightDataSerializers.VEC3.get()
   );
   private static final EntityDataAccessor<Vec3> DATA_LASER_DESTINATION_B = SynchedEntityData.m_135353_(
      WitherBoss.class, (EntityDataSerializer)EpicFightDataSerializers.VEC3.get()
   );
   private static final EntityDataAccessor<Vec3> DATA_LASER_DESTINATION_C = SynchedEntityData.m_135353_(
      WitherBoss.class, (EntityDataSerializer)EpicFightDataSerializers.VEC3.get()
   );
   private static final List<EntityDataAccessor<Vec3>> DATA_LASER_TARGET_POSITIONS = ImmutableList.of(
      DATA_LASER_DESTINATION_A, DATA_LASER_DESTINATION_B, DATA_LASER_DESTINATION_C
   );
   private static final EntityDataAccessor<Integer> DATA_LASER_TARGET_A = SynchedEntityData.m_135353_(WitherBoss.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Integer> DATA_LASER_TARGET_B = SynchedEntityData.m_135353_(WitherBoss.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Integer> DATA_LASER_TARGET_C = SynchedEntityData.m_135353_(WitherBoss.class, EntityDataSerializers.f_135028_);
   private static final List<EntityDataAccessor<Integer>> DATA_LASER_TARGETS = ImmutableList.of(DATA_LASER_TARGET_A, DATA_LASER_TARGET_B, DATA_LASER_TARGET_C);
   public static final TargetingConditions WTIHER_TARGETING_CONDITIONS = TargetingConditions.m_148352_()
      .m_26883_(20.0)
      .m_26888_(livingentity -> livingentity.m_6336_() != MobType.f_21641_ && livingentity.m_5789_());
   public static final TargetingConditions WTIHER_GHOST_TARGETING_CONDITIONS = WTIHER_TARGETING_CONDITIONS.m_148354_().m_148355_();
   private boolean blockedNow;
   private int deathTimerExt;
   private int blockingCount;
   private int blockingStartTick;
   private LivingEntityPatch<?> blockingEntity;

   public void onConstructed(WitherBoss witherBoss) {
      super.onConstructed(witherBoss);
      this.original.m_20088_().m_135372_(DATA_ARMOR_ACTIVED, false);
      this.original.m_20088_().m_135372_(DATA_GHOST, false);
      this.original.m_20088_().m_135372_(DATA_TRANSPARENCY, 0);
      this.original.m_20088_().m_135372_(DATA_LASER_DESTINATION_A, new Vec3(Double.NaN, Double.NaN, Double.NaN));
      this.original.m_20088_().m_135372_(DATA_LASER_DESTINATION_C, new Vec3(Double.NaN, Double.NaN, Double.NaN));
      this.original.m_20088_().m_135372_(DATA_LASER_DESTINATION_B, new Vec3(Double.NaN, Double.NaN, Double.NaN));
      this.original.m_20088_().m_135372_(DATA_LASER_TARGET_A, 0);
      this.original.m_20088_().m_135372_(DATA_LASER_TARGET_B, 0);
      this.original.m_20088_().m_135372_(DATA_LASER_TARGET_C, 0);
   }

   @Override
   public void onStartTracking(ServerPlayer trackingPlayer) {
      this.recordBossEventOwner(trackingPlayer);
   }

   @Override
   public void onStopTracking(ServerPlayer trackingPlayer) {
      this.removeBossEventOwner(trackingPlayer);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void entityPairing(SPEntityPairingPacket packet) {
      super.entityPairing(packet);
      if (packet.getPairingPacketType() == EntityPairingPacketTypes.SET_BOSS_EVENT_OWNER) {
         this.processOwnerRecordPacket(packet.getBuffer());
      }
   }

   @Override
   public void initAI() {
      super.initAI();
      this.original.f_21345_.m_25352_(1, new WitherPatch.WitherChasingGoal());
      this.original.f_21345_.m_25352_(0, new WitherPatch.WitherGhostAttackGoal());
      this.original.f_21345_.m_25352_(0, new AnimatedAttackGoal<>(this, MobCombatBehaviors.WITHER.build(this)));
   }

   public static void initAttributes(EntityAttributeModificationEvent event) {
      event.add(EntityType.f_20496_, (Attribute)EpicFightAttributes.IMPACT.get(), 3.0);
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.WITHER_IDLE);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.WITHER_DEATH);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      if (this.original.m_21223_() <= 0.0F) {
         this.currentLivingMotion = LivingMotions.DEATH;
      } else {
         this.currentLivingMotion = LivingMotions.IDLE;
      }
   }

   @Override
   public void tick(LivingTickEvent event) {
      if (this.original.m_21223_() <= 0.0F && this.original.f_20919_ > 1 && this.deathTimerExt < 17) {
         this.deathTimerExt++;
         this.original.f_20919_--;
      }

      if (!this.getEntityState().inaction()) {
         int targetId = this.original.m_31512_(0);
         Entity target = this.original.m_9236_().m_6815_(targetId);
         if (target != null) {
            Vec3 vec3 = target.m_20182_().m_82546_(this.original.m_20182_()).m_82541_();
            float yrot = MathUtils.rotlerp(
               this.original.m_146908_(), (float)Mth.m_14136_(vec3.f_82481_, vec3.f_82479_) * (180.0F / (float)Math.PI) - 90.0F, 10.0F
            );
            this.original.m_146922_(yrot);
         }
      }

      super.tick(event);
   }

   @Override
   public void poseTick(DynamicAnimation animation, Pose pose, float time, float partialTicks) {
      if (pose.hasTransform("Head_M")) {
         float headRotO = this.original.f_20884_ - this.original.f_20886_;
         float headRot = this.original.f_20883_ - this.original.f_20885_;
         float partialHeadRot = MathUtils.lerpBetween(headRotO, headRot, partialTicks);
         Quaternionf headRotation = OpenMatrix4f.createRotatorDeg(-this.original.m_146909_(), Vec3f.X_AXIS)
            .mulFront(OpenMatrix4f.createRotatorDeg(partialHeadRot, Vec3f.Y_AXIS))
            .toQuaternion();
         pose.orElseEmpty("Head_M").frontResult(JointTransform.rotation(headRotation), OpenMatrix4f::mul);
      }

      if (pose.hasTransform("Head_R")) {
         float rightHeadYRot = MathUtils.lerpBetween(this.original.f_20884_, this.original.f_20883_, partialTicks)
            - MathUtils.lerpBetween(this.original.f_31426_[1], this.original.f_31424_[1], partialTicks);
         float rightHeadXRot = MathUtils.lerpBetween(this.original.f_31425_[1], this.original.f_31423_[1], partialTicks);
         Quaternionf headRotation = OpenMatrix4f.createRotatorDeg(rightHeadYRot, Vec3f.Y_AXIS).rotateDeg(-rightHeadXRot, Vec3f.X_AXIS).toQuaternion();
         pose.orElseEmpty("Head_R").frontResult(JointTransform.rotation(headRotation), OpenMatrix4f::mul);
      }

      if (pose.hasTransform("Head_L")) {
         float leftHeadYRot = MathUtils.lerpBetween(this.original.f_20884_, this.original.f_20883_, partialTicks)
            - MathUtils.lerpBetween(this.original.f_31426_[0], this.original.f_31424_[0], partialTicks);
         float leftHeadXRot = MathUtils.lerpBetween(this.original.f_31425_[0], this.original.f_31423_[0], partialTicks);
         Quaternionf headRotation = OpenMatrix4f.createRotatorDeg(leftHeadYRot, Vec3f.Y_AXIS).rotateDeg(-leftHeadXRot, Vec3f.X_AXIS).toQuaternion();
         pose.orElseEmpty("Head_L").frontResult(JointTransform.rotation(headRotation), OpenMatrix4f::mul);
      }
   }

   @Override
   public void clientTick(LivingTickEvent event) {
      super.clientTick(event);
      this.original.m_20334_(0.0, 0.0, 0.0);
      int transparencyCount = this.getTransparency();
      if (transparencyCount != 0) {
         this.setTransparency(transparencyCount + (transparencyCount > 0 ? -1 : 1));
      }
   }

   @Override
   public void serverTick(LivingTickEvent event) {
      super.serverTick(event);
      if (this.original.m_21223_() <= this.original.m_21233_() * 0.5F) {
         if (!this.isArmorActivated() && !this.getEntityState().inaction() && this.original.m_31502_() <= 0 && this.original.m_6084_()) {
            this.playAnimationSynchronized(Animations.WITHER_SPELL_ARMOR, 0.0F);
         }
      } else if (this.isArmorActivated()) {
         this.setArmorActivated(false);
      }

      if (this.animator.getPlayerFor(null).getAnimation().equals(Animations.WITHER_CHARGE)
         && this.getEntityState().attacking()
         && ForgeEventFactory.getMobGriefingEvent(this.original.m_9236_(), this.original)) {
         int x = Mth.m_14107_(this.original.m_20185_());
         int y = Mth.m_14107_(this.original.m_20186_());
         int z = Mth.m_14107_(this.original.m_20189_());
         boolean flag = false;

         for (int j = -1; j <= 1; j++) {
            for (int k2 = -1; k2 <= 1; k2++) {
               for (int k = 0; k <= 3; k++) {
                  int l2 = x + j;
                  int l = y + k;
                  int i1 = z + k2;
                  BlockPos blockpos = new BlockPos(l2, l, i1);
                  BlockState blockstate = this.original.m_9236_().m_8055_(blockpos);
                  if (blockstate.canEntityDestroy(this.original.m_9236_(), blockpos, this.original)
                     && ForgeEventFactory.onEntityDestroyBlock((LivingEntity)this.original, blockpos, blockstate)) {
                     flag = this.original.m_9236_().m_46953_(blockpos, true, this.original) || flag;
                  }
               }
            }
         }

         if (flag) {
            this.original.m_9236_().m_5898_(null, 1022, this.original.m_20183_(), 0);
         }
      }

      if (this.blockedNow) {
         if (this.blockingCount < 0) {
            this.playAnimationSynchronized(Animations.WITHER_NEUTRALIZED, 0.0F);
            this.original.m_5496_((SoundEvent)EpicFightSounds.NEUTRALIZE_BOSSES.get(), 5.0F, 1.0F);
            this.blockedNow = false;
            this.blockingEntity = null;
         } else if (this.original.f_19797_ % 4 == (this.blockingStartTick - 1) % 4) {
            if (this.original.m_20182_().m_82557_(this.blockingEntity.getOriginal().m_20182_()) < 9.0) {
               EpicFightDamageSource extendedSource = this.getDamageSource(Animations.WITHER_CHARGE, InteractionHand.MAIN_HAND);
               extendedSource.setStunType(StunType.KNOCKDOWN).setBaseImpact(4.0F).setInitialPosition(this.lastAttackPosition);
               AttackResult attackResult = this.tryHarm(this.blockingEntity.getOriginal(), extendedSource, this.blockingCount);
               if (attackResult.resultType == AttackResult.ResultType.SUCCESS) {
                  this.blockingEntity.getOriginal().m_6469_(extendedSource, 4.0F);
                  this.blockedNow = false;
                  this.blockingEntity = null;
               }
            } else {
               this.blockedNow = false;
               this.blockingEntity = null;
            }
         }
      }
   }

   @Override
   public void onAttackBlocked(DamageSource damageSource, LivingEntityPatch<?> opponent) {
      if (damageSource instanceof EpicFightDamageSource extendedDamageSource && Animations.WITHER_CHARGE.equals(extendedDamageSource.getAnimation())) {
         if (!this.blockedNow) {
            this.blockedNow = true;
            this.blockingStartTick = this.original.f_19797_;
            this.blockingEntity = opponent;
            this.playAnimationSynchronized(Animations.WITHER_BLOCKED, 0.0F);
         }

         this.blockingCount--;
         Vec3 lookAngle = opponent.getOriginal().m_20154_();
         lookAngle = lookAngle.m_82492_(0.0, lookAngle.f_82480_, 0.0);
         lookAngle.m_82490_(0.1);
         this.original.m_146884_(opponent.getOriginal().m_20182_().m_82549_(lookAngle));
      }
   }

   @Override
   public AttackResult tryHurt(DamageSource damageSource, float amount) {
      AssetAccessor<? extends DynamicAnimation> animation = this.<Animator>getAnimator().getPlayerFor(null).getAnimation();
      if (animation.equals(Animations.WITHER_CHARGE) || animation.equals(Animations.WITHER_BLOCKED)) {
         Entity entity = damageSource.m_7640_();
         if (entity instanceof AbstractArrow) {
            return AttackResult.blocked(0.0F);
         }
      }

      return super.tryHurt(damageSource, amount);
   }

   @Override
   public void onDeath(LivingDeathEvent event) {
      super.onDeath(event);
      if (!this.isLogicalClient()
         && this.original.m_9236_().m_46469_().m_46207_(GameRules.f_46135_)
         && EpicFightGameRules.EPIC_DROP.getRuleValue(this.original.m_9236_())) {
         Vec3 startMovement = this.original.m_20154_().m_82490_(0.4).m_82520_(0.0, 0.63, 0.0);
         ItemEntity itemEntity = new DroppedNetherStar(
            this.original.m_9236_(), this.original.m_20182_().m_82520_(0.0, this.original.m_20206_() * 0.5, 0.0), startMovement
         );
         this.original.m_9236_().m_7967_(itemEntity);
      }
   }

   @Override
   public boolean onDrop(LivingDropsEvent event) {
      if (EpicFightGameRules.EPIC_DROP.getRuleValue(this.original.m_9236_())) {
         event.getDrops().removeIf(itemEntity -> itemEntity.m_32055_().m_150930_(Items.f_42686_));
      }

      return false;
   }

   @Override
   public OpenMatrix4f getModelMatrix(float partialTicks) {
      float prevYRot;
      float yRot;
      if (this.original.m_20202_() instanceof LivingEntity ridingEntity) {
         prevYRot = ridingEntity.f_20884_;
         yRot = ridingEntity.f_20883_;
      } else {
         prevYRot = this.isLogicalClient() ? this.original.f_20884_ : this.original.f_19859_;
         yRot = this.isLogicalClient() ? this.original.f_20883_ : this.original.m_146908_();
      }

      return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, prevYRot, yRot, partialTicks, 1.0F, 1.0F, 1.0F);
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      return null;
   }

   public void startCharging() {
      this.setLastAttackPosition();
      this.blockingCount = 3;
   }

   public void setArmorActivated(boolean set) {
      this.original.m_20088_().m_135381_(DATA_ARMOR_ACTIVED, set);
   }

   public boolean isArmorActivated() {
      return (Boolean)this.original.m_20088_().m_135370_(DATA_ARMOR_ACTIVED);
   }

   public void setGhost(boolean set) {
      this.original.m_20088_().m_135381_(DATA_GHOST, set);
      this.original.m_20242_(set);
      this.setTransparency(set ? 40 : -40);
      this.original.m_6842_(set);
   }

   public boolean isGhost() {
      return (Boolean)this.original.m_20088_().m_135370_(DATA_GHOST);
   }

   public void setTransparency(int set) {
      this.original.m_20088_().m_135381_(DATA_TRANSPARENCY, set);
   }

   public int getTransparency() {
      return (Integer)this.original.m_20088_().m_135370_(DATA_TRANSPARENCY);
   }

   public void setLaserTargetPosition(int head, Vec3 pos) {
      this.original.m_20088_().m_135381_(DATA_LASER_TARGET_POSITIONS.get(head), pos);
   }

   public Vec3 getLaserTargetPosition(int head) {
      return (Vec3)this.original.m_20088_().m_135370_(DATA_LASER_TARGET_POSITIONS.get(head));
   }

   public void setLaserTarget(int head, Entity target) {
      this.original.m_20088_().m_135381_(DATA_LASER_TARGETS.get(head), target != null ? target.m_19879_() : -1);
   }

   public Entity getLaserTargetEntity(int head) {
      int laserTarget = (Integer)this.original.m_20088_().m_135370_(DATA_LASER_TARGETS.get(head));
      return laserTarget > 0 ? this.original.m_9236_().m_6815_(laserTarget) : null;
   }

   public Entity getAlternativeTargetEntity(int head) {
      int id = this.original.m_31512_(head);
      return id > 0 ? this.original.m_9236_().m_6815_(id) : null;
   }

   public double getHeadX(int index) {
      if (index <= 0) {
         return this.original.m_20185_();
      }

      float f = (this.original.m_146908_() + 180 * (index - 1)) * (float) (Math.PI / 180.0);
      float f1 = Mth.m_14089_(f);
      return this.original.m_20185_() + f1 * 1.3;
   }

   public double getHeadY(int index) {
      return index <= 0 ? this.original.m_20186_() + 3.0 : this.original.m_20186_() + 2.2;
   }

   public double getHeadZ(int index) {
      if (index <= 0) {
         return this.original.m_20189_();
      }

      float f = (this.original.m_146908_() + 180 * (index - 1)) * (float) (Math.PI / 180.0);
      float f1 = Mth.m_14031_(f);
      return this.original.m_20189_() + f1 * 1.3;
   }

   @Override
   public BossEvent getBossEvent() {
      return this.original.f_31430_;
   }

   public class WitherChasingGoal extends Goal {
      public WitherChasingGoal() {
         this.m_7021_(EnumSet.of(Flag.MOVE));
      }

      public boolean m_8036_() {
         return WitherPatch.this.original.m_31512_(0) > 0;
      }

      public void m_8037_() {
         WitherBoss witherBoss = WitherPatch.this.getOriginal();
         Vec3 vec3 = witherBoss.m_20184_().m_82542_(1.0, 0.6, 1.0);
         Entity entity = witherBoss.m_9236_().m_6815_(WitherPatch.this.original.m_31512_(0));
         if (!WitherPatch.this.getEntityState().hurt() && !WitherPatch.this.blockedNow) {
            if (entity != null) {
               Vec3 vec31 = new Vec3(entity.m_20185_() - witherBoss.m_20185_(), 0.0, entity.m_20189_() - witherBoss.m_20189_());
               double d0 = vec3.f_82480_;
               if (witherBoss.m_20186_() < entity.m_20186_()
                  || !witherBoss.m_7090_()
                     && witherBoss.m_20186_() < entity.m_20186_() + 5.0
                     && !WitherPatch.this.<Animator>getAnimator()
                        .getPlayerFor(null)
                        .getAnimation()
                        .get()
                        .getProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL)
                        .orElse(false)) {
                  d0 = Math.max(0.0, d0);
                  d0 += 0.3 - d0 * 0.6F;
               }

               vec3 = new Vec3(vec3.f_82479_, d0, vec3.f_82481_);
               double followingRange = witherBoss.m_7090_() ? 9.0 : 49.0;
               if (vec31.m_165925_() > followingRange && !WitherPatch.this.getEntityState().inaction()) {
                  Vec3 vec32 = vec31.m_82541_();
                  vec3 = vec3.m_82520_(vec32.f_82479_ * 0.3 - vec3.f_82479_ * 0.6, 0.0, vec32.f_82481_ * 0.3 - vec3.f_82481_ * 0.6);
               }
            }

            witherBoss.m_20256_(vec3);
         }
      }
   }

   public class WitherGhostAttackGoal extends Goal {
      private int ghostSummonCount;
      private int maxGhostSpawn;
      private int summonInverval;
      private int cooldown;

      public WitherGhostAttackGoal() {
         this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
      }

      public boolean m_8036_() {
         return --this.cooldown < 0
            && WitherPatch.this.isArmorActivated()
            && !WitherPatch.this.getEntityState().inaction()
            && WitherPatch.this.original.m_5448_() != null;
      }

      public boolean m_8045_() {
         return this.ghostSummonCount <= this.maxGhostSpawn;
      }

      public void m_8056_() {
         WitherPatch.this.playAnimationSynchronized(Animations.WITHER_GHOST_STANDBY, 0.0F);
         WitherPatch.this.updateEntityState();
         WitherPatch.this.setGhost(true);
         List<LivingEntity> nearbyEnemies = this.getNearbyTargets();
         this.ghostSummonCount = 0;
         this.summonInverval = 25;
         this.maxGhostSpawn = Mth.m_14045_(nearbyEnemies.size() / 2, 2, 4);
      }

      public void m_8037_() {
         if (--this.summonInverval <= 0) {
            if (this.ghostSummonCount < this.maxGhostSpawn) {
               List<LivingEntity> nearbyEnemies = this.getNearbyTargets();
               if (!nearbyEnemies.isEmpty()) {
                  LivingEntity randomTarget = nearbyEnemies.get(WitherPatch.this.original.m_217043_().m_188503_(nearbyEnemies.size()));
                  Vec3 summonPosition = randomTarget.m_20182_()
                     .m_82549_(new Vec3(0.0, 0.0, 6.0).m_82524_(WitherPatch.this.original.m_217043_().m_188501_() * 360.0F));
                  WitherGhostClone ghostclone = new WitherGhostClone((ServerLevel)WitherPatch.this.original.m_9236_(), summonPosition, randomTarget);
                  WitherPatch.this.original.m_9236_().m_7967_(ghostclone);
               } else {
                  this.ghostSummonCount = this.maxGhostSpawn + 1;
               }
            }

            this.ghostSummonCount++;
            this.summonInverval = this.ghostSummonCount < this.maxGhostSpawn ? 25 : 35;
            if (this.ghostSummonCount == this.maxGhostSpawn) {
               LivingEntity target = WitherPatch.this.original.m_5448_();
               if (target != null) {
                  Vec3 summonPosition = target.m_20182_()
                     .m_82549_(new Vec3(0.0, 0.0, 6.0).m_82524_(WitherPatch.this.original.m_217043_().m_188501_() * 360.0F))
                     .m_82520_(0.0, 5.0, 0.0);
                  WitherPatch.this.original.m_146884_(summonPosition);
                  WitherPatch.this.original.m_7618_(Anchor.FEET, WitherPatch.this.original.m_5448_().m_20182_());
               }
            }
         }
      }

      public void m_8041_() {
         this.cooldown = 300;
         if (WitherPatch.this.original.m_5448_() != null) {
            WitherPatch.this.playSound(SoundEvents.f_12554_, -0.1F, 0.1F);
            WitherPatch.this.playAnimationSynchronized(Animations.WITHER_CHARGE, 0.0F);
         } else {
            WitherPatch.this.playAnimationSynchronized(Animations.OFF_ANIMATION_HIGHEST, 0.0F);
         }

         WitherPatch.this.setGhost(false);
      }

      public List<LivingEntity> getNearbyTargets() {
         return WitherPatch.this.original
            .m_9236_()
            .m_45971_(
               LivingEntity.class,
               WitherPatch.WTIHER_GHOST_TARGETING_CONDITIONS,
               (LivingEntity)WitherPatch.this.original,
               WitherPatch.this.original.m_20191_().m_82377_(20.0, 5.0, 20.0)
            );
      }
   }
}
