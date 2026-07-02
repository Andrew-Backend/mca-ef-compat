package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;

public class DragonNeutralizedPhase extends PatchedDragonPhase {
   public DragonNeutralizedPhase(EnderDragon dragon) {
      super(dragon);
   }

   public void m_7083_() {
      this.dragonpatch.<Animator>getAnimator().playAnimation(Animations.DRAGON_NEUTRALIZED, 0.0F);
      if (this.dragonpatch.isLogicalClient()) {
         Minecraft.m_91087_().m_91106_().m_120386_(((SoundEvent)EpicFightSounds.ENDER_DRAGON_CRYSTAL_LINK.get()).m_11660_(), SoundSource.HOSTILE);
         this.f_31176_
            .m_9236_()
            .m_7106_(
               (ParticleOptions)EpicFightParticles.FORCE_FIELD_END.get(),
               this.f_31176_.m_20185_(),
               this.f_31176_.m_20186_(),
               this.f_31176_.m_20189_(),
               0.0,
               0.0,
               0.0
            );
      }
   }

   public EnderDragonPhase<? extends DragonPhaseInstance> m_7309_() {
      return PatchedPhases.NEUTRALIZED;
   }

   public boolean m_7080_() {
      return true;
   }
}
