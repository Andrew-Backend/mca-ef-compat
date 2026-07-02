package fabric.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.MemoryModuleTypeMCA;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4215;

public class FollowTask extends class_4097<VillagerEntityMCA> {
   public FollowTask() {
      super(ImmutableMap.of((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get(), class_4141.field_18456));
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      return villager.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get()).isPresent();
   }

   protected boolean shouldKeepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void keepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      villager.method_18868()
         .method_46873((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get())
         .ifPresent(
            playerToFollow -> {
               if (villager.getVillagerBrain().isPanicking()
                  && villager.method_18868().method_46873(class_4140.field_18452).filter(livingEntity -> livingEntity == playerToFollow).isPresent()) {
                  villager.method_18868().method_18875((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get());
               } else {
                  float dist = villager.method_5739(playerToFollow) - 2.0F;
                  float speed = Math.min(1.0F, Math.max(0.6F, dist * 0.4F * 0.25F));
                  class_4215.method_24557(villager, playerToFollow, (villager.method_5765() ? 1.7F : 0.8F) * speed, 2);
               }
            }
         );
   }
}
