package yesman.epicfight.skill.modules;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

public interface HoldableSkill {
   default void startHolding(SkillContainer container) {
   }

   default void holdTick(SkillContainer container) {
   }

   default void onStopHolding(SkillContainer container, SPSkillExecutionFeedback feedbackPacket) {
   }

   default void resetHolding(SkillContainer container) {
   }

   @OnlyIn(Dist.CLIENT)
   default void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, FriendlyByteBuf buffer) {
   }

   default Skill asSkill() {
      return (Skill)this;
   }

   @OnlyIn(Dist.CLIENT)
   KeyMapping getKeyMapping();
}
