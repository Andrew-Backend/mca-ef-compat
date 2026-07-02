package yesman.epicfight.skill.modules;

import yesman.epicfight.skill.SkillContainer;

public interface ChargeableSkill extends HoldableSkill {
   int getAllowedMaxChargingTicks();

   int getMaxChargingTicks();

   int getMinChargingTicks();

   @Override
   default void resetHolding(SkillContainer container) {
      container.getExecutor().setChargingAmount(0);
   }

   @Override
   default void holdTick(SkillContainer container) {
      HoldableSkill.super.holdTick(container);
      container.getExecutor().setChargingAmount(container.getExecutor().getChargingAmount() + 1);
   }
}
