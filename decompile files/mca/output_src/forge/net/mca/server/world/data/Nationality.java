package forge.net.mca.server.world.data;

import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

public class Nationality extends SavedData {
   private static final int CHUNK_SIZE = 128;
   private Map<Long, Integer> map = new HashMap<>();
   final RandomSource random = RandomSource.m_216327_();
   private static final int[][] neighbours = new int[][]{{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}};

   public static Nationality get(ServerLevel world) {
      return WorldUtils.loadData(world.m_7654_().m_129783_(), Nationality::new, w -> new Nationality(), "mca_nationality");
   }

   Nationality() {
   }

   Nationality(CompoundTag nbt) {
      this.map = NbtHelper.toMap(nbt, Long::valueOf, e -> ((IntTag)e).m_7047_());
   }

   public CompoundTag m_7176_(CompoundTag nbt) {
      NbtHelper.fromMap(nbt, this.map, String::valueOf, IntTag::m_128679_);
      return nbt;
   }

   private static long toId(long x, long z) {
      return x / 128L * 2147483647L + z / 128L;
   }

   public int getRegionId(BlockPos pos) {
      int id = -1;

      for (int[] neighbour : neighbours) {
         int x = pos.m_123341_() + neighbour[0] * 128;
         int z = pos.m_123343_() + neighbour[1] * 128;
         long rid = toId(x, z);
         if (this.map.containsKey(rid)) {
            id = this.map.get(rid);
            break;
         }
      }

      if (id == -1) {
         id = this.random.m_188502_();
      }

      long rid = toId(pos.m_123341_(), pos.m_123343_());
      if (!this.map.containsKey(rid)) {
         this.map.put(rid, id);
         this.m_77762_();
      }

      return id;
   }
}
