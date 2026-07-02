package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;

public class PhaseManagerPatch extends EnderDragonPhaseManager {
   private final DragonPhaseInstance[] patchedPhases = new DragonPhaseInstance[EnderDragonPhase.m_31406_()];

   public PhaseManagerPatch(EnderDragon dragon, EnderDragonPatch dragonpatch) {
      super(dragon);
   }

   public <T extends DragonPhaseInstance> T m_31418_(EnderDragonPhase<T> phase) {
      if (this.patchedPhases != null) {
         int i = phase.m_31405_();
         if (this.patchedPhases[i] == null) {
            this.patchedPhases[i] = phase.m_31400_(this.f_31409_);
         }

         return (T)this.patchedPhases[i];
      } else {
         return (T)phase.m_31400_(this.f_31409_);
      }
   }

   public void m_31416_(EnderDragonPhase<?> phase) {
      if (phase.m_31400_(this.f_31409_) instanceof PatchedDragonPhase || phase == EnderDragonPhase.f_31386_) {
         super.m_31416_(phase);
      }
   }
}
