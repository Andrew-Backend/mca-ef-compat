package yesman.epicfight.mixin.common;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;

@Mixin(WitherBoss.class)
public abstract class MixinWitherBoss extends Monster implements PowerableMob, RangedAttackMob {
   @Shadow
   @Final
   private final int[] f_31427_ = new int[2];
   @Shadow
   @Final
   private final int[] f_31428_ = new int[2];
   @Shadow
   @Final
   public ServerBossEvent f_31430_;
   @Shadow
   private int f_31429_;
   @Unique
   private WitherPatch epicFight$epicfightPatch;

   protected MixinWitherBoss(EntityType<? extends WitherBoss> entityType, Level level) {
      super(entityType, level);
   }

   @Inject(at = @At("RETURN"), method = "<init>")
   private void epicfight_witherBossInit(CallbackInfo info) {
      this.epicFight$epicfightPatch = EpicFightCapabilities.getEntityPatch((WitherBoss)this, WitherPatch.class);
   }

   @Inject(at = @At("HEAD"), method = "aiStep()V", cancellable = true)
   private void epicfight_aiStep(CallbackInfo info) {
      if (this.epicFight$epicfightPatch != null) {
         info.cancel();
         WitherBoss self = this.epicFight$epicfightPatch.getOriginal();
         super.m_8107_();

         for (int i = 0; i < 2; i++) {
            self.f_31426_[i] = self.f_31424_[i];
            self.f_31425_[i] = self.f_31423_[i];
         }

         for (int j = 0; j < 2; j++) {
            int k = self.m_31512_(j + 1);
            Entity entity1 = null;
            if (k > 0) {
               entity1 = this.m_9236_().m_6815_(k);
            }

            if (this.epicFight$epicfightPatch.getLaserTargetEntity(j + 1) != null) {
               Entity laserTarget = this.epicFight$epicfightPatch.getLaserTargetEntity(j + 1);
               this.lookAt(j, laserTarget.m_20185_(), laserTarget.m_20188_(), laserTarget.m_20189_(), 360.0F, 360.0F);
            } else if (isValid(this.epicFight$epicfightPatch.getLaserTargetPosition(j + 1))) {
               Vec3 laserTargetPosition = this.epicFight$epicfightPatch.getLaserTargetPosition(j + 1);
               this.lookAt(j, laserTargetPosition.f_82479_, laserTargetPosition.f_82480_, laserTargetPosition.f_82481_, 360.0F, 360.0F);
            } else if (this.epicFight$epicfightPatch.getEntityState().inaction()) {
               self.f_31423_[j] = this.m_31442_(self.f_31423_[j], 0.0F, 40.0F);
               self.f_31424_[j] = this.m_31442_(self.f_31424_[j], this.f_20883_, 10.0F);
            } else if (entity1 != null) {
               this.lookAt(j, entity1.m_20185_(), entity1.m_20188_(), entity1.m_20189_(), 40.0F, 10.0F);
            } else {
               self.f_31424_[j] = this.m_31442_(self.f_31424_[j], this.f_20883_, 10.0F);
            }
         }

         boolean powered = this.m_7090_();

         for (int l = 0; l < 3; l++) {
            double subHeadX = self.m_31514_(l);
            double subHeadY = self.m_31516_(l);
            double subHeadZ = self.m_31518_(l);
            if (!this.epicFight$epicfightPatch.isGhost()) {
               this.m_9236_()
                  .m_7106_(
                     ParticleTypes.f_123762_,
                     subHeadX + this.f_19796_.m_188583_() * 0.3F,
                     subHeadY + this.f_19796_.m_188583_() * 0.3F,
                     subHeadZ + this.f_19796_.m_188583_() * 0.3F,
                     0.0,
                     0.0,
                     0.0
                  );
               if (powered && this.m_9236_().f_46441_.m_188503_(4) == 0) {
                  this.m_9236_()
                     .m_7106_(
                        ParticleTypes.f_123811_,
                        subHeadX + this.f_19796_.m_188583_() * 0.3F,
                        subHeadY + this.f_19796_.m_188583_() * 0.3F,
                        subHeadZ + this.f_19796_.m_188583_() * 0.3F,
                        0.7F,
                        0.7F,
                        0.5
                     );
               }
            }
         }

         if (self.m_31502_() > 0) {
            for (int i1 = 0; i1 < 3; i1++) {
               this.m_9236_()
                  .m_7106_(
                     ParticleTypes.f_123811_,
                     this.m_20185_() + this.f_19796_.m_188583_(),
                     this.m_20186_() + this.f_19796_.m_188501_() * 3.3F,
                     this.m_20189_() + this.f_19796_.m_188583_(),
                     0.7F,
                     0.7F,
                     0.9F
                  );
            }
         }
      }
   }

