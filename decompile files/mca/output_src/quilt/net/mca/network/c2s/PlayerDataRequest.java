package quilt.net.mca.network.c2s;

import java.util.UUID;
import net.minecraft.class_2487;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.s2c.PlayerDataMessage;
import quilt.net.mca.server.world.data.PlayerSaveData;

public class PlayerDataRequest implements Message {
   private static final long serialVersionUID = -1869959282406697226L;
   private final UUID uuid;

   public PlayerDataRequest(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void receive(class_3222 player) {
      if (player.method_37908().method_18470(this.uuid) instanceof class_3222 serverPlayerEntity) {
         PlayerSaveData data = PlayerSaveData.get(serverPlayerEntity);
         if (data.isEntityDataSet()) {
            class_2487 nbt = data.getEntityData();
            NetworkHandler.sendToPlayer(new PlayerDataMessage(this.uuid, nbt), player);
         }
      }
   }
}
