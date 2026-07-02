package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StayTask extends Behavior<VillagerEntityMCA> {
   public StayTask() {
      super(ImmutableMap.of());
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      return villager.getMCABrain().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.STAYING.get()).isPresent() && !villager.getVillagerBrain().isPanicking();
   }

   protected boolean shouldKeepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      villager.m_21573_().m_26573_();
   }

   protected void keepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      villager.m_21573_().m_26573_();
      villager.m_6274_().m_21936_(MemoryModuleType.f_26370_);
   }
}
