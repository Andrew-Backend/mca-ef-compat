package quilt.net.mca.network.c2s;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.s2c.SkinListResponse;
import quilt.net.mca.resources.ClothingList;
import quilt.net.mca.resources.HairList;
import quilt.net.mca.resources.data.skin.Clothing;
import quilt.net.mca.resources.data.skin.Hair;
import quilt.net.mca.server.world.data.CustomClothingManager;

public class SkinListRequest implements Message {
   private static final long serialVersionUID = -6508206556519152120L;

   private <T> HashMap<String, T> merge(Map<String, T> a, Map<String, T> b) {
      HashMap<String, T> map = new HashMap<>();
      map.putAll(a);
      map.putAll(b);
      return map;
   }

   @Override
   public void receive(class_3222 player) {
      Map<String, Clothing> clothing = CustomClothingManager.getClothing().getEntries();
      Map<String, Hair> hair = CustomClothingManager.getHair().getEntries();
      NetworkHandler.sendToPlayer(
         new SkinListResponse(this.merge(ClothingList.getInstance().clothing, clothing), this.merge(HairList.getInstance().hair, hair)), player
      );
   }
}
