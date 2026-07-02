package forge.net.mca.entity.ai.pathfinder;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;

public class VillagerNavigation extends GroundPathNavigation {
   public VillagerNavigation(Mob mobEntity, Level world) {
      super(mobEntity, world);
   }

   protected PathFinder m_5532_(int range) {
      this.f_26508_ = new VillagerLandPathNodeMaker();
      this.f_26508_.m_77351_(true);
      return new PathFinder(this.f_26508_, range);
   }
}
