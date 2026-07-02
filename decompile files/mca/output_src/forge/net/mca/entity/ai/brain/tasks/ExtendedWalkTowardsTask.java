package forge.net.mca.entity.ai.brain.tasks;

import forge.net.mca.entity.VillagerEntityMCA;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

public class ExtendedWalkTowardsTask {
   public static OneShot<VillagerEntityMCA> create(
      MemoryModuleType<GlobalPos> destination,
      float speed,
      int completionRange,
      int maxDistance,
      int maxRunTime,
      Predicate<VillagerEntityMCA> canGiveUp,
      Consumer<VillagerEntityMCA> onGiveUp
   ) {
      return create(destination, speed, completionRange, maxDistance, maxRunTime, canGiveUp, onGiveUp, (world, entity, globalPos) -> Optional.empty());
   }

   public static OneShot<VillagerEntityMCA> create(
      MemoryModuleType<GlobalPos> destination,
      float speed,
      int completionRange,
      int maxDistance,
      int maxRunTime,
      Predicate<VillagerEntityMCA> canGiveUp,
      Consumer<VillagerEntityMCA> onGiveUp,
      ExtendedWalkTowardsTask.WalkTargetResolver walkTargetResolver
   ) {
      return BehaviorBuilder.m_258034_(
         context -> context.group(context.m_257492_(MemoryModuleType.f_26326_), context.m_258080_(MemoryModuleType.f_26370_), context.m_257495_(destination))
            .apply(context, (cantReachWalkTargetSince, walkTarget, destinationResult) -> (world, entity, time) -> {
               GlobalPos globalPos = (GlobalPos)context.m_258051_(destinationResult);
               Optional<Long> optional = context.m_257828_(cantReachWalkTargetSince);
               if (globalPos.m_122640_() == world.m_46472_() && (optional.isEmpty() || world.m_46467_() - optional.get() <= maxRunTime)) {
                  Optional<BlockPos> resolvedTarget = walkTargetResolver.resolve(world, entity, globalPos);
                  BlockPos targetPos = resolvedTarget.orElse(globalPos.m_122646_());
                  int targetCompletionRange = resolvedTarget.isPresent() ? 0 : completionRange;
                  if (targetPos.m_123333_(entity.m_20183_()) > maxDistance) {
                     Vec3 vec3d = null;
                     int l = 0;

                     while (vec3d == null || BlockPos.m_274446_(vec3d).m_123333_(entity.m_20183_()) > maxDistance) {
                        vec3d = DefaultRandomPos.m_148412_(entity, 15, 7, Vec3.m_82539_(targetPos), (float) (Math.PI / 2));
                        if (++l == 1000) {
                           entity.m_35428_(destination);
                           destinationResult.m_257971_();
                           cantReachWalkTargetSince.m_257512_(time);
                           return true;
                        }
                     }

                     walkTarget.m_257512_(new WalkTarget(vec3d, speed, completionRange));
                  } else if (targetPos.m_123333_(entity.m_20183_()) > targetCompletionRange) {
                     walkTarget.m_257512_(new WalkTarget(targetPos, speed, targetCompletionRange));
                  }
               } else if (canGiveUp.test(entity)) {
                  entity.m_35428_(destination);
                  destinationResult.m_257971_();
                  cantReachWalkTargetSince.m_257512_(time);
                  onGiveUp.accept(entity);
               } else {
                  cantReachWalkTargetSince.m_257512_(time);
               }

               return true;
            })
      );
   }

   public static Optional<BlockPos> findBedStandPosition(ServerLevel world, VillagerEntityMCA entity, GlobalPos destination) {
      if (entity.m_5803_()) {
         return Optional.empty();
      }

      BlockPos bedPos = destination.m_122646_();
      BlockState bedState = world.m_8055_(bedPos);
      if (!bedState.m_204336_(BlockTags.f_13038_)) {
         return Optional.empty();
      }

      Direction facing = bedState.m_61138_(BedBlock.f_54117_) ? (Direction)bedState.m_61143_(BedBlock.f_54117_) : Direction.NORTH;
      BlockPos footPos;
      BlockPos headPos;
      if (bedState.m_61138_(BedBlock.f_49440_)) {
         footPos = bedState.m_61143_(BedBlock.f_49440_) == BedPart.FOOT ? bedPos : bedPos.m_121945_(facing.m_122424_());
         headPos = bedState.m_61143_(BedBlock.f_49440_) == BedPart.HEAD ? bedPos : bedPos.m_121945_(facing);
      } else {
         footPos = bedPos;
         headPos = bedPos;
      }

      return Stream.of(
            footPos.m_121945_(facing.m_122427_()),
            footPos.m_121945_(facing.m_122428_()),
            headPos.m_121945_(facing.m_122427_()),
            headPos.m_121945_(facing.m_122428_()),
            footPos.m_121945_(facing.m_122424_()),
            headPos.m_121945_(facing)
         )
         .distinct()
         .filter(candidate -> bedPos.m_203195_(Vec3.m_82512_(candidate), 2.0))
         .filter(candidate -> entity.m_21573_().m_6342_(candidate))
         .filter(candidate -> world.m_45756_(entity, entity.m_20191_().m_82383_(Vec3.m_82539_(candidate).m_82546_(entity.m_20182_()))))
         .min(Comparator.comparingInt(candidate -> candidate.m_123333_(entity.m_20183_())));
   }

   @FunctionalInterface
   public interface WalkTargetResolver {
      Optional<BlockPos> resolve(ServerLevel var1, VillagerEntityMCA var2, GlobalPos var3);
   }
}
