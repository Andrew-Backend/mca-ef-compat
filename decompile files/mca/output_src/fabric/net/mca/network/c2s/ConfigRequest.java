package fabric.net.mca.network.c2s;

import fabric.net.mca.Config;
import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.s2c.ConfigResponse;
import net.minecraft.class_3222;

public class ConfigRequest implements Message {
   private static final long serialVersionUID = 7108115056986169352L;

   @Override
   public void receive(class_3222 player) {
      NetworkHandler.sendToPlayer(new ConfigResponse(Config.getInstance()), player);
   }
}
