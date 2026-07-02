package forge.net.mca.entity.ai.brain.tasks;

import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;

public class LoseUnimportantJobTask {
   protected static boolean shouldRun(ServerLevel world, Villager entity) {
      return !((VillagerEntityMCA)entity).isProfessionImportant();
   }

   public static BehaviorControl<Villager> create() {
      return BehaviorBuilder.m_258034_(
         context -> context.group(context.m_258080_(MemoryModuleType.f_26360_))
            .apply(
               context,
               jobSite -> (world, entity, time) -> {
                  VillagerData villagerData = entity.m_7141_();
                  if (shouldRun(world, entity)
                     && villagerData.m_35571_() != VillagerProfession.f_35585_
                     && villagerData.m_35571_() != VillagerProfession.f_35596_
                     && entity.m_7809_() == 0
                     && villagerData.m_35576_() <= 1) {
                     entity.m_34375_(entity.m_7141_().m_35565_(VillagerProfession.f_35585_));
                     entity.m_35483_(world);
                     return true;
                  } else {
                     return false;
                  }
               }
            )
      );
   }
}
