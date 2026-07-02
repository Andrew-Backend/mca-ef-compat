package fabric.net.mca;

import net.minecraft.class_1792;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_6862;
import net.minecraft.class_7924;

public interface TagsMCA {
   interface Blocks {
      class_6862<class_2248> TOMBSTONES = register("tombstones");

      static void bootstrap() {
      }

      static class_6862<class_2248> register(String path) {
         return class_6862.method_40092(class_7924.field_41254, new class_2960("mca", path));
      }
   }

   interface Items {
      class_6862<class_1792> VILLAGER_EGGS = register("villager_eggs");
      class_6862<class_1792> ZOMBIE_EGGS = register("zombie_eggs");
      class_6862<class_1792> VILLAGER_PLANTABLE = register("villager_plantable");
      class_6862<class_1792> BABIES = register("babies");

      static void bootstrap() {
      }

      static class_6862<class_1792> register(String path) {
         return class_6862.method_40092(class_7924.field_41197, new class_2960("mca", path));
      }
   }
}
