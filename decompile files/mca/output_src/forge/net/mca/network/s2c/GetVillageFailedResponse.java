package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;

public class GetVillageFailedResponse implements Message {
   private static final long serialVersionUID = 4021214184633955444L;

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleVillageDataFailedResponse(this);
   }
}
