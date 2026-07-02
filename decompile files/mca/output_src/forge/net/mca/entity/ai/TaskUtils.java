package forge.net.mca.entity.ai;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface TaskUtils {
   static int getSpawnSafeTopLevel(Level world, int x, int y, int z) {
      MutableBlockPos pos = new MutableBlockPos(x, Math.min(y, world.m_151558_()), z);

      while (world.m_46859_(pos.m_122173_(Direction.DOWN)) && pos.m_123342_() > world.m_141937_()) {
      }

      return pos.m_123342_() + 1;
   }

   static List<BlockPos> getNearbyBlocks(BlockPos origin, Level world, @Nullable Predicate<BlockState> filter, int xzDist, int yDist) {
      return BlockPos.m_121985_(origin, xzDist, yDist, xzDist)
         .filter(pos -> !origin.equals(pos) && (filter == null || filter.test(world.m_8055_(pos))))
         .<BlockPos>map(BlockPos::m_7949_)
         .toList();
   }

   @Nullable
   static BlockPos getNearestPoint(BlockPos origin, List<BlockPos> blocks) {
      return blocks.stream().min(Comparator.comparing(origin::m_123331_)).orElse(null);
   }
}
