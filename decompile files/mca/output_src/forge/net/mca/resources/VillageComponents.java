package forge.net.mca.resources;

import forge.net.mca.resources.data.NameSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.RandomSource;

public class VillageComponents {
   private final Map<String, NameSet> namePool = new HashMap<>();
   private final RandomSource rng;

   VillageComponents(RandomSource rng) {
      this.rng = rng;
   }

   void load() throws Resources.BrokenResourceException {
      this.namePool.put("village", Resources.read("api/names/village.json", NameSet.class));
   }

   public String pickVillageName(String from) {
      return this.namePool.getOrDefault(from, NameSet.DEFAULT).toName(this.rng);
   }
}
