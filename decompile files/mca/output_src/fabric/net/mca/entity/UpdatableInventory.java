package fabric.net.mca.entity;

import net.minecraft.class_1277;
import net.minecraft.class_1297;

public class UpdatableInventory extends class_1277 {
   public UpdatableInventory(int size) {
      super(size);
   }

   public void update(class_1297 entity) {
      for (int slot = 0; slot < this.method_5439(); slot++) {
         if (!this.method_5438(slot).method_7960()) {
            this.method_5438(slot).method_7917(entity.method_37908(), entity, slot, false);
         }
      }
   }
}
