package forge.net.mca.util.compat;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;

public class ExtendedFuzzyPositions {
   public static BlockPos downWhile(BlockPos pos, int minY, Predicate<BlockPos> condition) {
      if (!condition.test(pos)) {
         return pos;
      }

      BlockPos blockPos = pos.m_7495_();

      while (blockPos.m_123342_() > minY && condition.test(blockPos)) {
         blockPos = blockPos.m_7495_();
      }

      return blockPos;
   }
}
