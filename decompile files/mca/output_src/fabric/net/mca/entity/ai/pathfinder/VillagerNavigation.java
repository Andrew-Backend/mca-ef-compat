package fabric.net.mca.entity.ai.pathfinder;

import net.minecraft.class_13;
import net.minecraft.class_1308;
import net.minecraft.class_1409;
import net.minecraft.class_1937;

public class VillagerNavigation extends class_1409 {
   public VillagerNavigation(class_1308 mobEntity, class_1937 world) {
      super(mobEntity, world);
   }

   protected class_13 method_6336(int range) {
      this.field_6678 = new VillagerLandPathNodeMaker();
      this.field_6678.method_15(true);
      return new class_13(this.field_6678, range);
   }
}
