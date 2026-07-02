package quilt.net.mca.network.s2c;

import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;

public class CustomSkinsChangedMessage implements Message {
   private static final long serialVersionUID = 2044285891943685881L;

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleCustomSkinsChangedMessage(this);
   }
}
