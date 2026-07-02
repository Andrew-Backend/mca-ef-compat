package fabric.net.mca.entity.interaction.gifts;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import fabric.net.mca.MCA;
import fabric.net.mca.resources.Resources;
import java.util.Map;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3518;
import net.minecraft.class_3695;
import net.minecraft.class_4309;

public class GiftLoader extends class_4309 {
   protected static final class_2960 ID = new class_2960("mca", "gifts");

   public GiftLoader() {
      super(Resources.GSON, "gifts");
   }

   protected void apply(Map<class_2960, JsonElement> data, class_3300 manager, class_3695 profiler) {
      GiftType.REGISTRY.clear();
      data.forEach((id, json) -> {
         try {
            GiftType.REGISTRY.add(GiftType.fromJson(id, class_3518.method_15295(json, "root")));
         } catch (JsonParseException e) {
            MCA.LOGGER.error("Could not load gift type for id {}", id, e);
         }
      });

      for (GiftType type : GiftType.REGISTRY) {
         if (!type.getId().method_12836().equals("mca") && type.getConditions().isEmpty()) {
            for (GiftType extendingType : GiftType.REGISTRY) {
               if (extendingType.getId().method_12836().equals("mca") && extendingType.getId().method_12832().equals(type.getId().method_12832())) {
                  type.extendFrom(extendingType);
                  break;
               }
            }
         }
      }
   }
}
