package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.network.NbtDataMessage;
import net.minecraft.class_2487;

public class GetVillagerResponse extends NbtDataMessage {
   private static final long serialVersionUID = 4997443623143425383L;

   public GetVillagerResponse(class_2487 data) {
      super(data);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleVillagerDataResponse(this);
   }
}
