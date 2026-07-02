package fabric.net.mca.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4309;
import net.minecraft.class_5819;

public class Supporters extends class_4309 {
   protected static final class_2960 ID = new class_2960("mca", "api/supporters");
   private static Supporters INSTANCE;
   static final class_5819 rng = class_5819.method_43047();
   private final List<String> supporters = new ArrayList<>();
   private final Map<String, List<String>> supporterGroups = new HashMap<>();

   public Supporters() {
      super(Resources.GSON, ID.method_12832());
      INSTANCE = this;
   }

   public Supporters(Gson gson, String dataType) {
      super(gson, dataType);
   }

   protected void apply(Map<class_2960, JsonElement> prepared, class_3300 manager, class_3695 profiler) {
      for (Entry<class_2960, JsonElement> pair : prepared.entrySet()) {
         List<String> strings = this.supporterGroups.computeIfAbsent(pair.getKey().toString(), x -> new LinkedList<>());

         for (JsonElement e : pair.getValue().getAsJsonArray()) {
            this.supporters.add(e.getAsString());
            strings.add(e.getAsString());
         }
      }
   }

   public String pickSupporter() {
      return PoolUtil.pickOne(this.supporters, "nobody", rng);
   }

   public static String getRandomSupporter() {
      return INSTANCE.pickSupporter();
   }

   public static List<String> getSupporterGroup(String group) {
      return INSTANCE.supporterGroups.getOrDefault(group, new LinkedList<>());
   }
}
