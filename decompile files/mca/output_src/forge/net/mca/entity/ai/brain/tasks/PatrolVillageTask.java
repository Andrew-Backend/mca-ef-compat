package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import forge.net.mca.util.BlockBoxExtended;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class PatrolVillageTask extends Behavior<VillagerEntityMCA> {
   private final int completionRange;
   private final float speed;

   public PatrolVillageTask(int completionRange, float speed) {
      super(
         ImmutableMap.of(
            (MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get(),
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.f_26374_,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.f_26372_,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.f_26370_,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.f_26371_,
            MemoryStatus.REGISTERED
         )
      );
      this.completionRange = completionRange;
      this.speed = speed;
   }

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA entity) {
      return !InteractTask.shouldRun(entity);
   }

   protected void run(ServerLevel serverWorld, VillagerEntityMCA villager, long l) {
      this.getNextPosition(villager).ifPresent(pos -> BehaviorUtils.m_22617_(villager, pos, this.speed, this.completionRange));
   }

   private Optional<BlockPos> getNextPosition(VillagerEntityMCA villager) {
      return villager.getResidency().getHomeVillage().map(village -> {
         BlockBoxExtended box = village.getBox();
         int x = box.m_162395_() + villager.m_217043_().m_188503_(box.m_71056_());
         int z = box.m_162398_() + villager.m_217043_().m_188503_(box.m_71058_());
         Vec3 targetPos = new Vec3(x, box.m_162394_().m_123342_(), z);
         return DefaultRandomPos.m_148412_(villager, 32, 16, targetPos, Math.PI / 2);
      }).map(BlockPos::m_274446_);
   }
}
