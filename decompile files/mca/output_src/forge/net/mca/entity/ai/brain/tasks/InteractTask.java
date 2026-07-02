package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Chore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractTask extends Behavior<VillagerEntityMCA> {
   private final float speedModifier;

   public InteractTask(float speedModifier) {
      super(ImmutableMap.of(MemoryModuleType.f_26370_, MemoryStatus.REGISTERED, MemoryModuleType.f_26371_, MemoryStatus.REGISTERED), Integer.MAX_VALUE);
      this.speedModifier = speedModifier;
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      return shouldRun(villager);
   }

   public static boolean shouldRun(VillagerEntityMCA villager) {
      return villager.m_6084_()
         && villager.m_6274_().m_257414_(MemoryModuleType.f_26372_).isEmpty()
         && villager.getInteractions().getInteractingPlayer().filter(player -> villager.m_20280_(player) <= 25.0).isPresent()
         && !villager.m_20069_()
         && !villager.f_19864_
         && villager.getVillagerBrain().getCurrentJob() == Chore.NONE;
   }

   protected boolean shouldKeepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      this.followPlayer(villager);
   }

   protected void finishRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      Brain<?> brain = villager.m_6274_();
      brain.m_21936_(MemoryModuleType.f_26370_);
      brain.m_21936_(MemoryModuleType.f_26371_);
   }

   protected void keepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      this.followPlayer(villager);
   }

   protected boolean m_7773_(long time) {
      return false;
   }

   private void followPlayer(VillagerEntityMCA villager) {
      Brain<?> brain = villager.m_6274_();
      villager.getInteractions().getInteractingPlayer().ifPresentOrElse(player -> {
         brain.m_21879_(MemoryModuleType.f_26370_, new WalkTarget(player, this.speedModifier, 2));
         brain.m_21879_(MemoryModuleType.f_26371_, new EntityTracker(player, true));
      }, () -> {
         brain.m_21936_(MemoryModuleType.f_26370_);
         brain.m_21936_(MemoryModuleType.f_26371_);
      });
   }
}
