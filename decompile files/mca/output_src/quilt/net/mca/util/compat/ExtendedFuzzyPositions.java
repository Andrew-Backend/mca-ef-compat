package quilt.net.mca.util.compat;

import java.util.function.Predicate;
import net.minecraft.class_2338;

public class ExtendedFuzzyPositions {
   public static class_2338 downWhile(class_2338 pos, int minY, Predicate<class_2338> condition) {
      if (!condition.test(pos)) {
         return pos;
      }

      class_2338 blockPos = pos.method_10074();

      while (blockPos.method_10264() > minY && condition.test(blockPos)) {
         blockPos = blockPos.method_10074();
      }

      return blockPos;
   }
}
