package yesman.epicfight.compat.kubejs.skill;

import dev.latvian.mods.kubejs.typings.Info;
import java.util.Arrays;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class CustomChargeableSkill extends CustomSkill implements ChargeableSkill {
   private final Consumer<PlayerPatch<?>> startCharging;
   private final Consumer<SkillContainer> resetCharging;
   private final int allowedMaxChargingTicks;
   private final int maxChargingTicks;
   private final int minChargingTicks;
   private final Consumer<CustomChargeableSkill.CastSkillContext> castSkill;
   private final Consumer<CustomChargeableSkill.GatherChargingArgumentsContext> gatherChargingArguments;
   private final Consumer<PlayerPatch<?>> chargingTick;
   private final String keyMapping;

   public CustomChargeableSkill(CustomChargeableSkill.CustomChargeableSkillBuilder builder) {
      super(builder);
      this.startCharging = builder.startCharging;
      this.resetCharging = builder.resetCharging;
      this.allowedMaxChargingTicks = builder.allowedMaxChargingTicks;
      this.maxChargingTicks = builder.maxChargingTicks;
      this.minChargingTicks = builder.minChargingTicks;
      this.castSkill = builder.castSkill;
      this.gatherChargingArguments = builder.gatherChargingArguments;
      this.chargingTick = builder.chargingTick;
      this.keyMapping = builder.keyMapping;
   }

   @Override
   public void startHolding(SkillContainer container) {
      ChargeableSkill.super.startHolding(container);
      if (this.startCharging != null) {
         this.startCharging.accept(container.getExecutor());
      }
   }

   @Override
   public void resetHolding(SkillContainer container) {
      if (this.resetCharging != null) {
         this.resetCharging.accept(container);
      }
   }

   @Override
   public int getAllowedMaxChargingTicks() {
      return this.allowedMaxChargingTicks;
   }

   @Override
   public int getMaxChargingTicks() {
      return this.maxChargingTicks;
   }

   @Override
   public int getMinChargingTicks() {
      return this.minChargingTicks;
   }

   @Override
   public void onStopHolding(SkillContainer skillContainer, SPSkillExecutionFeedback spSkillExecutionFeedback) {
      if (this.castSkill != null) {
         this.castSkill.accept(new CustomChargeableSkill.CastSkillContext(this, skillContainer, spSkillExecutionFeedback));
      }
   }

   @Override
   public void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, FriendlyByteBuf buffer) {
      if (this.gatherChargingArguments != null) {
         this.gatherChargingArguments
            .accept(new CustomChargeableSkill.GatherChargingArgumentsContext(this, container.getClientExecutor(), controlEngine, buffer));
      }
   }

   @Override
   public void holdTick(SkillContainer container) {
      ChargeableSkill.super.holdTick(container);
      if (this.chargingTick != null) {
         this.chargingTick.accept(container.getExecutor());
      }
   }

   @Override
   public KeyMapping getKeyMapping() {
      return Arrays.stream(Minecraft.m_91087_().f_91066_.f_92059_).filter(keyMapping -> keyMapping.m_90860_().equals(this.keyMapping)).findFirst().orElse(null);
   }

   public record CastSkillContext(Skill getSkill, SkillContainer getSkillContainer, SPSkillExecutionFeedback getFeedbackPacket) {
   }

   @Info(
      "Creates a new chargeable skill. Must provide at least one of the following:\n- allowedMaxChargingTicks\n- maxChargingTicks\n- minChargingTicks\n- setKeyMapping\n"
   )
   public static class CustomChargeableSkillBuilder extends CustomSkill.CustomSkillBuilder {
      private Consumer<PlayerPatch<?>> startCharging;
      private Consumer<SkillContainer> resetCharging;
      private int allowedMaxChargingTicks;
      private int maxChargingTicks;
      private int minChargingTicks;
      private Consumer<CustomChargeableSkill.CastSkillContext> castSkill;
      private Consumer<CustomChargeableSkill.GatherChargingArgumentsContext> gatherChargingArguments;
      private Consumer<PlayerPatch<?>> chargingTick;
      private String keyMapping;

      public CustomChargeableSkillBuilder(ResourceLocation id) {
         super(id);
      }

      @Info("Called when the skill starts charging.\n")
      public CustomChargeableSkill.CustomChargeableSkillBuilder startCharging(Consumer<PlayerPatch<?>> startCharging) {
         this.startCharging = startCharging;
         return this;
      }

      @Info("Called when the skill charge is reset.\n")
      public CustomChargeableSkill.CustomChargeableSkillBuilder resetCharging(Consumer<SkillContainer> resetCharging) {
         this.resetCharging = resetCharging;
         return this;
      }

      @Info("The maximum amount of ticks the skill can be charged.\n")
      public CustomChargeableSkill.CustomChargeableSkillBuilder allowedMaxChargingTicks(int allowedMaxChargingTicks) {
         this.allowedMaxChargingTicks = allowedMaxChargingTicks;
         return this;
      }

      @Info("The cap for the amount of ticks the skill can be charged.\n")
      public CustomChargeableSkill.CustomChargeableSkillBuilder maxChargingTicks(int maxChargingTicks) {
         this.maxChargingTicks = maxChargingTicks;
         return this;
      }

      @Info("The minimum amount of ticks the skill can be charged before it is cast.\n")
      public CustomChargeableSkill.CustomChargeableSkillBuilder minChargingTicks(int minChargingTicks) {
         this.minChargingTicks = minChargingTicks;
         return this;
      }

      @Info("Called when the skill is done charging and the key is released.\n")
      public CustomChargeableSkill.CustomChargeableSkillBuilder onCastSkill(Consumer<CustomChargeableSkill.CastSkillContext> castSkill) {
         this.castSkill = castSkill;
         return this;
      }

      @Info("Called when the skill has started charging.\n")
      public CustomChargeableSkill.CustomChargeableSkillBuilder gatherChargingArguments(
         Consumer<CustomChargeableSkill.GatherChargingArgumentsContext> gatherChargingArguments
      ) {
         this.gatherChargingArguments = gatherChargingArguments;
         return this;
      }

      @Info(
         "The key mapping that is used to charge the skill.\nThis uses a string that matches the ID of the key mapping. IDs of key mappings can be found in the `options.txt` file in the Minecraft directory. (e.g. `key.jump` or `key.attack`)\n"
      )
      public CustomChargeableSkill.CustomChargeableSkillBuilder setKeyMapping(String keyMapping) {
         this.keyMapping = keyMapping;
         return this;
      }

      public CustomChargeableSkill.CustomChargeableSkillBuilder onChargingTick(Consumer<PlayerPatch<?>> chargingTick) {
         this.chargingTick = chargingTick;
         return this;
      }

      public CustomChargeableSkill createObject() {
         return new CustomChargeableSkill(this);
      }
   }

   public record GatherChargingArgumentsContext(Skill getSkill, LocalPlayerPatch getCaster, ControlEngine getControlEngine, FriendlyByteBuf getBuffer) {
   }
}
