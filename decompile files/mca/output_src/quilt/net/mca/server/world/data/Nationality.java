package quilt.net.mca.server.world.data;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_18;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2497;
import net.minecraft.class_3218;
import net.minecraft.class_5819;
import quilt.net.mca.util.NbtHelper;
import quilt.net.mca.util.WorldUtils;

public class Nationality extends class_18 {
   private static final int CHUNK_SIZE = 128;
   private Map<Long, Integer> map = new HashMap<>();
   final class_5819 random = class_5819.method_43047();
   private static final int[][] neighbours = new int[][]{{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}};

   public static Nationality get(class_3218 world) {
      return WorldUtils.loadData(world.method_8503().method_30002(), Nationality::new, w -> new Nationality(), "mca_nationality");
   }

   Nationality() {
   }

   Nationality(class_2487 nbt) {
      this.map = NbtHelper.toMap(nbt, Long::valueOf, e -> ((class_2497)e).method_10701());
   }

   public class_2487 method_75(class_2487 nbt) {
      NbtHelper.fromMap(nbt, this.map, String::valueOf, class_2497::method_23247);
      return nbt;
   }

   private static long toId(long x, long z) {
      return x / 128L * 2147483647L + z / 128L;
   }

   public int getRegionId(class_2338 pos) {
      int id = -1;

      for (int[] neighbour : neighbours) {
         int x = pos.method_10263() + neighbour[0] * 128;
         int z = pos.method_10260() + neighbour[1] * 128;
         long rid = toId(x, z);
         if (this.map.containsKey(rid)) {
            id = this.map.get(rid);
            break;
         }
      }

      if (id == -1) {
         id = this.random.method_43054();
      }

      long rid = toId(pos.method_10263(), pos.method_10260());
      if (!this.map.containsKey(rid)) {
         this.map.put(rid, id);
         this.method_80();
      }

      return id;
   }
}
