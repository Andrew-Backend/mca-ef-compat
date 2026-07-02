package yesman.epicfight.skill;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.BasicAttackEvent;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillConsumeEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class BasicAttack extends Skill {
   private static final UUID EVENT_UUID = UUID.fromString("a42e0198-fdbc-11eb-9a03-0242ac130003");
   public static final AnimationVariables.IndependentAnimationVariableKey<Boolean> COMBO = AnimationVariables.independent(animator -> false, false);
   private float dashAttackConsumption = 0.0F;
   private float airAttackConsumption = 0.0F;

   public static SkillBuilder<BasicAttack> createBasicAttackBuilder() {
      return new SkillBuilder().setCategory(SkillCategories.BASIC_ATTACK).setActivateType(Skill.ActivateType.ONE_SHOT).setResource(Skill.Resource.NONE);
   }

   public static void setComboCounterWithEvent(
      ComboCounterHandleEvent.Causal reason,
      ServerPlayerPatch playerpatch,
      SkillContainer container,
      @Nullable AnimationManager.AnimationAccessor<? extends MainFrameAnimation> causalAnimation,
      int counter
   ) {
      if (reason == ComboCounterHandleEvent.Causal.TIME_EXPIRED
         || causalAnimation.get().getProperty(AnimationProperty.ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER).orElse(true)) {
         CapabilityItem itemCapability = playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
         int modifiedCombo = itemCapability.handleComboCounter(reason, playerpatch, causalAnimation, counter);
         int prevValue = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.COMBO_COUNTER.get());
         ComboCounterHandleEvent comboResetEvent = new ComboCounterHandleEvent(reason, playerpatch, causalAnimation, prevValue, modifiedCombo);
         container.getExecutor().getEventListener().triggerEvents(PlayerEventListener.EventType.COMBO_COUNTER_HANDLE_EVENT, comboResetEvent);
         List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> comboMotions = itemCapability.getAutoAttackMotion(playerpatch);
         int comboCounterSafe = Mth.m_14045_(comboResetEvent.getNextValue(), 0, comboMotions == null ? 0 : comboMotions.size() - 3);
         container.getDataManager().setData((SkillDataKey<Integer>)SkillDataKeys.COMBO_COUNTER.get(), comboCounterSafe);
      }
   }

   public BasicAttack(SkillBuilder<? extends BasicAttack> builder) {
      super(builder);
   }

   @Override
   public void onInitiate(SkillContainer container) {
      container.getExecutor()
         .getEventListener()
         .addEventListener(
            PlayerEventListener.EventType.ACTION_EVENT_SERVER,
            EVENT_UUID,
            event -> {
               int comboCounter = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.COMBO_COUNTER.get());
               setComboCounterWithEvent(
                  ComboCounterHandleEvent.Causal.ANOTHER_ACTION_ANIMATION, event.getPlayerPatch(), container, event.getAnimation(), comboCounter
               );
            }
         );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.ACTION_EVENT_SERVER, EVENT_UUID);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.dashAttackConsumption = parameters.m_128457_("dash_attack_consumption");
      this.airAttackConsumption = parameters.m_128457_("air_attack_consumption");
   }

   @Override
   public boolean isExecutableState(PlayerPatch<?> executor) {
      EntityState playerState = executor.getEntityState();
      Player player = executor.getOriginal();
      return !player.m_5833_() && !executor.isInAir() && playerState.canBasicAttack();
   }

   @Override
   public void executeOnServer(SkillContainer skillContainer, FriendlyByteBuf args) {
      ServerPlayerPatch executor = skillContainer.getServerExecutor();
      SkillConsumeEvent event = new SkillConsumeEvent(executor, this, this.resource, null);
      executor.getEventListener().triggerEvents(PlayerEventListener.EventType.SKILL_CONSUME_EVENT, event);
      if (!event.isCanceled()) {
         event.getResourceType().consumer.consume(skillContainer, executor, event.getAmount());
      }

      if (!executor.getEventListener().triggerEvents(PlayerEventListener.EventType.BASIC_ATTACK_EVENT, new BasicAttackEvent(executor))) {
         CapabilityItem cap = executor.getHoldingItemCapability(InteractionHand.MAIN_HAND);
         AnimationManager.AnimationAccessor<? extends AttackAnimation> attackMotion = null;
         ServerPlayer player = executor.getOriginal();
         SkillDataManager dataManager = skillContainer.getDataManager();
         int comboCounter = dataManager.<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.COMBO_COUNTER.get());
         boolean dashAttack = player.m_20142_();
         boolean airAttack = !skillContainer.getExecutor().getOriginal().m_20096_() && !skillContainer.getExecutor().getOriginal().m_20069_();
         if (player.m_20159_()) {
            if (player.m_20202_() instanceof PlayerRideableJumping rideable
               && rideable.m_7132_()
               && cap.availableOnHorse()
               && cap.getMountAttackMotion() != null) {
               comboCounter %= cap.getMountAttackMotion().size();
               attackMotion = cap.getMountAttackMotion().get(comboCounter);
               comboCounter++;
            }
         } else {
            List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> combo = cap.getAutoAttackMotion(executor);
            if (combo == null) {
               return;
            }

            int comboSize = combo.size();
            if (airAttack) {
               attackMotion = combo.get(comboSize - 1);
            } else if (dashAttack) {
               attackMotion = combo.get(comboSize - 2);
            } else {
               attackMotion = combo.get(comboCounter);
               comboCounter = (comboCounter + 1) % (comboSize - 2);
            }
         }

         if (!airAttack && !dashAttack) {
            dataManager.setData((SkillDataKey<Integer>)SkillDataKeys.COMBO_COUNTER.get(), comboCounter);
         }

         if (attackMotion != null && this.checkConsumption(skillContainer, dashAttack, airAttack)) {
            executor.<Animator>getAnimator().playAnimation(attackMotion, 0.0F);
            executor.<Animator>getAnimator().getVariables().put(COMBO, attackMotion, true);
            boolean stiffAttack = EpicFightGameRules.STIFF_COMBO_ATTACKS.getRuleValue(executor.getOriginal().m_9236_());
            SPAnimatorControl animatorControlPacket;
            if (stiffAttack) {
               animatorControlPacket = new SPAnimatorControl(AnimatorControlPacket.Action.PLAY, attackMotion, 0.0F, skillContainer.getExecutor());
            } else {
               animatorControlPacket = new SPAnimatorControl(
                  AnimatorControlPacket.Action.PLAY_CLIENT,
                  attackMotion,
                  0.0F,
                  skillContainer.getExecutor(),
                  AnimatorControlPacket.Layer.COMPOSITE_LAYER,
                  AnimatorControlPacket.Priority.HIGHEST
               );
            }

            EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(animatorControlPacket, player);
         }

         executor.updateEntityState();
      }
   }

   @Override
   public void updateContainer(SkillContainer container) {
      if (!container.getExecutor().isLogicalClient()
         && container.getExecutor().getTickSinceLastAction() > 16
         && container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.COMBO_COUNTER.get()) > 0) {
         setComboCounterWithEvent(ComboCounterHandleEvent.Causal.TIME_EXPIRED, container.getServerExecutor(), container, null, 0);
      }
   }

   protected boolean checkConsumption(SkillContainer container, boolean dash, boolean air) {
      float finalConsumption = air ? this.airAttackConsumption : this.dashAttackConsumption;
      if (this.resource == Skill.Resource.STAMINA) {
         finalConsumption = container.getExecutor().getModifiedStaminaConsume(finalConsumption);
      }

      return !air && !dash
         ? container.getExecutor().consumeForSkill(this, this.resource)
         : container.getExecutor().consumeForSkill(this, this.resource, finalConsumption);
   }
}
