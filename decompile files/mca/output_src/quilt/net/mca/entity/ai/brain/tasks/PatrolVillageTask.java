package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4215;
import net.minecraft.class_5532;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.MemoryModuleTypeMCA;
import quilt.net.mca.util.BlockBoxExtended;

public class PatrolVillageTask extends class_4097<VillagerEntityMCA> {
   private final int completionRange;
   private final float speed;

   public PatrolVillageTask(int completionRange, float speed) {
      super(
         ImmutableMap.of(
            (class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get(),
            class_4141.field_18457,
            class_4140.field_18447,
            class_4141.field_18457,
            class_4140.field_22355,
            class_4141.field_18457,
            class_4140.field_18445,
            class_4141.field_18457,
            class_4140.field_18446,
            class_4141.field_18458
         )
      );
      this.completionRange = completionRange;
      this.speed = speed;
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA entity) {
      return !InteractTask.shouldRun(entity);
   }

   protected void run(class_3218 serverWorld, VillagerEntityMCA villager, long l) {
      this.getNextPosition(villager).ifPresent(pos -> class_4215.method_24561(villager, pos, this.speed, this.completionRange));
   }

   private Optional<class_2338> getNextPosition(VillagerEntityMCA villager) {
      return villager.getResidency().getHomeVillage().map(village -> {
         BlockBoxExtended box = village.getBox();
         int x = box.method_35415() + villager.method_6051().method_43048(box.method_35414());
         int z = box.method_35417() + villager.method_6051().method_43048(box.method_14663());
         class_243 targetPos = new class_243(x, box.method_22874().method_10264(), z);
         return class_5532.method_31512(villager, 32, 16, targetPos, Math.PI / 2);
      }).map(class_2338::method_49638);
   }
}
