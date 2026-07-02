package yesman.epicfight.skill.guard;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSetSkillContainerValue;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillCancelEvent;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

public class GuardSkill extends Skill implements HoldableSkill {
   protected static final UUID EVENT_UUID = UUID.fromString("b422f7a0-f378-11eb-9a03-0242ac130003");
   protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions;
   protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> advancedGuardMotions;
   protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardBreakMotions;
   protected float penalizer;

   public static GuardSkill.Builder createGuardBuilder() {
      return new GuardSkill.Builder()
         .addGuardMotion(CapabilityItem.WeaponCategories.AXE, (item, player) -> Animations.SWORD_GUARD_HIT)
         .addGuardMotion(CapabilityItem.WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_HIT)
         .addGuardMotion(CapabilityItem.WeaponCategories.UCHIGATANA, (item, player) -> Animations.UCHIGATANA_GUARD_HIT)
         .addGuardMotion(CapabilityItem.WeaponCategories.LONGSWORD, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
         .addGuardMotion(
            CapabilityItem.WeaponCategories.SPEAR,
            (item, player) -> item.getStyle(player) == CapabilityItem.Styles.TWO_HAND ? Animations.SPEAR_GUARD_HIT : null
         )
         .addGuardMotion(
            CapabilityItem.WeaponCategories.SWORD,
            (item, player) -> item.getStyle(player) == CapabilityItem.Styles.ONE_HAND ? Animations.SWORD_GUARD_HIT : Animations.SWORD_DUAL_GUARD_HIT
         )
         .addGuardMotion(CapabilityItem.WeaponCategories.TACHI, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
         .addGuardBreakMotion(CapabilityItem.WeaponCategories.AXE, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
         .addGuardBreakMotion(CapabilityItem.WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_BREAK)
         .addGuardBreakMotion(CapabilityItem.WeaponCategories.UCHIGATANA, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
         .addGuardBreakMotion(CapabilityItem.WeaponCategories.LONGSWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
         .addGuardBreakMotion(CapabilityItem.WeaponCategories.SPEAR, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
         .addGuardBreakMotion(CapabilityItem.WeaponCategories.SWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
         .addGuardBreakMotion(CapabilityItem.WeaponCategories.TACHI, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
         .setCategory(SkillCategories.GUARD)
         .setActivateType(Skill.ActivateType.HELD)
         .setResource(Skill.Resource.STAMINA);
   }

   public GuardSkill(GuardSkill.Builder builder) {
      super(builder);
      this.guardMotions = builder.guardMotions;
      this.advancedGuardMotions = builder.advancedGuardMotions;
      this.guardBreakMotions = builder.guardBreakMotions;
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.penalizer = parameters.m_128457_("penalizer");
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      container.getExecutor()
         .getEventListener()
         .addEventListener(
            PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE,
            EVENT_UUID,
            event -> container.getDataManager().setDataSync((SkillDataKey)SkillDataKeys.PENALTY.get(), 0.0F)
         );
      container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, event -> {
         if (container.isActivated() && event.getPlayerPatch().getHoldingSkill() == this) {
            event.getPlayerPatch().getOriginal().m_6858_(false);
            event.getPlayerPatch().getOriginal().f_108583_ = -1;
            ControlEngine.setSprintingKeyStateNotDown();
            PlayerInputState current = event.getInputState();
            PlayerInputState updated = current.withForwardImpulse(current.forwardImpulse() * 0.5F).withLeftImpulse(current.leftImpulse() * 0.5F);
            InputManager.setInputState(updated);
         }
      });
      container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, event -> {
         CapabilityItem itemCapability = event.getPlayerPatch().getHoldingItemCapability(InteractionHand.MAIN_HAND);
         if (container.isActivated() && event.getPlayerPatch().getHoldingSkill() == this) {
            DamageSource damageSource = event.getDamageSource();
            boolean isFront = false;
            Vec3 sourceLocation = damageSource.m_7270_();
            if (sourceLocation != null) {
               Vec3 viewVector = event.getPlayerPatch().getOriginal().m_20252_(1.0F);
               viewVector = viewVector.m_82492_(0.0, viewVector.f_82480_, 0.0).m_82541_();
               Vec3 toSourceLocation = sourceLocation.m_82546_(event.getPlayerPatch().getOriginal().m_20182_()).m_82541_();
               if (toSourceLocation.m_82526_(viewVector) > 0.0) {
                  isFront = true;
               }
            }

            if (isFront) {
               float impact = 0.5F;
               float knockback = 0.25F;
               if (event.getDamageSource() instanceof EpicFightDamageSource epicfightDamageSource) {
                  if (epicfightDamageSource.m_269533_(EpicFightDamageTypeTags.GUARD_PUNCTURE)) {
                     return;
                  }

                  impact = epicfightDamageSource.calculateImpact();
                  knockback += Math.min(impact * 0.1F, 1.0F);
               }

               this.guard(container, itemCapability, event, knockback, impact, false);
            }
         }
      }, 1);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onInitiateClient(SkillContainer container) {
      super.onInitiateClient(container);
      container.getExecutor()
         .getEventListener()
         .addEventListener(
            PlayerEventListener.EventType.UPDATE_COMPOSITE_LIVING_MOTION_EVENT,
            EVENT_UUID,
            event -> {
               if (container.isActivated()
                  && this.isHoldingWeaponAvailable(
                     container.getExecutor(), container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND), GuardSkill.BlockType.GUARD
                  )) {
                  event.setMotion(LivingMotions.BLOCK);
               }
            }
         );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, 1);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.CLIENT_ITEM_USE_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SERVER_ITEM_STOP_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onRemoveClient(SkillContainer container) {
      super.onRemoveClient(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.UPDATE_COMPOSITE_LIVING_MOTION_EVENT, EVENT_UUID);
   }

   public void guard(SkillContainer container, CapabilityItem itemCapability, TakeDamageEvent.Attack event, float knockback, float impact, boolean advanced) {
      DamageSource damageSource = event.getDamageSource();
      Entity offender = getOffender(damageSource);
      if (offender != null && this.isBlockableSource(damageSource, advanced)) {
         event.getPlayerPatch().playSound((SoundEvent)EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
         ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
         ((HitParticleType)EpicFightParticles.HIT_BLUNT.get())
            .spawnParticleWithArgument(serverPlayer.m_284548_(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serverPlayer, offender);
         if (offender instanceof LivingEntity livingEntity) {
            knockback += EnchantmentHelper.m_44894_(livingEntity) * 0.1F;
         }

         float penalty = container.getDataManager().<Float>getDataValue((SkillDataKey<Float>)SkillDataKeys.PENALTY.get()) + this.getPenalizer(itemCapability);
         float consumeAmount = penalty * impact;
         boolean canAfford = event.getPlayerPatch().consumeForSkill(this, Skill.Resource.STAMINA, consumeAmount);
         event.getPlayerPatch().knockBackEntity(offender.m_20182_(), knockback);
         container.getDataManager().setDataSync((SkillDataKey<Float>)SkillDataKeys.PENALTY.get(), penalty);
         container.getDataManager()
            .setDataSync((SkillDataKey<Integer>)SkillDataKeys.PENALTY_RESTORE_COUNTER.get(), container.getServerExecutor().getOriginal().f_19797_);
         GuardSkill.BlockType blockType = canAfford ? GuardSkill.BlockType.GUARD : GuardSkill.BlockType.GUARD_BREAK;
         AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = this.getGuardMotion(
            container, event.getPlayerPatch(), itemCapability, blockType
         );
         if (animation != null) {
            event.getPlayerPatch().playAnimationSynchronized(animation, 0.0F);
         }

         if (blockType == GuardSkill.BlockType.GUARD_BREAK) {
            event.getPlayerPatch().playSound((SoundEvent)EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
         }

         this.dealEvent(event.getPlayerPatch(), event, advanced);
      }
   }

   public void dealEvent(PlayerPatch<?> playerpatch, TakeDamageEvent.Attack event, boolean advanced) {
      event.setCanceled(true);
      event.setResult(AttackResult.ResultType.BLOCKED);
      playerpatch.countHurtTime(event.getDamage());
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getDamageSource().m_7639_(), LivingEntityPatch.class)
         .ifPresent(attackerpatch -> attackerpatch.setLastAttackEntity(playerpatch.getOriginal()));
      EpicFightCapabilities.getParameterizedEntityPatch(event.getDamageSource().m_7640_(), LivingEntity.class, LivingEntityPatch.class)
         .ifPresent(entitypatch -> entitypatch.onAttackBlocked(event.getDamageSource(), playerpatch));
   }

   @Override
   public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
      container.deactivate();
      container.getExecutor().resetHolding();
      ServerPlayerPatch executor = container.getServerExecutor();
      SkillCancelEvent skillCancelEvent = new SkillCancelEvent(executor, container);
      executor.getEventListener().triggerEvents(PlayerEventListener.EventType.SKILL_CANCEL_EVENT, skillCancelEvent);
      EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(
         SPSetSkillContainerValue.activate(container.getSlot(), false, container.getExecutor().getOriginal().m_19879_()), container.getExecutor().getOriginal()
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
      container.deactivate();
      super.cancelOnClient(container, args);
   }

   @Override
   public void startHolding(SkillContainer container) {
      container.activate();
      if (!container.getExecutor().isLogicalClient()) {
         EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(
            SPSetSkillContainerValue.activate(container.getSlot(), true, container.getExecutor().getOriginal().m_19879_()),
            container.getExecutor().getOriginal()
         );
      }
   }

   @Override
   public void holdTick(SkillContainer container) {
      if (!container.getExecutor().isLogicalClient() && container.isActivated()) {
         container.getDataManager()
            .setDataSync((SkillDataKey<Integer>)SkillDataKeys.PENALTY_RESTORE_COUNTER.get(), container.getServerExecutor().getOriginal().f_19797_);
      }
   }

   @Override
   public void resetHolding(SkillContainer container) {
      container.deactivate();
   }

   @Override
   public void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, FriendlyByteBuf buffer) {
   }

   @Override
   public void onStopHolding(SkillContainer container, SPSkillExecutionFeedback feedback) {
      container.deactivate();
   }

   @Override
   public KeyMapping getKeyMapping() {
      return EpicFightKeyMappings.GUARD;
   }

   @Override
   public boolean canExecute(SkillContainer container) {
      return this.checkExecuteCondition(container)
         && this.isHoldingWeaponAvailable(
            container.getExecutor(), container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND), GuardSkill.BlockType.GUARD
         );
   }

   protected float getPenalizer(CapabilityItem itemCapability) {
      return this.penalizer;
   }

   protected Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> getGuardMotionMap(GuardSkill.BlockType blockType) {
      return switch (blockType) {
         case GUARD_BREAK -> this.guardBreakMotions;
         case GUARD -> this.guardMotions;
         case ADVANCED_GUARD -> this.advancedGuardMotions;
      };
   }

   public boolean isHoldingWeaponAvailable(PlayerPatch<?> playerpatch, CapabilityItem itemCapability, GuardSkill.BlockType blockType) {
      AnimationManager.AnimationAccessor<? extends StaticAnimation> anim = itemCapability.getGuardMotion(this, blockType, playerpatch);
      if (anim != null) {
         return true;
      }

      Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions = this.getGuardMotionMap(blockType);
      if (!guardMotions.containsKey(itemCapability.getWeaponCategory())) {
         return false;
      }

      Object motion = guardMotions.get(itemCapability.getWeaponCategory()).apply(itemCapability, playerpatch);
      return motion != null;
   }

   @Nullable
   protected AnimationManager.AnimationAccessor<? extends StaticAnimation> getGuardMotion(
      SkillContainer container, PlayerPatch<?> playerpatch, CapabilityItem itemCapability, GuardSkill.BlockType blockType
   ) {
      AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = itemCapability.getGuardMotion(this, blockType, playerpatch);
      return animation != null
         ? animation
         : (AnimationManager.AnimationAccessor)this.getGuardMotionMap(blockType)
            .getOrDefault(itemCapability.getWeaponCategory(), (a, b) -> null)
            .apply(itemCapability, playerpatch);
   }

   @Override
   public void updateContainer(SkillContainer container) {
      super.updateContainer(container);
      if (!container.getExecutor().isLogicalClient()) {
         if (!container.getExecutor().isHoldingSkill(this)) {
            float penalty = container.getDataManager().<Float>getDataValue((SkillDataKey<Float>)SkillDataKeys.PENALTY.get());
            if (penalty > 0.0F) {
               int hitTick = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.PENALTY_RESTORE_COUNTER.get());
               if (container.getExecutor().getOriginal().f_19797_ - hitTick > 40) {
                  container.getDataManager().setDataSync((SkillDataKey<Float>)SkillDataKeys.PENALTY.get(), 0.0F);
               }
            }
         } else {
            container.getExecutor().resetActionTick();
         }
      }
   }

   @Override
   public boolean isExecutableState(PlayerPatch<?> executor) {
      return executor.isEpicFightMode()
         && !executor.isInAir()
         && !executor.getEntityState().hurt()
         && executor.getEntityState().canUseSkill()
         && !executor.isHoldingAny();
   }

   protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
      return !damageSource.m_269533_(DamageTypeTags.f_268738_)
         && !damageSource.m_269533_(EpicFightDamageTypeTags.UNBLOCKALBE)
         && !damageSource.m_269533_(DamageTypeTags.f_268490_)
         && !damageSource.m_269533_(DamageTypeTags.f_268524_)
         && !damageSource.m_269533_(DamageTypeTags.f_268415_)
         && !damageSource.m_276093_(DamageTypes.f_268515_)
         && !damageSource.m_269533_(DamageTypeTags.f_268745_);
   }

   @Override
   public boolean shouldDraw(SkillContainer container) {
      return container.getDataManager().<Float>getDataValue((SkillDataKey<Float>)SkillDataKeys.PENALTY.get()) > 0.0F;
   }

   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      PoseStack poseStack = guiGraphics.m_280168_();
      poseStack.m_85836_();
      poseStack.m_252880_(0.0F, gui.getSlidingProgression(), 0.0F);
      guiGraphics.m_280411_(EpicFightSkills.GUARD.getSkillTexture(), (int)x, (int)y, 24, 24, 0.0F, 0.0F, 1, 1, 1, 1);
      guiGraphics.drawString(
         gui.getFont(), String.format("x%.1f", container.getDataManager().getDataValue((SkillDataKey)SkillDataKeys.PENALTY.get())), x, y + 6.0F, 16777215, true
      );
      poseStack.m_85849_();
   }

   public static Entity getOffender(DamageSource damageSource) {
      return damageSource.m_7640_() == null ? damageSource.m_7639_() : damageSource.m_7640_();
   }

   @Override
   public Set<WeaponCategory> getAvailableWeaponCategories() {
      return this.guardMotions.keySet();
   }

   @Override
   public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
      consumptionList.add(
         Component.m_237115_("attribute.name.epicfight.stamina.consume.tooltip"),
         Component.m_237115_("skill.epicfight.guard.consume.tooltip"),
         SkillBookScreen.STAMINA_TEXTURE_INFO
      );
      return true;
   }

   protected boolean isAdvancedGuard() {
      return false;
   }

   public enum BlockType {
      GUARD_BREAK,
      GUARD,
      ADVANCED_GUARD;
   }

   public static class Builder extends SkillBuilder<GuardSkill> {
      protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions = Maps.newHashMap();
      protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> advancedGuardMotions = Maps.newHashMap();
      protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardBreakMotions = Maps.newHashMap();

      public GuardSkill.Builder addGuardMotion(
         WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>> function
      ) {
         this.guardMotions.put(weaponCategory, function);
         return this;
      }

      public GuardSkill.Builder addAdvancedGuardMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?> function) {
         this.advancedGuardMotions.put(weaponCategory, function);
         return this;
      }

      public GuardSkill.Builder addGuardBreakMotion(
         WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>> function
      ) {
         this.guardBreakMotions.put(weaponCategory, function);
         return this;
      }
   }
}
