package quilt.net.mca.network.c2s;

import net.minecraft.class_3222;
import quilt.net.mca.Config;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.s2c.ConfigResponse;

public class ConfigRequest implements Message {
   private static final long serialVersionUID = 7108115056986169352L;

   @Override
   public void receive(class_3222 player) {
      NetworkHandler.sendToPlayer(new ConfigResponse(Config.getInstance()), player);
   }
}
