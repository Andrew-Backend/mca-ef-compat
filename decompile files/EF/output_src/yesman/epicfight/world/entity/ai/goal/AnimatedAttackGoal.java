package yesman.epicfight.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class AnimatedAttackGoal<T extends MobPatch<?>> extends Goal {
   protected final T mobpatch;
   protected final CombatBehaviors<T> combatBehaviors;

   public AnimatedAttackGoal(T mobpatch, CombatBehaviors<T> combatBehaviors) {
      this.mobpatch = mobpatch;
      this.combatBehaviors = combatBehaviors;
   }

   public boolean m_8036_() {
      return this.checkTargetValid();
   }

   public void m_8037_() {
      if (this.mobpatch.getTarget() != null) {
         EntityState state = this.mobpatch.getEntityState();
         this.combatBehaviors.tick();
         if (this.combatBehaviors.hasActivatedMove()) {
            if (state.canBasicAttack()) {
               CombatBehaviors.Behavior<T> result = this.combatBehaviors.tryProceed();
               if (result != null) {
                  result.execute(this.mobpatch);
               }
            }
         } else if (!state.inaction()) {
            CombatBehaviors.Behavior<T> result = this.combatBehaviors.selectRandomBehaviorSeries();
            if (result != null) {
               result.execute(this.mobpatch);
            }
         }
      }
   }

   protected boolean checkTargetValid() {
      LivingEntity livingentity = this.mobpatch.getTarget();
      if (livingentity == null) {
         return false;
      } else {
         return !livingentity.m_6084_() ? false : !(livingentity instanceof Player player && (livingentity.m_5833_() || player.m_7500_()));
      }
   }
}
