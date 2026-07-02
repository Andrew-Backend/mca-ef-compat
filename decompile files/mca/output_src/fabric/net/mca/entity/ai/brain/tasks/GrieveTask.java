package fabric.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.ActivityMCA;
import net.minecraft.class_1646;
import net.minecraft.class_3218;
import net.minecraft.class_4095;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4168;

public class GrieveTask extends class_4097<VillagerEntityMCA> {
   public GrieveTask() {
      super(ImmutableMap.of());
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA entity) {
      return entity.getVillagerBrain().shouldGrieve() && entity.getResidency().getHomeVillage().filter(v -> v.hasBuilding("graveyard")).isPresent();
   }

   protected void run(class_3218 serverWorld, VillagerEntityMCA villager, long l) {
      class_4095<class_1646> brain = villager.method_18868();
      if (!brain.method_18906((class_4168)ActivityMCA.GRIEVE.get())) {
         brain.method_18875(class_4140.field_18449);
         brain.method_18875(class_4140.field_18445);
         brain.method_18875(class_4140.field_18446);
         brain.method_18875(class_4140.field_18448);
         brain.method_18875(class_4140.field_18447);
      }

      villager.getMCABrain().method_24526((class_4168)ActivityMCA.GRIEVE.get());
   }
}
