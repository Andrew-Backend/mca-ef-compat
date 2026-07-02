package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.s2c.SkinListResponse;
import fabric.net.mca.resources.ClothingList;
import fabric.net.mca.resources.HairList;
import fabric.net.mca.resources.data.skin.Clothing;
import fabric.net.mca.resources.data.skin.Hair;
import fabric.net.mca.server.world.data.CustomClothingManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_3222;

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
