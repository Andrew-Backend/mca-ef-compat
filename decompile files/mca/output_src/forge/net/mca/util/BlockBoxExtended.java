package forge.net.mca.util;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BlockBoxExtended extends BoundingBox {
   public BlockBoxExtended(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      super(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public BoundingBox m_191961_(int margin) {
      return this.expand(margin, margin, margin);
   }

   public BoundingBox expand(int x, int y, int z) {
      return new BoundingBox(this.m_162395_() - x, this.m_162396_() - y, this.m_162398_() - z, this.m_162399_() + x, this.m_162400_() + y, this.m_162401_() + z);
   }

   public int getMaxBlockCount() {
      return Math.max(Math.max(this.m_71056_(), this.m_71057_()), this.m_71058_());
   }
}
