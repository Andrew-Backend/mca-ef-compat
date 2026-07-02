package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.SkinListResponse;
import forge.net.mca.resources.ClothingList;
import forge.net.mca.resources.HairList;
import forge.net.mca.resources.data.skin.Clothing;
import forge.net.mca.resources.data.skin.Hair;
import forge.net.mca.server.world.data.CustomClothingManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;

public class SkinListRequest implements Message {
   private static final long serialVersionUID = -6508206556519152120L;

   private <T> HashMap<String, T> merge(Map<String, T> a, Map<String, T> b) {
      HashMap<String, T> map = new HashMap<>();
      map.putAll(a);
      map.putAll(b);
      return map;
   }

   @Override
   public void receive(ServerPlayer player) {
      Map<String, Clothing> clothing = CustomClothingManager.getClothing().getEntries();
      Map<String, Hair> hair = CustomClothingManager.getHair().getEntries();
      NetworkHandler.sendToPlayer(
         new SkinListResponse(this.merge(ClothingList.getInstance().clothing, clothing), this.merge(HairList.getInstance().hair, hair)), player
      );
   }
}
