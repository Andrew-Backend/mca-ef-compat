package yesman.epicfight.skill.passive;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.network.server.SPPlayUISound;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class VengeanceSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("bcb1f110-c987-412e-9e40-5831c62eba2e");
   private float damageBonus;

   public VengeanceSkill(SkillBuilder<? extends PassiveSkill> builder) {
      super(builder);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.damageBonus = parameters.m_128457_("damage_bonus");
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK,
         EVENT_UUID,
         event -> {
            if (event.getDamageSource().m_7639_() != null) {
               int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
               if (currentTargetId == -1 && event.getDamageSource().m_7639_() instanceof LivingEntity livingentity) {
                  setNewTarget(container, livingentity);
               } else if (currentTargetId == event.getDamageSource().m_7639_().m_19879_()) {
                  container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get(), event.getPlayerPatch().getOriginal().f_19797_);
               } else if (currentTargetId != event.getDamageSource().m_7639_().m_19879_()
                  && event.getDamageSource().m_7639_() instanceof LivingEntity livingentity
                  && canResetTarget(container)) {
                  setNewTarget(container, livingentity);
               }
            }
         }
      );
      listener.addEventListener(
         PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT,
         EVENT_UUID,
         event -> {
            int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
            if (currentTargetId == event.getTarget().m_19879_()) {
               event.getDamageSource().attachDamageModifier(ValueModifier.multiplier(1.0F + this.damageBonus));
               container.getDataManager()
                  .setDataSync((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get(), ((ServerPlayerPatch)event.getPlayerPatch()).getOriginal().f_19797_);
            } else if (container.isActivated()) {
               float f = this.damageBonus * container.getDurationRatio(1.0F);
               event.getDamageSource().attachDamageModifier(ValueModifier.multiplier(1.0F + f));
            }
         }
      );
      listener.addEventListener(PlayerEventListener.EventType.PLAYER_KILLED_EVENT, EVENT_UUID, event -> {
         if (!container.isActivated()) {
            int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
            if (currentTargetId == event.getKilledEntity().m_19879_()) {
               this.executeOnServer(container, null);
               container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get(), -1);
            }
         }
      });
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      cancelTarget(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID);
      listener.removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID);
      listener.removeListener(PlayerEventListener.EventType.PLAYER_KILLED_EVENT, EVENT_UUID);
   }

   @Override
   public void updateContainer(SkillContainer container) {
      super.updateContainer(container);
      if (!container.getExecutor().isLogicalClient()) {
         int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
         if (currentTargetId > -1) {
            Entity entity = container.getExecutor().getOriginal().m_9236_().m_6815_(currentTargetId);
            if (container.getExecutor().getOriginal().f_19797_
                  - container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get())
               >= 160) {
               cancelTarget(container);
            } else if (entity == null || !entity.m_6084_()) {
               cancelTarget(container);
            }
         }
      }
   }

   @Override
   public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
      super.executeOnServer(container, args);
      container.getExecutor().getOriginal().m_5496_((SoundEvent)EpicFightSounds.VENGEANCE.get(), 1.0F, 1.0F);
      EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(
         new SPEntityPairingPacket(container.getExecutor().getOriginal().m_19879_(), EntityPairingPacketTypes.VENGEANCE_OVERLAY),
         container.getServerExecutor().getOriginal()
      );
   }

   @Override
   public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
      super.cancelOnServer(container, args);
      EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(
         new SPEntityPairingPacket(container.getExecutor().getOriginal().m_19879_(), EntityPairingPacketTypes.VENGEANCE_TARGET_CANCEL),
         container.getServerExecutor().getOriginal()
      );
   }

   @Override
   public void onTracked(SkillContainer container, EpicFightNetworkManager.PayloadBundleBuilder payloadBuilder) {
      if (container.isActivated()) {
         payloadBuilder.and(new SPEntityPairingPacket(container.getExecutor().getOriginal().m_19879_(), EntityPairingPacketTypes.VENGEANCE_OVERLAY));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
      container.activate();
      Minecraft.m_91087_().m_91106_().m_120367_(SimpleSoundInstance.m_119755_((SoundEvent)EpicFightSounds.VENGEANCE.get(), 1.0F, 1.0F));
      Minecraft.m_91087_().m_91106_().m_120367_(SimpleSoundInstance.m_119755_((SoundEvent)EpicFightSounds.VENGEANCE.get(), 1.0F, 1.0F));
   }

   public static boolean tickExceeded(SkillContainer container) {
      return container.getExecutor().getOriginal().f_19797_
            - container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get())
         >= 160;
   }

   public static boolean canResetTarget(SkillContainer container) {
      return container.getExecutor().getOriginal().f_19797_
            - container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get())
         >= 80;
   }

   public static void setNewTarget(SkillContainer container, LivingEntity target) {
      cancelTarget(container);
      container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get(), target.m_19879_());
      container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get(), container.getExecutor().getOriginal().f_19797_);
      EpicFightNetworkManager.sendToPlayer(new SPPlayUISound((SoundEvent)EpicFightSounds.VENGEANCE.get()), container.getServerExecutor().getOriginal());
      EpicFightNetworkManager.sendToPlayer(
         new SPEntityPairingPacket(target.m_19879_(), EntityPairingPacketTypes.VENGEANCE_OVERLAY), container.getServerExecutor().getOriginal()
      );
   }

   public static void cancelTarget(SkillContainer container) {
      int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
      Entity entity = container.getExecutor().getOriginal().m_9236_().m_6815_(currentTargetId);
      container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get(), -1);
      if (entity != null && !container.getExecutor().isLogicalClient()) {
         EpicFightNetworkManager.sendToPlayer(
            new SPEntityPairingPacket(currentTargetId, EntityPairingPacketTypes.VENGEANCE_TARGET_CANCEL), container.getServerExecutor().getOriginal()
         );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean shouldDraw(SkillContainer container) {
      return container.isActivated()
         || container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get()) > -1 && !tickExceeded(container);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      PoseStack poseStack = guiGraphics.m_280168_();
      poseStack.m_85836_();
      poseStack.m_252880_(0.0F, gui.getSlidingProgression(), 0.0F);
      guiGraphics.m_280411_(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0.0F, 0.0F, 1, 1, 1, 1);
      if (container.isActivated()) {
         float f = Math.round(this.damageBonus * 100.0F * container.getDurationRatio(1.0F));
         guiGraphics.drawString(gui.getFont(), ItemStack.f_41584_.format(f) + "%", x + 6.0F, y + 8.0F, 16777215, true);
      } else if (canResetTarget(container)) {
         int seconds = 4
            - (
                  container.getExecutor().getOriginal().f_19797_
                     - container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.TICK_RECORD.get())
                     - 80
               )
               / 20;
         guiGraphics.drawString(gui.getFont(), String.valueOf(seconds), x + 6.0F, y + 8.0F, 16777215, true);
      }

      poseStack.m_85849_();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(ItemStack.f_41584_.format(this.damageBonus * 100.0F));
      list.add(ItemStack.f_41584_.format(this.damageBonus * 100.0F));
      list.add(String.valueOf(this.maxDuration / 20));
      return list;
   }
}
