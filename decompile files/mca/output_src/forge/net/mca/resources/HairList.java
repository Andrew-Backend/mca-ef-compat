package forge.net.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import forge.net.mca.MCA;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.resources.data.skin.Hair;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class HairList extends SimpleJsonResourceReloadListener {
   protected static final ResourceLocation ID = MCA.locate("skins/hair");
   public final HashMap<String, Hair> hair = new HashMap<>();
   private static HairList INSTANCE;

   public static HairList getInstance() {
      return INSTANCE;
   }

   public HairList() {
      super(Resources.GSON, "skins/hair");
      INSTANCE = this;
   }

   protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
      this.hair.clear();
      data.forEach((id, file) -> {
         Gender gender = Gender.byName(id.m_135815_().split("\\.")[0]);
         if (gender == Gender.UNASSIGNED) {
            MCA.LOGGER.warn("Invalid gender for clothing pool: {}", id);
         } else {
            for (String key : file.getAsJsonObject().keySet()) {
               JsonObject object = file.getAsJsonObject().get(key).getAsJsonObject();

               for (int i = 0; i < GsonHelper.m_13824_(object, "count", 1); i++) {
                  String identifier = String.format(Locale.ROOT, key, i);
                  Hair c = new Hair(identifier, gender, GsonHelper.m_13820_(object, "chance", 1.0F));
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
