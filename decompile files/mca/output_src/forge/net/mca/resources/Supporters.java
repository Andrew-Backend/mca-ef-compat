package forge.net.mca.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

public class Supporters extends SimpleJsonResourceReloadListener {
   protected static final ResourceLocation ID = new ResourceLocation("mca", "api/supporters");
   private static Supporters INSTANCE;
   static final RandomSource rng = RandomSource.m_216327_();
   private final List<String> supporters = new ArrayList<>();
   private final Map<String, List<String>> supporterGroups = new HashMap<>();

   public Supporters() {
      super(Resources.GSON, ID.m_135815_());
      INSTANCE = this;
   }

   public Supporters(Gson gson, String dataType) {
      super(gson, dataType);
   }

   protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
      for (Entry<ResourceLocation, JsonElement> pair : prepared.entrySet()) {
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
