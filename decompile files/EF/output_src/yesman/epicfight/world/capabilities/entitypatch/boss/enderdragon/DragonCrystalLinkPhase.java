package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public class DragonCrystalLinkPhase extends PatchedDragonPhase {
   public static final float STUN_SHIELD_AMOUNT = 20.0F;
   public static final int CHARGING_TICK = 158;
   private int absorbCount;
   private EndCrystal absorbingCrystal;

   public DragonCrystalLinkPhase(EnderDragon enderdragon) {
      super(enderdragon);
   }

   public void m_7083_() {
      BlockPos blockpos = this.f_31176_.m_9236_().m_5452_(Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.m_287210_(new BlockPos(0, 0, 0)));
      List<EndCrystal> list = this.f_31176_.m_9236_().m_45976_(EndCrystal.class, new AABB(blockpos).m_82400_(500.0));
      EndCrystal nearestCrystal = null;
      double d0 = Double.MAX_VALUE;

      for (EndCrystal endcrystal : list) {
         double d1 = endcrystal.m_20280_(this.f_31176_);
         if (d1 < d0) {
            d0 = d1;
            nearestCrystal = endcrystal;
         }
      }

      this.absorbingCrystal = nearestCrystal;
      if (this.absorbingCrystal != null) {
         this.dragonpatch.<Animator>getAnimator().playAnimation(Animations.DRAGON_CRYSTAL_LINK, 0.0F);
         this.f_31176_
            .m_9236_()
            .m_7785_(
               this.f_31176_.m_20185_(),
               this.f_31176_.m_20186_(),
               this.f_31176_.m_20189_(),
               (SoundEvent)EpicFightSounds.ENDER_DRAGON_CRYSTAL_LINK.get(),
               this.f_31176_.m_5720_(),
               10.0F,
               1.0F,
               false
            );
         this.absorbingCrystal.m_20331_(true);
         this.absorbCount = 158;
         if (this.dragonpatch.isLogicalClient()) {
            double x = -45.0;
            double z = 0.0;
            Vec3 correction = this.f_31176_.m_20154_().m_82542_(2.0, 0.0, 2.0).m_82492_(0.0, 2.0, 0.0);
            Vec3 spawnPosition = this.f_31176_.m_20182_().m_82546_(correction);

            for (int i = 0; i < 2; i++) {
               for (int j = 0; j < 2; j++) {
                  this.f_31176_
                     .m_9236_()
                     .m_7107_(
                        (ParticleOptions)EpicFightParticles.FORCE_FIELD.get(),
                        spawnPosition.f_82479_,
                        spawnPosition.f_82480_,
                        spawnPosition.f_82481_,
                        x + 90.0 * i,
                        Double.longBitsToDouble(this.f_31176_.m_19879_()),
                        z + 90.0 * j
                     );
               }
            }
         } else if (!this.dragonpatch.isLogicalClient()) {
            int shieldCorrection = this.getPlayersNearbyWithin(100.0).size() - 1;
            float stunShield = 20.0F + 15.0F * shieldCorrection;
            this.dragonpatch.setMaxStunShield(stunShield);
            this.dragonpatch.setStunShield(stunShield);
         }
      } else {
         this.f_31176_.m_31157_().m_31416_(PatchedPhases.GROUND_BATTLE);
      }
   }

   public void m_7081_() {
      if (!this.dragonpatch.isLogicalClient() && this.absorbingCrystal != null) {
         BlockPos blockpos = this.absorbingCrystal.m_20183_();
         this.absorbingCrystal.m_20331_(false);
         this.f_31176_.m_9236_().m_254849_(null, blockpos.m_123341_(), blockpos.m_123342_(), blockpos.m_123343_(), 6.0F, ExplosionInteraction.BLOCK);
      }

      this.f_31176_.f_31086_ = null;
      this.absorbingCrystal = null;
   }

   public float m_7584_(DamageSource damagesource, float amount) {
      if (damagesource instanceof EpicFightDamageSource epicfightDamagesource) {
         this.dragonpatch.setStunShield(this.dragonpatch.getStunShield() - epicfightDamagesource.calculateImpact());
      }

      return amount;
   }

   @Override
   public void m_6991_() {
      super.m_6991_();
      this.f_31176_.f_31075_ = 200;
      this.absorbCount--;
      this.f_31176_.f_31086_ = this.absorbingCrystal;
   }

   public void m_6989_() {
      this.absorbCount--;
      this.f_31176_.f_21363_ = 0;
      if (this.absorbCount > 0) {
         this.f_31176_.m_21153_(this.f_31176_.m_21223_() + 0.5F);
      }
   }

   public int getChargingCount() {
      return this.absorbCount;
   }

   public boolean m_7080_() {
      return true;
   }

   public EnderDragonPhase<? extends DragonPhaseInstance> m_7309_() {
      return PatchedPhases.CRYSTAL_LINK;
   }
}
