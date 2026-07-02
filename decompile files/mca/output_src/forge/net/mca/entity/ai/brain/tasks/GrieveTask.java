package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.ActivityMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GrieveTask extends Behavior<VillagerEntityMCA> {
   public GrieveTask() {
      super(ImmutableMap.of());
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA entity) {
      return entity.getVillagerBrain().shouldGrieve() && entity.getResidency().getHomeVillage().filter(v -> v.hasBuilding("graveyard")).isPresent();
   }

   protected void run(ServerLevel serverWorld, VillagerEntityMCA villager, long l) {
      Brain<Villager> brain = villager.m_6274_();
      if (!brain.m_21954_((Activity)ActivityMCA.GRIEVE.get())) {
         brain.m_21936_(MemoryModuleType.f_26377_);
         brain.m_21936_(MemoryModuleType.f_26370_);
         brain.m_21936_(MemoryModuleType.f_26371_);
         brain.m_21936_(MemoryModuleType.f_26375_);
         brain.m_21936_(MemoryModuleType.f_26374_);
      }

      villager.getMCABrain().m_21889_((Activity)ActivityMCA.GRIEVE.get());
   }
}
