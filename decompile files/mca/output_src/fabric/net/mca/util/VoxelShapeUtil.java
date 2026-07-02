package fabric.net.mca.util;

import java.util.function.Function;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_265;

public interface VoxelShapeUtil {
   class_243 CENTER = new class_243(0.5, 0.0, 0.5);

   static Function<class_2350, class_265> rotator(class_265 base) {
      return d -> rotate(base, d);
   }

   static class_265 rotate(class_265 shape, class_2350 direction) {
      if (direction.method_10144() == 0.0F) {
         return shape;
      }

      float angle = (float)(-direction.method_10144() * Math.PI / 180.0);
      return class_259.method_17786(
         class_259.method_1073(),
         shape.method_1090()
            .stream()
            .map(
               box -> {
                  class_243 a = rotate(box.field_1323, box.field_1321, angle);
                  class_243 b = rotate(box.field_1320, box.field_1324, angle);
                  class_243 c = rotate(box.field_1323, box.field_1324, angle);
                  class_243 d = rotate(box.field_1320, box.field_1321, angle);
                  return class_259.method_1078(
                     new class_238(
                        Math.min(Math.min(a.field_1352, b.field_1352), Math.min(c.field_1352, d.field_1352)),
                        box.field_1322,
                        Math.min(Math.min(a.field_1350, b.field_1350), Math.min(c.field_1350, d.field_1350)),
                        Math.max(Math.max(a.field_1352, b.field_1352), Math.max(c.field_1352, d.field_1352)),
                        box.field_1325,
                        Math.max(Math.max(a.field_1350, b.field_1350), Math.max(c.field_1350, d.field_1350))
                     )
                  );
               }
            )
            .toArray(class_265[]::new)
      );
   }

   static class_243 rotate(double x, double z, float angle) {
      return new class_243(x, 0.0, z).method_1020(CENTER).method_1024(angle).method_1019(CENTER);
   }
}
