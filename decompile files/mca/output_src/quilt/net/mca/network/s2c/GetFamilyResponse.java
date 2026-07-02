package quilt.net.mca.network.s2c;

import net.minecraft.class_2487;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.network.NbtDataMessage;

public class GetFamilyResponse extends NbtDataMessage {
   private static final long serialVersionUID = -8537919427646877115L;

   public GetFamilyResponse(class_2487 data) {
      super(data);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleFamilyDataResponse(this);
   }
}
