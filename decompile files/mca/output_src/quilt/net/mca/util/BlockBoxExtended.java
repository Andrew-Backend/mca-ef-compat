package quilt.net.mca.util;

import net.minecraft.class_3341;

public class BlockBoxExtended extends class_3341 {
   public BlockBoxExtended(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      super(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public class_3341 method_35410(int margin) {
      return this.expand(margin, margin, margin);
   }

   public class_3341 expand(int x, int y, int z) {
      return new class_3341(
         this.method_35415() - x, this.method_35416() - y, this.method_35417() - z, this.method_35418() + x, this.method_35419() + y, this.method_35420() + z
      );
   }

   public int getMaxBlockCount() {
      return Math.max(Math.max(this.method_35414(), this.method_14660()), this.method_14663());
   }
}
