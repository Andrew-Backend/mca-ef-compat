package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;

public class CustomSkinsChangedMessage implements Message {
   private static final long serialVersionUID = 2044285891943685881L;

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleCustomSkinsChangedMessage(this);
   }
}
