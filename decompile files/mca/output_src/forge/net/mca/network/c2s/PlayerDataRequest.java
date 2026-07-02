package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.PlayerDataMessage;
import forge.net.mca.server.world.data.PlayerSaveData;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerDataRequest implements Message {
   private static final long serialVersionUID = -1869959282406697226L;
   private final UUID uuid;

   public PlayerDataRequest(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void receive(ServerPlayer player) {
      if (player.m_9236_().m_46003_(this.uuid) instanceof ServerPlayer serverPlayerEntity) {
         PlayerSaveData data = PlayerSaveData.get(serverPlayerEntity);
         if (data.isEntityDataSet()) {
            CompoundTag nbt = data.getEntityData();
            NetworkHandler.sendToPlayer(new PlayerDataMessage(this.uuid, nbt), player);
         }
      }
   }
}
