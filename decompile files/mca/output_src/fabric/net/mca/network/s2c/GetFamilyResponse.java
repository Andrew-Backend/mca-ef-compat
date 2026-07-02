package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.network.NbtDataMessage;
import net.minecraft.class_2487;

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
