package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.cobalt.network.Message;

public class VillagerNameResponse implements Message {
   private static final long serialVersionUID = 3907539869834679334L;
   private final String name;

   public VillagerNameResponse(String name) {
      this.name = name;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleVillagerNameResponse(this);
   }

   public String getName() {
      return this.name;
   }
}
