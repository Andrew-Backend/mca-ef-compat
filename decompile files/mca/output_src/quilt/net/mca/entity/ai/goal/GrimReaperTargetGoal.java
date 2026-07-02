package quilt.net.mca.entity.ai.goal;

import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1314;
import net.minecraft.class_1352;
import net.minecraft.class_1657;
import net.minecraft.class_4051;

public class GrimReaperTargetGoal extends class_1352 {
   private final class_4051 attackTargeting = class_4051.method_36625().method_18418(64.0);
   private final class_1314 mob;
   private int nextScanTick = 20;

   public GrimReaperTargetGoal(class_1314 mob) {
      this.mob = mob;
   }

   public boolean method_6264() {
      if (this.nextScanTick > 0) {
         this.nextScanTick--;
      } else {
         this.nextScanTick = 20;
         List<class_1657> list = this.mob.method_37908().method_18464(this.attackTargeting, this.mob, this.mob.method_5829().method_1009(48.0, 64.0, 48.0));
         if (!list.isEmpty()) {
            list.sort(Comparator.comparing(class_1297::method_23318).reversed());

            for (class_1657 playerentity : list) {
               if (this.mob.method_18391(playerentity, class_4051.field_18092)) {
                  this.mob.method_5980(playerentity);
                  return true;
               }
            }
         }
      }

      return false;
   }

   public boolean method_6266() {
      return this.mob.method_5968() != null;
   }
}
