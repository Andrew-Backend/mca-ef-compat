package quilt.net.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3518;
import net.minecraft.class_3695;
import net.minecraft.class_4309;
import quilt.net.mca.MCA;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.resources.data.skin.Hair;

public class HairList extends class_4309 {
   protected static final class_2960 ID = MCA.locate("skins/hair");
   public final HashMap<String, Hair> hair = new HashMap<>();
   private static HairList INSTANCE;

   public static HairList getInstance() {
      return INSTANCE;
   }

   public HairList() {
      super(Resources.GSON, "skins/hair");
      INSTANCE = this;
   }

   protected void apply(Map<class_2960, JsonElement> data, class_3300 manager, class_3695 profiler) {
      this.hair.clear();
      data.forEach((id, file) -> {
         Gender gender = Gender.byName(id.method_12832().split("\\.")[0]);
         if (gender == Gender.UNASSIGNED) {
            MCA.LOGGER.warn("Invalid gender for clothing pool: {}", id);
         } else {
            for (String key : file.getAsJsonObject().keySet()) {
               JsonObject object = file.getAsJsonObject().get(key).getAsJsonObject();

               for (int i = 0; i < class_3518.method_15282(object, "count", 1); i++) {
                  String identifier = String.format(Locale.ROOT, key, i);
                  Hair c = new Hair(identifier, gender, class_3518.method_15277(object, "chance", 1.0F));
                  if (!this.hair.containsKey(identifier) || !object.has("count")) {
                     this.hair.put(identifier, c);
                  }
               }
            }
         }
      });
   }

   public WeightedPool<String> getPool(Gender gender) {
      return this.hair
         .values()
         .stream()
         .filter(c -> c.getGender() == Gender.NEUTRAL || gender == Gender.NEUTRAL || c.getGender() == gender)
         .collect(
            () -> new WeightedPool.Mutable<>("mca:missing"),
            (list, entry) -> list.add(entry.getIdentifier(), entry.getChance()),
            (a, b) -> a.entries.addAll(b.entries)
         );
   }
}
