package quilt.net.mca.network.s2c;

import java.util.HashMap;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.resources.data.skin.Clothing;
import quilt.net.mca.resources.data.skin.Hair;

public class SkinListResponse implements Message {
   private static final long serialVersionUID = 3523559818338225910L;
   private final HashMap<String, Clothing> clothing;
   private final HashMap<String, Hair> hair;

   public SkinListResponse(HashMap<String, Clothing> clothing, HashMap<String, Hair> hair) {
      this.clothing = clothing;
      this.hair = hair;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleSkinListResponse(this);
   }

   public HashMap<String, Clothing> getClothing() {
      return this.clothing;
   }

   public HashMap<String, Hair> getHair() {
      return this.hair;
   }
}
