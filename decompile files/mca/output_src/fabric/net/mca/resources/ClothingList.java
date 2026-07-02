package fabric.net.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.resources.data.skin.Clothing;
import fabric.net.mca.server.world.data.CustomClothingManager;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3518;
import net.minecraft.class_3695;
import net.minecraft.class_3852;
import net.minecraft.class_4309;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public class ClothingList extends class_4309 {
   protected static final class_2960 ID = MCA.locate("skins/clothing");
   public final HashMap<String, Clothing> clothing = new HashMap<>();
   private static ClothingList INSTANCE;

   public static ClothingList getInstance() {
      return INSTANCE;
   }

   public ClothingList() {
      super(Resources.GSON, "skins/clothing");
      INSTANCE = this;
   }

   protected void apply(Map<class_2960, JsonElement> data, class_3300 manager, class_3695 profiler) {
      this.clothing.clear();
      data.forEach((id, file) -> {
         Gender gender = Gender.byName(id.method_12832().split("\\.")[0]);
         if (gender == Gender.UNASSIGNED) {
            MCA.LOGGER.warn("Invalid gender for clothing pool: {}", id);
         } else {
            for (String key : file.getAsJsonObject().keySet()) {
               JsonObject object = file.getAsJsonObject().get(key).getAsJsonObject();

               for (int i = 0; i < class_3518.method_15282(object, "count", 1); i++) {
                  String identifier = String.format(Locale.ROOT, key, i);
                  object.addProperty("gender", gender.getId());
                  Clothing c = new Clothing(identifier, object);
                  if (!this.clothing.containsKey(identifier) || !object.has("count")) {
                     this.clothing.put(identifier, c);
                  }
               }
            }
         }
      });
   }

   public WeightedPool<String> getPool(VillagerLike<?> villager) {
      Gender gender = villager.getGenetics().getGender();

      return switch (villager.getAgeState()) {
         case BABY -> this.getPool(gender, MCA.locate("baby").toString());
         case TODDLER -> this.getPool(gender, MCA.locate("toddler").toString());
         case CHILD, TEEN -> this.getPool(gender, MCA.locate("child").toString());
         default -> {
            WeightedPool<String> pool = this.getPool(gender, villager.method_7231().method_16924());
            if (pool.entries.isEmpty()) {
               pool = this.getPool(gender, class_3852.field_17051);
            }

            yield pool;
         }
      };
   }

   public WeightedPool<String> getPool(Gender gender, @Nullable class_3852 profession) {
      Map<String, String> map = Config.getInstance().professionConversionsMap;
      String currentValue = profession == null ? "minecraft:none" : class_7923.field_41195.method_10221(profession).toString();
      String identifier = map.getOrDefault(currentValue, map.getOrDefault("default", currentValue));
      return this.getPool(gender, identifier);
   }

   public WeightedPool<String> getPool(Gender gender, @Nullable String profession) {
      return Stream.concat(this.clothing.values().stream(), CustomClothingManager.getClothing().getEntries().values().stream())
         .filter(c -> c.getGender() == Gender.NEUTRAL || gender == Gender.NEUTRAL || c.getGender() == gender)
         .filter(
            c -> c.profession == null
               || profession == null && !c.exclude
               || c.profession.equals(profession)
               || profession != null && c.profession.equals(profession.replace(":", "."))
         )
         .collect(
            () -> new WeightedPool.Mutable<>("mca:missing"),
            (list, entry) -> list.add(entry.getIdentifier(), entry.getChance()),
            (a, b) -> a.entries.addAll(b.entries)
         );
   }
}
