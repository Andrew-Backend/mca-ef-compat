package yesman.epicfight.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class TargetChasingGoal extends MeleeAttackGoal {
   protected final MobPatch<? extends PathfinderMob> mobpatch;
   protected final double attackRadiusSqr;

   public TargetChasingGoal(MobPatch<? extends PathfinderMob> mobpatch, PathfinderMob pathfinderMob, double speedModifier, boolean longMemory) {
      this(mobpatch, pathfinderMob, speedModifier, longMemory, 0.0);
   }

   public TargetChasingGoal(
      MobPatch<? extends PathfinderMob> mobpatch, PathfinderMob pathfinderMob, double speedModifier, boolean longMemory, double attackRadius
   ) {
      super(pathfinderMob, speedModifier, longMemory);
      this.mobpatch = mobpatch;
      this.attackRadiusSqr = attackRadius * attackRadius;
   }

   public void m_8037_() {
      LivingEntity livingentity = this.f_25540_.m_5448_();
      if (livingentity != null) {
         double d0 = this.f_25540_.m_20275_(livingentity.m_20185_(), livingentity.m_20186_(), livingentity.m_20189_());
         if (!(d0 > this.attackRadiusSqr)) {
            this.f_25540_.m_21573_().m_26573_();
            this.f_25540_.m_21563_().m_24960_(livingentity, 30.0F, 30.0F);
         } else {
            super.m_8037_();
         }
      }
   }

   protected void m_6739_(LivingEntity target, double p_25558_) {
   }
}
