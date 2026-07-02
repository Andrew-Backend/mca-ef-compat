package yesman.epicfight.skill.passive;

import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class EnduranceSkill extends PassiveSkill {
   private static final UUID EVENT_UUID = UUID.fromString("12ce9f7a-0457-11ee-be56-0242ac120002");
   private float staminaRatio;

   public EnduranceSkill(SkillBuilder<? extends PassiveSkill> builder) {
      super(builder);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.staminaRatio = parameters.m_128457_("stamina_ratio");
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      PlayerEventListener listener = container.getExecutor().getEventListener();
      listener.addEventListener(
         PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK,
         EVENT_UUID,
         event -> {
            if (container.getExecutor().getEntityState().getLevel() == 1
               && event.getDamageSource().m_7639_() != null
               && event.getPlayerPatch().consumeForSkill(this, this.resource)) {
               float staminaConsumption = Math.max(container.getExecutor().getStamina() * this.staminaRatio, 1.5F);
               if (container.getExecutor().consumeForSkill(this, Skill.Resource.STAMINA, staminaConsumption)) {
                  FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                  buf.writeFloat(staminaConsumption);
                  this.executeOnServer(container, buf);
               }
            }
         }
      );
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID);
   }

   @Override
   public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
      super.executeOnServer(container, args);
      float staminaConsume = args.readFloat();
      container.getExecutor().setMaxStunShield(staminaConsume);
      container.getExecutor().setStunShield(staminaConsume);
      Player player = container.getExecutor().getOriginal();
      player.m_9236_()
         .m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), (SoundEvent)EpicFightSounds.ENDURACNE.get(), player.m_5720_(), 1.0F, 1.0F);
      SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(container.getExecutor().getOriginal().m_19879_(), EntityPairingPacketTypes.FLASH_WHITE);
      pairingPacket.getBuffer().writeInt(9);
      pairingPacket.getBuffer().writeInt(15);
      pairingPacket.getBuffer().writeInt(1);
      pairingPacket.getBuffer().writeBoolean(true);
      EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, container.getServerExecutor().getOriginal());
   }

   @Override
   public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
      container.getExecutor().setStunShield(0.0F);
      container.getExecutor().setMaxStunShield(0.0F);
      super.cancelOnServer(container, args);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean shouldDraw(SkillContainer container) {
      return container.getStack() == 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(String.format("%d", this.maxDuration / 20));
      return list;
   }

   @Override
   public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
      consumptionList.add(
         Component.m_237115_("attribute.name.epicfight.cooldown.consume.tooltip"),
         Component.m_237110_("attribute.name.epicfight.cooldown.consume", new Object[]{ItemStack.f_41584_.format(this.getConsumption())}),
         SkillBookScreen.COOLDOWN_TEXTURE_INFO
      );
      consumptionList.add(
         Component.m_237115_("attribute.name.epicfight.stamina.consume.tooltip"),
         Component.m_237110_("attribute.name.epicfight.stamina_current_ratio.consume", new Object[]{ItemStack.f_41584_.format(this.staminaRatio * 100.0F)}),
         SkillBookScreen.STAMINA_TEXTURE_INFO
      );
      return true;
   }
}
