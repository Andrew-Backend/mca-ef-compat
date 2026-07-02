package fabric.net.mca.resources;

import com.google.gson.JsonElement;
import fabric.net.mca.Config;
import fabric.net.mca.MCA;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.server.world.data.Nationality;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4309;
import net.minecraft.class_5819;
import org.jetbrains.annotations.NotNull;

public class Names extends class_4309 {
   protected static final class_2960 ID = MCA.locate("mca_names");
   public static final Map<String, Map<Gender, WeightedPool<String>>> NAMES_MAP = new HashMap<>();
   public static final List<String> REGION_NAMES = new LinkedList<>();
   static final class_5819 random = class_5819.method_43047();

   public Names() {
      super(Resources.GSON, ID.method_12832());
   }

   protected void apply(Map<class_2960, JsonElement> prepared, class_3300 manager, class_3695 profiler) {
      NAMES_MAP.clear();

      for (Entry<class_2960, JsonElement> entry : prepared.entrySet()) {
         String[] split = entry.getKey().method_12832().split("/");
         Gender gender = Gender.byName(split[1]);
         Map<Gender, WeightedPool<String>> map = NAMES_MAP.computeIfAbsent(split[0], a -> new HashMap<>());
         WeightedPool.Mutable<String> names = new WeightedPool.Mutable<>("?");

         for (Entry<String, JsonElement> elementEntry : entry.getValue().getAsJsonObject().entrySet()) {
            names.add(elementEntry.getKey(), (float)Math.pow(elementEntry.getValue().getAsInt(), 0.5));
         }

         map.put(gender, names);
      }

      REGION_NAMES.clear();
      Arrays.stream(NAMES_MAP.keySet().toArray()).sorted().forEach(n -> REGION_NAMES.add(n));
   }

   public static String getCitizenNation(class_1297 entity) {
      if (Config.getInstance().useModernUSANamesOnly) {
         return "modernusa";
      }

      int i = Nationality.get((class_3218)entity.method_37908()).getRegionId(entity.method_24515());
      return REGION_NAMES.get(Math.floorMod(i, REGION_NAMES.size()));
   }

   public static String pickCitizenName(@NotNull Gender gender, class_1297 entity) {
      return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(getCitizenNation(entity)).get(gender.binary()).pickOne();
   }

   public static String pickCitizenName(@NotNull Gender gender) {
      return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(REGION_NAMES.get(random.method_43048(REGION_NAMES.size()))).get(gender.binary()).pickOne();
   }
}
