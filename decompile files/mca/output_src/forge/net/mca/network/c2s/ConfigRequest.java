package forge.net.mca.network.c2s;

import forge.net.mca.Config;
import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.ConfigResponse;
import net.minecraft.server.level.ServerPlayer;

public class ConfigRequest implements Message {
   private static final long serialVersionUID = 7108115056986169352L;

   @Override
   public void receive(ServerPlayer player) {
      NetworkHandler.sendToPlayer(new ConfigResponse(Config.getInstance()), player);
   }
}
