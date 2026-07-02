package yesman.epicfight.world.capabilities.entitypatch.boss;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public interface BossPatch<T extends Entity> {
   BossEvent getBossEvent();

   T getOriginal();

   default void recordBossEventOwner(ServerPlayer trackingPlayer) {
      SPEntityPairingPacket packet = new SPEntityPairingPacket(this.getOriginal().m_19879_(), EntityPairingPacketTypes.SET_BOSS_EVENT_OWNER);
      packet.getBuffer().writeBoolean(true);
      packet.getBuffer().m_130077_(this.getBossEvent().m_18860_());
      EpicFightNetworkManager.sendToPlayer(packet, trackingPlayer);
   }

   default void removeBossEventOwner(ServerPlayer trackingPlayer) {
      SPEntityPairingPacket packet = new SPEntityPairingPacket(this.getOriginal().m_19879_(), EntityPairingPacketTypes.SET_BOSS_EVENT_OWNER);
      packet.getBuffer().writeBoolean(false);
      packet.getBuffer().m_130077_(this.getBossEvent().m_18860_());
      EpicFightNetworkManager.sendToPlayer(packet, trackingPlayer);
   }

   default <P extends LivingEntityPatch<?>> P cast() {
      return (P)this;
   }

   @OnlyIn(Dist.CLIENT)
   default void processOwnerRecordPacket(FriendlyByteBuf buffer) {
      boolean addOperation = buffer.readBoolean();
      UUID eventUUID = buffer.m_130259_();
      if (addOperation) {
         ClientEngine.getInstance().renderEngine.addBossEventOwner(eventUUID, this);
      } else {
         ClientEngine.getInstance().renderEngine.removeBossEventOwner(eventUUID, this);
      }
   }
}