   @Unique
   private void lookAt(int head, double x, double y, double z, float lerpX, float lerpY) {
      WitherBoss self = this.epicFight$epicfightPatch.getOriginal();
      double d9 = self.m_31514_(head + 1);
      double d1 = self.m_31516_(head + 1);
      double d3 = self.m_31518_(head + 1);
      double d4 = x - d9;
      double d5 = y - d1;
      double d6 = z - d3;
      double d7 = Math.sqrt(d4 * d4 + d6 * d6);
      float f = (float)(Mth.m_14136_(d6, d4) * (180.0 / Math.PI)) - 90.0F;
      float f1 = (float)(-(Mth.m_14136_(d5, d7) * (180.0 / Math.PI)));
      self.f_31423_[head] = this.m_31442_(self.f_31423_[head], f1, lerpX);
      self.f_31424_[head] = this.m_31442_(self.f_31424_[head], f, lerpY);
   }

   @Inject(at = @At("HEAD"), method = "customServerAiStep()V", cancellable = true)
   private void epicfight_customServerAiStep(CallbackInfo info) {
      if (this.epicFight$epicfightPatch != null) {
         info.cancel();
         WitherBoss self = this.epicFight$epicfightPatch.getOriginal();
         if (self.m_31502_() > 0) {
            int k1 = self.m_31502_() - 1;
            this.f_31430_.m_142711_(1.0F - k1 / 220.0F);
            if (k1 <= 0) {
               ExplosionInteraction explosion$blockinteraction = ForgeEventFactory.getMobGriefingEvent(self.m_9236_(), this)
                  ? ExplosionInteraction.BLOCK
                  : ExplosionInteraction.NONE;
               self.m_9236_().m_255391_(this, self.m_20185_(), self.m_20188_(), self.m_20189_(), 7.0F, false, explosion$blockinteraction);
               if (!self.m_20067_()) {
                  self.m_9236_().m_6798_(1023, self.m_20183_(), 0);
               }
            }

            self.m_31510_(k1);
            if (self.f_19797_ % 10 == 0) {
               self.m_5634_(10.0F);
            }
         } else {
            super.m_8024_();

            for (int i = 1; i < 3; i++) {
               if (self.f_19797_ >= this.f_31427_[i - 1]) {
                  this.f_31427_[i - 1] = self.f_19797_ + 10 + self.m_217043_().m_188503_(10);
                  if ((self.m_9236_().m_46791_() == Difficulty.NORMAL || self.m_9236_().m_46791_() == Difficulty.HARD)
                     && !this.epicFight$epicfightPatch.getEntityState().inaction()) {
                     int i3 = i - 1;
                     int j3 = this.f_31428_[i - 1];
                     this.f_31428_[i3] = this.f_31428_[i - 1] + 1;
                     if (j3 > 15) {
                        double d0 = Mth.m_216263_(self.m_217043_(), self.m_20185_() - 10.0, self.m_20185_() + 10.0);
                        double d1 = Mth.m_216263_(self.m_217043_(), self.m_20186_() - 5.0, self.m_20186_() + 5.0);
                        double d2 = Mth.m_216263_(self.m_217043_(), self.m_20189_() - 10.0, self.m_20189_() + 10.0);
                        self.m_31448_(i + 1, d0, d1, d2, true);
                        this.f_31428_[i - 1] = 0;
                     }
                  }

                  int l1 = self.m_31512_(i);
                  if (this.epicFight$epicfightPatch.getEntityState().inaction()) {
                     this.f_31427_[i - 1] = self.f_19797_ + 30;
                  }

                  if (l1 > 0) {
                     LivingEntity livingentity = (LivingEntity)self.m_9236_().m_6815_(l1);
                     if (livingentity == null || !self.m_6779_(livingentity) || self.m_20280_(livingentity) > 900.0 || !self.m_142582_(livingentity)) {
                        self.m_31454_(i, 0);
                     } else if (!this.epicFight$epicfightPatch.getEntityState().inaction()) {
                        self.m_31457_(i + 1, livingentity);
                        this.f_31427_[i - 1] = self.f_19797_ + 40 + self.m_217043_().m_188503_(20);
                        this.f_31428_[i - 1] = 0;
                     }
                  } else {
                     List<LivingEntity> list = self.m_9236_()
                        .m_45971_(LivingEntity.class, WitherPatch.WTIHER_TARGETING_CONDITIONS, this, self.m_20191_().m_82377_(20.0, 8.0, 20.0));
                     if (!list.isEmpty()) {
                        LivingEntity livingentity1 = list.get(self.m_217043_().m_188503_(list.size()));
                        self.m_31454_(i, livingentity1.m_19879_());
                     }
                  }
               }
            }

            if (self.m_5448_() != null) {
               self.m_31454_(0, self.m_5448_().m_19879_());
            } else {
               self.m_31454_(0, 0);
            }

            if (this.f_31429_ > 0) {
               this.f_31429_--;
               if (this.f_31429_ == 0 && ForgeEventFactory.getMobGriefingEvent(self.m_9236_(), this)) {
                  int j1 = Mth.m_14107_(self.m_20186_());
                  int i2 = Mth.m_14107_(self.m_20185_());
                  int j2 = Mth.m_14107_(self.m_20189_());
                  boolean flag = false;

                  for (int j = -1; j <= 1; j++) {
                     for (int k2 = -1; k2 <= 1; k2++) {
                        for (int k = 0; k <= 3; k++) {
                           int l2 = i2 + j;
                           int l = j1 + k;
                           int i1 = j2 + k2;
                           BlockPos blockpos = new BlockPos(l2, l, i1);
                           BlockState blockstate = self.m_9236_().m_8055_(blockpos);
                           if (blockstate.canEntityDestroy(self.m_9236_(), blockpos, this)
                              && ForgeEventFactory.onEntityDestroyBlock(this, blockpos, blockstate)) {
                              flag = self.m_9236_().m_46953_(blockpos, true, this) || flag;
                           }
                        }
                     }
                  }

                  if (flag) {
                     self.m_9236_().m_5898_(null, 1022, self.m_20183_(), 0);
                  }
               }
            }

            this.f_31430_.m_142711_(self.m_21223_() / self.m_21233_());
         }
      }
   }

   @Unique
   public boolean m_5833_() {
      return this.epicFight$epicfightPatch != null ? this.epicFight$epicfightPatch.isGhost() : super.m_5833_();
   }

   @Unique
   protected SoundEvent m_7515_() {
      return this.epicFight$epicfightPatch != null ? (this.epicFight$epicfightPatch.isGhost() ? null : SoundEvents.f_12554_) : null;
   }

   @Shadow
   private float m_31442_(float p_31443_, float p_31444_, float p_31445_) {
      throw new AbstractMethodError("Shadow");
   }

   @Unique
   private static boolean isValid(Vec3 vec) {
      return !Double.isNaN(vec.f_82479_) && !Double.isNaN(vec.f_82480_) && !Double.isNaN(vec.f_82481_);
   }
}
