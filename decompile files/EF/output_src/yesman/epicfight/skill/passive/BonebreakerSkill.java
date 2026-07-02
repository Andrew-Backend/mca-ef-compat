package yesman.epicfight.skill.passive;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class BonebreakerSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("06212e76-6dbe-4d4b-a875-562829bb6db4");
   private float damageBonus;
   private int maxDamageBonusStacks;

   public BonebreakerSkill(SkillBuilder<? extends PassiveSkill> builder) {
      super(builder);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.damageBonus = parameters.m_128457_("damage_bonus");
      this.maxDamageBonusStacks = parameters.m_128451_("max_damage_bonus_stacks");
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT,
         EVENT_UUID,
         event -> {
            int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
            if (currentTargetId == -1) {
               container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get(), event.getTarget().m_19879_());
               container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), 1);
               EpicFightNetworkManager.sendToPlayer(
                  new SPEntityPairingPacket(event.getTarget().m_19879_(), EntityPairingPacketTypes.BONEBREAKER_BEGIN),
                  ((ServerPlayerPatch)event.getPlayerPatch()).getOriginal()
               );
            } else if (currentTargetId == event.getTarget().m_19879_()) {
               int stacks = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.STACKS.get());
               event.getDamageSource().attachDamageModifier(ValueModifier.multiplier(1.0F + this.damageBonus * stacks));
               if (stacks + 1 == this.maxDamageBonusStacks) {
                  EpicFightNetworkManager.sendToPlayer(
                     new SPEntityPairingPacket(event.getTarget().m_19879_(), EntityPairingPacketTypes.BONEBREAKER_MAX_STACK),
                     ((ServerPlayerPatch)event.getPlayerPatch()).getOriginal()
                  );
               }

               container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), Math.min(stacks + 1, this.maxDamageBonusStacks));
            }
         }
      );
      listener.addEventListener(
         PlayerEventListener.EventType.ATTACK_PHASE_END_EVENT,
         EVENT_UUID,
         event -> {
            int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
            if (currentTargetId != -1) {
               Entity entity = container.getExecutor().getOriginal().m_9236_().m_6815_(currentTargetId);
               if (!event.getPlayerPatch().getCurrentlyActuallyHitEntities().contains(entity)
                  && event.getPlayerPatch().getCurrentlyActuallyHitEntities().size() > 0) {
                  Entity newTarget = (Entity)event.getPlayerPatch().getCurrentlyActuallyHitEntities().get(0);
                  if (entity != null) {
                     EpicFightNetworkManager.sendToPlayer(
                        new SPEntityPairingPacket(entity.m_19879_(), EntityPairingPacketTypes.BONEBREAKER_CLEAR), event.getPlayerPatch().getOriginal()
                     );
                  }

                  container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get(), newTarget.m_19879_());
                  container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), 1);
                  EpicFightNetworkManager.sendToPlayer(
                     new SPEntityPairingPacket(newTarget.m_19879_(), EntityPairingPacketTypes.BONEBREAKER_BEGIN), event.getPlayerPatch().getOriginal()
                  );
               }
            }
         }
      );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID);
      listener.removeListener(PlayerEventListener.EventType.ATTACK_PHASE_END_EVENT, EVENT_UUID);
      if (!container.getExecutor().isLogicalClient()) {
         int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
         if (currentTargetId != -1) {
            Entity entity = container.getExecutor().getOriginal().m_9236_().m_6815_(currentTargetId);
            if (entity != null) {
               EpicFightNetworkManager.sendToPlayer(
                  new SPEntityPairingPacket(entity.m_19879_(), EntityPairingPacketTypes.BONEBREAKER_CLEAR), container.getServerExecutor().getOriginal()
               );
            }
         }
      }
   }

   @Override
   public void updateContainer(SkillContainer container) {
      super.updateContainer(container);
      if (!container.getExecutor().isLogicalClient()) {
         int currentTargetId = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get());
         if (currentTargetId > -1) {
            Entity entity = container.getExecutor().getOriginal().m_9236_().m_6815_(currentTargetId);
            if (entity == null || !entity.m_6084_()) {
               container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get(), -1);
               container.getDataManager().setDataSync((SkillDataKey<Integer>)SkillDataKeys.STACKS.get(), 0);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean shouldDraw(SkillContainer container) {
      Entity target = container.getExecutor()
         .getOriginal()
         .m_9236_()
         .m_6815_(container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.ENTITY_ID.get()));
      return target != null && target.m_6084_();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
      PoseStack poseStack = guiGraphics.m_280168_();
      poseStack.m_85836_();
      poseStack.m_252880_(0.0F, gui.getSlidingProgression(), 0.0F);
      guiGraphics.m_280411_(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0.0F, 0.0F, 1, 1, 1, 1);
      guiGraphics.drawString(
         gui.getFont(), String.valueOf(container.getDataManager().getDataValue((SkillDataKey)SkillDataKeys.STACKS.get())), x + 10.0F, y + 10.0F, 16777215, true
      );
      poseStack.m_85849_();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(ItemStack.f_41584_.format(this.damageBonus * 100.0F));
      list.add(this.maxDamageBonusStacks);
      return list;
   }
}
