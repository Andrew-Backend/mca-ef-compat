package forge.net.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.resources.data.skin.Clothing;
import forge.net.mca.server.world.data.CustomClothingManager;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.jetbrains.annotations.Nullable;

public class ClothingList extends SimpleJsonResourceReloadListener {
   protected static final ResourceLocation ID = MCA.locate("skins/clothing");
   public final HashMap<String, Clothing> clothing = new HashMap<>();
   private static ClothingList INSTANCE;

   public static ClothingList getInstance() {
      return INSTANCE;
   }

   public ClothingList() {
      super(Resources.GSON, "skins/clothing");
      INSTANCE = this;
   }

   protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
      this.clothing.clear();
      data.forEach((id, file) -> {
         Gender gender = Gender.byName(id.m_135815_().split("\\.")[0]);
         if (gender == Gender.UNASSIGNED) {
            MCA.LOGGER.warn("Invalid gender for clothing pool: {}", id);
         } else {
            for (String key : file.getAsJsonObject().keySet()) {
               JsonObject object = file.getAsJsonObject().get(key).getAsJsonObject();

               for (int i = 0; i < GsonHelper.m_13824_(object, "count", 1); i++) {
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
            WeightedPool<String> pool = this.getPool(gender, villager.m_7141_().m_35571_());
            if (pool.entries.isEmpty()) {
               pool = this.getPool(gender, VillagerProfession.f_35585_);
            }

            yield pool;
         }
      };
   }

   public WeightedPool<String> getPool(Gender gender, @Nullable VillagerProfession profession) {
      Map<String, String> map = Config.getInstance().professionConversionsMap;
      String currentValue = profession == null ? "minecraft:none" : BuiltInRegistries.f_256735_.m_7981_(profession).toString();
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
