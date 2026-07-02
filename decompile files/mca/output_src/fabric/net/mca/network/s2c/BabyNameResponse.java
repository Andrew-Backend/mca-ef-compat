package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.cobalt.network.Message;

public class BabyNameResponse implements Message {
   private static final long serialVersionUID = -2800883604573859252L;
   private final String name;

   public BabyNameResponse(String name) {
      this.name = name;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleBabyNameResponse(this);
   }

   public String getName() {
      return this.name;
   }
}
