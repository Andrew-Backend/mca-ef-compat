package quilt.net.mca.network.s2c;

import net.minecraft.class_2487;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.network.NbtDataMessage;

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
