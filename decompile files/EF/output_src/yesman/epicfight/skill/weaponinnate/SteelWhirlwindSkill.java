package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.SynchedAnimationVariableKeys;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class SteelWhirlwindSkill extends WeaponInnateSkill implements ChargeableSkill {
   private static final UUID EVENT_UUID = UUID.fromString("d2d057cc-f30f-11ed-a05b-0242ac120003");
   private AnimationManager.AnimationAccessor<? extends StaticAnimation> chargingAnimation = Animations.STEEL_WHIRLWIND_CHARGING;
   private AnimationManager.AnimationAccessor<? extends AttackAnimation> attackAnimation = Animations.STEEL_WHIRLWIND;

   public SteelWhirlwindSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
      super(builder);
   }

   @Override
   public void onInitiate(SkillContainer container) {
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT,
         EVENT_UUID,
         event -> {
            if (event.getPlayerPatch().isHoldingSkill(this)) {
               LocalPlayer clientPlayer = event.getPlayerPatch().getOriginal();
               clientPlayer.m_6858_(false);
               clientPlayer.f_108583_ = -1;
               ControlEngine.setSprintingKeyStateNotDown();
               PlayerInputState current = event.getInputState();
               PlayerInputState updated = current.withForwardImpulse(
                  current.forwardImpulse() * (1.0F - 0.8F * event.getPlayerPatch().getSkillChargingTicks() / 30.0F)
               );
               InputManager.setInputState(updated);
            }
         }
      );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
   }

   public WeaponInnateSkill registerPropertiesToAnimation() {
      AttackAnimation anim = this.attackAnimation.get();

      for (AttackAnimation.Phase phase : anim.phases) {
         phase.addProperties(this.properties.get(0).entrySet());
      }

      return this;
   }

   @Override
   public int getAllowedMaxChargingTicks() {
      return 60;
   }

   @Override
   public int getMaxChargingTicks() {
      return 30;
   }

   @Override
   public int getMinChargingTicks() {
      return 6;
   }

   @Override
   public void startHolding(SkillContainer container) {
      AssetAccessor<? extends StaticAnimation> currentPlaying = container.getExecutor().<Animator>getAnimator().getPlayerFor(null).getRealAnimation();
      if (currentPlaying.get().isMainFrameAnimation()) {
         container.getExecutor().stopPlaying(currentPlaying);
      }

      container.getExecutor().playAnimationSynchronized(this.chargingAnimation, 0.0F);
   }

   @Override
   public void resetHolding(SkillContainer container) {
      if (container.getExecutor().isLogicalClient()) {
         container.getExecutor().<Animator>getAnimator().stopPlaying(this.chargingAnimation);
      } else {
         container.getExecutor().stopPlaying(this.chargingAnimation);
      }
   }

   @Override
   public void onStopHolding(SkillContainer container, SPSkillExecutionFeedback feedback) {
      container.getExecutor()
         .<Animator>getAnimator()
         .getVariables()
         .put(
            (AnimationVariables.IndependentAnimationVariableKey<Integer>)SynchedAnimationVariableKeys.CHARGING_TICKS.get(),
            this.attackAnimation,
            container.getExecutor().getAccumulatedChargeAmount()
         );
      container.getExecutor().playAnimationSynchronized(this.attackAnimation, 0.0F);
      this.cancelOnServer(container, null);
   }

   @Override
   public void holdTick(SkillContainer container) {
      ChargeableSkill.super.holdTick(container);
   }

   @Override
   public KeyMapping getKeyMapping() {
      return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
   }

   @Override
   public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
      List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
      this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strike:");
      return list;
   }
}
