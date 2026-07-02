package forge.net.mca.resources;

import com.google.gson.JsonElement;
import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.server.world.data.Nationality;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class Names extends SimpleJsonResourceReloadListener {
   protected static final ResourceLocation ID = MCA.locate("mca_names");
   public static final Map<String, Map<Gender, WeightedPool<String>>> NAMES_MAP = new HashMap<>();
   public static final List<String> REGION_NAMES = new LinkedList<>();
   static final RandomSource random = RandomSource.m_216327_();

   public Names() {
      super(Resources.GSON, ID.m_135815_());
   }

   protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
      NAMES_MAP.clear();

      for (Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
         String[] split = entry.getKey().m_135815_().split("/");
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

   public static String getCitizenNation(Entity entity) {
      if (Config.getInstance().useModernUSANamesOnly) {
         return "modernusa";
      }

      int i = Nationality.get((ServerLevel)entity.m_9236_()).getRegionId(entity.m_20183_());
      return REGION_NAMES.get(Math.floorMod(i, REGION_NAMES.size()));
   }

   public static String pickCitizenName(@NotNull Gender gender, Entity entity) {
      return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(getCitizenNation(entity)).get(gender.binary()).pickOne();
   }

   public static String pickCitizenName(@NotNull Gender gender) {
      return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(REGION_NAMES.get(random.m_188503_(REGION_NAMES.size()))).get(gender.binary()).pickOne();
   }
}
