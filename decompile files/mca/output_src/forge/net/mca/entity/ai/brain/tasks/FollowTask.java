package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class FollowTask extends Behavior<VillagerEntityMCA> {
   public FollowTask() {
      super(ImmutableMap.of((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get(), MemoryStatus.VALUE_PRESENT));
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      return villager.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get()).isPresent();
   }

   protected boolean shouldKeepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void keepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      villager.m_6274_()
         .m_257414_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get())
         .ifPresent(
            playerToFollow -> {
               if (villager.getVillagerBrain().isPanicking()
                  && villager.m_6274_().m_257414_(MemoryModuleType.f_26382_).filter(livingEntity -> livingEntity == playerToFollow).isPresent()) {
                  villager.m_6274_().m_21936_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get());
               } else {
                  float dist = villager.m_20270_(playerToFollow) - 2.0F;
                  float speed = Math.min(1.0F, Math.max(0.6F, dist * 0.4F * 0.25F));
                  BehaviorUtils.m_22590_(villager, playerToFollow, (villager.m_20159_() ? 1.7F : 0.8F) * speed, 2);
               }
            }
         );
   }
}
