package forge.net.mca.util;

import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface VoxelShapeUtil {
   Vec3 CENTER = new Vec3(0.5, 0.0, 0.5);

   static Function<Direction, VoxelShape> rotator(VoxelShape base) {
      return d -> rotate(base, d);
   }

   static VoxelShape rotate(VoxelShape shape, Direction direction) {
      if (direction.m_122435_() == 0.0F) {
         return shape;
      }

      float angle = (float)(-direction.m_122435_() * Math.PI / 180.0);
      return Shapes.m_83124_(
         Shapes.m_83040_(),
         shape.m_83299_()
            .stream()
            .map(
               box -> {
                  Vec3 a = rotate(box.f_82288_, box.f_82290_, angle);
                  Vec3 b = rotate(box.f_82291_, box.f_82293_, angle);
                  Vec3 c = rotate(box.f_82288_, box.f_82293_, angle);
                  Vec3 d = rotate(box.f_82291_, box.f_82290_, angle);
                  return Shapes.m_83064_(
                     new AABB(
                        Math.min(Math.min(a.f_82479_, b.f_82479_), Math.min(c.f_82479_, d.f_82479_)),
                        box.f_82289_,
                        Math.min(Math.min(a.f_82481_, b.f_82481_), Math.min(c.f_82481_, d.f_82481_)),
                        Math.max(Math.max(a.f_82479_, b.f_82479_), Math.max(c.f_82479_, d.f_82479_)),
                        box.f_82292_,
                        Math.max(Math.max(a.f_82481_, b.f_82481_), Math.max(c.f_82481_, d.f_82481_))
                     )
                  );
               }
            )
            .toArray(VoxelShape[]::new)
      );
   }

   static Vec3 rotate(double x, double z, float angle) {
      return new Vec3(x, 0.0, z).m_82546_(CENTER).m_82524_(angle).m_82549_(CENTER);
   }
}
