package fabric.net.mca.entity.ai.brain.tasks.chore;

import fabric.net.mca.MCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4142;
import net.minecraft.class_5534;

public abstract class AbstractChoreTask extends class_4097<VillagerEntityMCA> {
   protected VillagerEntityMCA villager;
   protected int failedTicks;
   protected int walkingTicks;
   protected int lastAge;
   protected static final int FAILED_COOLDOWN = 100;
   protected static final int WALKING_THRESHOLD = 200;

   public AbstractChoreTask(Map<class_4140<?>, class_4141> requirements) {
      super(requirements, 400);
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA entity) {
      int diff = Math.max(0, entity.field_6012 - this.lastAge);
      this.lastAge = entity.field_6012;
      if (this.failedTicks > 0) {
         this.failedTicks -= diff;
         this.walkingTicks += diff;
         if (this.walkingTicks > 200) {
            Optional<class_243> optional = Optional.ofNullable(class_5534.method_31527(entity, 10, 5));
            entity.method_18868().method_18879(class_4140.field_18445, optional.map(vec3d -> new class_4142(vec3d, 0.4F, 0)));
            this.walkingTicks = 0;
         }

         return false;
      } else {
         return this.villager == null || !this.villager.getVillagerBrain().isPanicking();
      }
   }

   protected void keepRunning(class_3218 world, VillagerEntityMCA entity, long time) {
      if (this.getAssigningPlayer().isEmpty()) {
         MCA.LOGGER.info("Force-stopped chore because assigning player was not present.");
         this.villager.getVillagerBrain().abandonJob();
      }
   }

   protected void run(class_3218 world, VillagerEntityMCA entity, long time) {
      this.villager = entity;
   }

   Optional<class_1657> getAssigningPlayer() {
      return this.villager.getVillagerBrain().getJobAssigner();
   }

   void abandonJobWithMessage(String message) {
      this.getAssigningPlayer().ifPresent(player -> this.villager.sendChatMessage(player, message));
      this.villager.getVillagerBrain().abandonJob();
   }
}
