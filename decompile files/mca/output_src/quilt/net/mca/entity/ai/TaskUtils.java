package quilt.net.mca.entity.ai;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2338.class_2339;
import org.jetbrains.annotations.Nullable;

public interface TaskUtils {
   static int getSpawnSafeTopLevel(class_1937 world, int x, int y, int z) {
      class_2339 pos = new class_2339(x, Math.min(y, world.method_31600()), z);

      while (world.method_22347(pos.method_10098(class_2350.field_11033)) && pos.method_10264() > world.method_31607()) {
      }

      return pos.method_10264() + 1;
   }

   static List<class_2338> getNearbyBlocks(class_2338 origin, class_1937 world, @Nullable Predicate<class_2680> filter, int xzDist, int yDist) {
      return class_2338.method_25998(origin, xzDist, yDist, xzDist)
         .filter(pos -> !origin.equals(pos) && (filter == null || filter.test(world.method_8320(pos))))
         .<class_2338>map(class_2338::method_10062)
         .toList();
   }

   @Nullable
   static class_2338 getNearestPoint(class_2338 origin, List<class_2338> blocks) {
      return blocks.stream().min(Comparator.comparing(origin::method_10262)).orElse(null);
   }
}
