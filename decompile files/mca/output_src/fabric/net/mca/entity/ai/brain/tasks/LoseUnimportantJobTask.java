package fabric.net.mca.entity.ai.brain.tasks;

import fabric.net.mca.entity.VillagerEntityMCA;
import net.minecraft.class_1646;
import net.minecraft.class_3218;
import net.minecraft.class_3850;
import net.minecraft.class_3852;
import net.minecraft.class_4140;
import net.minecraft.class_7893;
import net.minecraft.class_7898;

public class LoseUnimportantJobTask {
   protected static boolean shouldRun(class_3218 world, class_1646 entity) {
      return !((VillagerEntityMCA)entity).isProfessionImportant();
   }

   public static class_7893<class_1646> create() {
      return class_7898.method_47224(
         context -> context.group(context.method_47245(class_4140.field_18439))
            .apply(
               context,
               jobSite -> (world, entity, time) -> {
                  class_3850 villagerData = entity.method_7231();
                  if (shouldRun(world, entity)
                     && villagerData.method_16924() != class_3852.field_17051
                     && villagerData.method_16924() != class_3852.field_17062
                     && entity.method_19269() == 0
                     && villagerData.method_16925() <= 1) {
                     entity.method_7195(entity.method_7231().method_16921(class_3852.field_17051));
                     entity.method_19179(world);
                     return true;
                  } else {
                     return false;
                  }
               }
            )
      );
   }
}
