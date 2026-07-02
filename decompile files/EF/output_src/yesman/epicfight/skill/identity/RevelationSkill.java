package yesman.epicfight.skill.identity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class RevelationSkill extends Skill {
   private static final UUID EVENT_UUID = UUID.fromString("31a396ea-0361-11ee-be56-0242ac120002");
   protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> motions;
   protected final Map<EntityType<?>, Integer> maxRevelationStacks = Maps.newHashMap();
   protected int blockStack;
   protected int parryStack;
   protected int dodgeStack;
   protected int defaultRevelationStacks;

   public static RevelationSkill.Builder createRevelationSkillBuilder() {
      return new RevelationSkill.Builder()
         .addMotion(CapabilityItem.WeaponCategories.LONGSWORD, (item, player) -> Animations.REVELATION_TWOHAND)
         .addMotion(CapabilityItem.WeaponCategories.GREATSWORD, (item, player) -> Animations.REVELATION_TWOHAND)
         .addMotion(CapabilityItem.WeaponCategories.TACHI, (item, player) -> Animations.REVELATION_TWOHAND)
         .setCategory(SkillCategories.IDENTITY)
         .setActivateType(Skill.ActivateType.DURATION)
         .setResource(Skill.Resource.NONE);
   }

   public RevelationSkill(RevelationSkill.Builder builder) {
      super(builder);
      this.motions = builder.motions;
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.maxRevelationStacks.clear();
      this.blockStack = parameters.m_128451_("block_stacks");
      this.parryStack = parameters.m_128451_("parry_stacks");
      this.dodgeStack = parameters.m_128451_("dodge_stacks");
      this.defaultRevelationStacks = parameters.m_128451_("default_revelation_stacks");
      CompoundTag maxStacks = parameters.m_128469_("max_revelations");

      for (String registryName : maxStacks.m_128431_()) {
         EntityType<?> entityType = (EntityType<?>)EntityType.m_20632_(registryName).orElse(null);
         if (entityType != null) {
            this.maxRevelationStacks.put(entityType, maxStacks.m_128451_(registryName));
         } else {
            EpicFightMod.LOGGER.warn("Revelation registry error: no entity type named : " + registryName);
         }
      }
   }

   @Override
   public void onInitiate(SkillContainer container) {
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.SKILL_CAST_EVENT,
         EVENT_UUID,
         event -> {
            if (container.getExecutor().isLogicalClient()) {
               Skill skill = event.getSkillContainer().getSkill();
               if (skill.getCategory() != SkillCategories.WEAPON_INNATE) {
                  return;
               }

               if (container.getExecutor().getTarget() != null) {
                  EpicFightCapabilities.getUnparameterizedEntityPatch(container.getExecutor().getTarget(), LivingEntityPatch.class)
                     .ifPresent(
                        entitypatch -> {
                           if (this.isActivated(container)
                              && container.sendCastRequest((LocalPlayerPatch)container.getExecutor(), ClientEngine.getInstance().controlEngine).isExecutable()) {
                              container.setDuration(0);
                              event.setCanceled(true);
                           }
                        }
                     );
               }
            }
         }
      );
      listener.addEventListener(
         PlayerEventListener.EventType.SET_TARGET_EVENT,
         EVENT_UUID,
         event -> container.getDataManager().setDataSync((SkillDataKey)SkillDataKeys.STACKS.get(), 0)
      );
      listener.addEventListener(
         PlayerEventListener.EventType.DODGE_SUCCESS_EVENT,
         EVENT_UUID,
         event -> {
            LivingEntity target = container.getExecutor().getTarget();
            if (target != null && target.m_7306_(event.getDamageSource().m_7640_())) {
               this.checkStackAndActivate(
                  container,
                  event.getPlayerPatch(),
                  target,
                  container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.STACKS.get()),
                  this.dodgeStack
               );
            }
         },
         -1
      );
      listener.addEventListener(
         PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK,
         EVENT_UUID,
         event -> {
            if (event.getResult() == AttackResult.ResultType.BLOCKED) {
               LivingEntity target = container.getExecutor().getTarget();
               if (target != null && target.m_7306_(event.getDamageSource().m_7640_())) {
                  int stacks = event.isParried() ? this.parryStack : this.blockStack;
                  this.checkStackAndActivate(
                     container,
                     event.getPlayerPatch(),
                     target,
                     container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.STACKS.get()),
                     stacks
                  );
               }
            }
         },
         -1
      );
      listener.addEventListener(PlayerEventListener.EventType.TARGET_INDICATOR_ALERT_CHECK_EVENT, EVENT_UUID, event -> {
         if (this.isActivated(container)) {
            event.setCanceled(false);
         }
      });
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SKILL_CAST_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.SET_TARGET_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.TARGET_INDICATOR_ALERT_CHECK_EVENT, EVENT_UUID);
   }

   @Override
   public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
      super.executeOnServer(container, args);
      CapabilityItem holdingItem = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);
      AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = this.motions.containsKey(holdingItem.getWeaponCategory())
         ? this.motions.get(holdingItem.getWeaponCategory()).apply(holdingItem, container.getExecutor())
         : Animations.REVELATION_ONEHAND;
      container.getExecutor().playAnimationSynchronized(animation, 0.0F);
   }

   public void checkStackAndActivate(SkillContainer container, ServerPlayerPatch playerpatch, LivingEntity target, int stacks, int addStacks) {
      int maxStackSize = this.maxRevelationStacks.getOrDefault(target.m_6095_(), this.defaultRevelationStacks);
      int stacksToAdd = stacks + addStacks;
      if (stacksToAdd < maxStackSize) {
         container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), stacksToAdd);
      } else {
         if (!this.isActivated(container)) {
            this.setDurationSynchronize(container, this.maxDuration);
         }

         container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), 0);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean shouldDraw(SkillContainer container) {
      return container.getExecutor().getTarget() != null;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      PoseStack poseStack = guiGraphics.m_280168_();
      poseStack.m_85836_();
      poseStack.m_252880_(0.0F, gui.getSlidingProgression(), 0.0F);
      guiGraphics.m_280411_(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0.0F, 0.0F, 1, 1, 1, 1);
      int stacks = container.getRemainDuration() > 0
         ? 0
         : this.maxRevelationStacks.getOrDefault(container.getExecutor().getTarget().m_6095_(), this.defaultRevelationStacks)
            - container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.STACKS.get());
      guiGraphics.drawString(gui.getFont(), String.format("%d", stacks), x + 18.0F, y + 14.0F, 16777215, true);
      poseStack.m_85849_();
   }

   public static class Builder extends SkillBuilder<RevelationSkill> {
      protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>>> motions = Maps.newHashMap();

      public RevelationSkill.Builder addMotion(
         WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationManager.AnimationAccessor<? extends StaticAnimation>> function
      ) {
         this.motions.put(weaponCategory, function);
         return this;
      }
   }
}
