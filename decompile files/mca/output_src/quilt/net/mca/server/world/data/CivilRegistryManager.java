package quilt.net.mca.server.world.data;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.class_18;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_2561.class_2562;
import quilt.net.mca.util.NbtHelper;
import quilt.net.mca.util.WorldUtils;

public class CivilRegistryManager extends class_18 {
   private final LinkedList<class_2561> entries = new LinkedList<>();

   public static CivilRegistryManager get(class_3218 world, Village village) {
      return WorldUtils.loadData(
         world.method_8503().method_30002(), CivilRegistryManager::new, CivilRegistryManager::new, "mca_civil_registry_" + village.getId()
      );
   }

   CivilRegistryManager(class_3218 world) {
   }

   CivilRegistryManager(class_2487 nbt) {
      this.entries.addAll(NbtHelper.toList(nbt.method_10580("entries"), element -> class_2562.method_10877(element.method_10714())));
   }

   public class_2487 method_75(class_2487 nbt) {
      class_2499 elements = NbtHelper.fromList(this.entries, a -> class_2519.method_23256(class_2562.method_10867(a)));
      class_2487 compound = new class_2487();
      compound.method_10566("entries", elements);
      return compound;
   }

   public void addText(class_2561 text) {
      this.entries.addFirst(text);
      this.method_80();
   }

   public List<class_2561> getPage(int from, int to) {
      to = Math.min(this.entries.size(), to);
      return to <= from ? List.of() : this.entries.subList(from, to);
   }
}
