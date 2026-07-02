package fabric.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.MemoryModuleTypeMCA;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;

public class StayTask extends class_4097<VillagerEntityMCA> {
   public StayTask() {
      super(ImmutableMap.of());
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      return villager.getMCABrain().method_46873((class_4140)MemoryModuleTypeMCA.STAYING.get()).isPresent() && !villager.getVillagerBrain().isPanicking();
   }

   protected boolean shouldKeepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      villager.method_5942().method_6340();
   }

   protected void keepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      villager.method_5942().method_6340();
      villager.method_18868().method_18875(class_4140.field_18445);
   }
}
