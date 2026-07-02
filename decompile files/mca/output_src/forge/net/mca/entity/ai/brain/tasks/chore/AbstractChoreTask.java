package forge.net.mca.entity.ai.brain.tasks.chore;

import forge.net.mca.MCA;
import forge.net.mca.entity.VillagerEntityMCA;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractChoreTask extends Behavior<VillagerEntityMCA> {
   protected VillagerEntityMCA villager;
   protected int failedTicks;
   protected int walkingTicks;
   protected int lastAge;
   protected static final int FAILED_COOLDOWN = 100;
   protected static final int WALKING_THRESHOLD = 200;

   public AbstractChoreTask(Map<MemoryModuleType<?>, MemoryStatus> requirements) {
      super(requirements, 400);
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA entity) {
      int diff = Math.max(0, entity.f_19797_ - this.lastAge);
      this.lastAge = entity.f_19797_;
      if (this.failedTicks > 0) {
         this.failedTicks -= diff;
         this.walkingTicks += diff;
         if (this.walkingTicks > 200) {
            Optional<Vec3> optional = Optional.ofNullable(LandRandomPos.m_148488_(entity, 10, 5));
            entity.m_6274_().m_21886_(MemoryModuleType.f_26370_, optional.map(vec3d -> new WalkTarget(vec3d, 0.4F, 0)));
            this.walkingTicks = 0;
         }

         return false;
      } else {
         return this.villager == null || !this.villager.getVillagerBrain().isPanicking();
      }
   }

   protected void keepRunning(ServerLevel world, VillagerEntityMCA entity, long time) {
      if (this.getAssigningPlayer().isEmpty()) {
         MCA.LOGGER.info("Force-stopped chore because assigning player was not present.");
         this.villager.getVillagerBrain().abandonJob();
      }
   }

   protected void run(ServerLevel world, VillagerEntityMCA entity, long time) {
      this.villager = entity;
   }

   Optional<Player> getAssigningPlayer() {
      return this.villager.getVillagerBrain().getJobAssigner();
   }

   void abandonJobWithMessage(String message) {
      this.getAssigningPlayer().ifPresent(player -> this.villager.sendChatMessage(player, message));
      this.villager.getVillagerBrain().abandonJob();
   }
}
