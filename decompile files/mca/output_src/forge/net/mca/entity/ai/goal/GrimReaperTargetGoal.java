package forge.net.mca.entity.ai.goal;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class GrimReaperTargetGoal extends Goal {
   private final TargetingConditions attackTargeting = TargetingConditions.m_148352_().m_26883_(64.0);
   private final PathfinderMob mob;
   private int nextScanTick = 20;

   public GrimReaperTargetGoal(PathfinderMob mob) {
      this.mob = mob;
   }

   public boolean m_8036_() {
      if (this.nextScanTick > 0) {
         this.nextScanTick--;
      } else {
         this.nextScanTick = 20;
         List<Player> list = this.mob.m_9236_().m_45955_(this.attackTargeting, this.mob, this.mob.m_20191_().m_82377_(48.0, 64.0, 48.0));
         if (!list.isEmpty()) {
            list.sort(Comparator.comparing(Entity::m_20186_).reversed());

            for (Player playerentity : list) {
               if (this.mob.m_21040_(playerentity, TargetingConditions.f_26872_)) {
                  this.mob.m_6710_(playerentity);
                  return true;
               }
            }
         }
      }

      return false;
   }

   public boolean m_8045_() {
      return this.mob.m_5448_() != null;
   }
}
