package quilt.net.mca.entity.ai.brain.sensor;

import net.minecraft.class_1309;
import net.minecraft.class_1548;
import net.minecraft.class_4140;
import net.minecraft.class_6045;

public class ExplodingCreeperSensor extends class_6045 {
   protected boolean method_35148(class_1309 entity, class_1309 target) {
      return target instanceof class_1548 && ((class_1548)target).method_7000();
   }

   protected class_4140<class_1309> method_35150() {
      return class_4140.field_18453;
   }
}
