package quilt.net.mca.resources;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_5819;
import quilt.net.mca.resources.data.NameSet;

public class VillageComponents {
   private final Map<String, NameSet> namePool = new HashMap<>();
   private final class_5819 rng;

   VillageComponents(class_5819 rng) {
      this.rng = rng;
   }

   void load() throws Resources.BrokenResourceException {
      this.namePool.put("village", Resources.read("api/names/village.json", NameSet.class));
   }

   public String pickVillageName(String from) {
      return this.namePool.getOrDefault(from, NameSet.DEFAULT).toName(this.rng);
   }
}
