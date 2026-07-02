package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.Config;
import fabric.net.mca.cobalt.network.Message;
import net.minecraft.class_3222;

public class OpenDestinyGuiRequest implements Message {
   private static final long serialVersionUID = -8912548616237596312L;
   public final int player;
   public final boolean allowTeleportation;

   public OpenDestinyGuiRequest(class_3222 player) {
      this.player = player.method_5628();
      this.allowTeleportation = Config.getInstance().allowDestinyTeleportation;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleDestinyGuiRequest(this);
   }
}
