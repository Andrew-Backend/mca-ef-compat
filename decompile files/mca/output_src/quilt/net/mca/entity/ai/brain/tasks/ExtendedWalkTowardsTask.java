package quilt.net.mca.entity.ai.brain.tasks;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2742;
import net.minecraft.class_3218;
import net.minecraft.class_3481;
import net.minecraft.class_4140;
import net.minecraft.class_4142;
import net.minecraft.class_4208;
import net.minecraft.class_5532;
import net.minecraft.class_7894;
import net.minecraft.class_7898;
import quilt.net.mca.entity.VillagerEntityMCA;

public class ExtendedWalkTowardsTask {
   public static class_7894<VillagerEntityMCA> create(
      class_4140<class_4208> destination,
      float speed,
      int completionRange,
      int maxDistance,
      int maxRunTime,
      Predicate<VillagerEntityMCA> canGiveUp,
      Consumer<VillagerEntityMCA> onGiveUp
   ) {
      return create(destination, speed, completionRange, maxDistance, maxRunTime, canGiveUp, onGiveUp, (world, entity, globalPos) -> Optional.empty());
   }

   public static class_7894<VillagerEntityMCA> create(
      class_4140<class_4208> destination,
      float speed,
      int completionRange,
      int maxDistance,
      int maxRunTime,
      Predicate<VillagerEntityMCA> canGiveUp,
      Consumer<VillagerEntityMCA> onGiveUp,
      ExtendedWalkTowardsTask.WalkTargetResolver walkTargetResolver
   ) {
      return class_7898.method_47224(
         context -> context.group(context.method_47235(class_4140.field_19293), context.method_47245(class_4140.field_18445), context.method_47244(destination))
            .apply(context, (cantReachWalkTargetSince, walkTarget, destinationResult) -> (world, entity, time) -> {
               class_4208 globalPos = (class_4208)context.method_47243(destinationResult);
               Optional<Long> optional = context.method_47233(cantReachWalkTargetSince);
               if (globalPos.method_19442() == world.method_27983() && (optional.isEmpty() || world.method_8510() - optional.get() <= maxRunTime)) {
                  Optional<class_2338> resolvedTarget = walkTargetResolver.resolve(world, entity, globalPos);
                  class_2338 targetPos = resolvedTarget.orElse(globalPos.method_19446());
                  int targetCompletionRange = resolvedTarget.isPresent() ? 0 : completionRange;
                  if (targetPos.method_19455(entity.method_24515()) > maxDistance) {
                     class_243 vec3d = null;
                     int l = 0;

                     while (vec3d == null || class_2338.method_49638(vec3d).method_19455(entity.method_24515()) > maxDistance) {
                        vec3d = class_5532.method_31512(entity, 15, 7, class_243.method_24955(targetPos), (float) (Math.PI / 2));
                        if (++l == 1000) {
                           entity.method_19176(destination);
                           destinationResult.method_47252();
                           cantReachWalkTargetSince.method_47249(time);
                           return true;
                        }
                     }

                     walkTarget.method_47249(new class_4142(vec3d, speed, completionRange));
                  } else if (targetPos.method_19455(entity.method_24515()) > targetCompletionRange) {
                     walkTarget.method_47249(new class_4142(targetPos, speed, targetCompletionRange));
                  }
               } else if (canGiveUp.test(entity)) {
                  entity.method_19176(destination);
                  destinationResult.method_47252();
                  cantReachWalkTargetSince.method_47249(time);
                  onGiveUp.accept(entity);
               } else {
                  cantReachWalkTargetSince.method_47249(time);
               }

               return true;
            })
      );
   }

   public static Optional<class_2338> findBedStandPosition(class_3218 world, VillagerEntityMCA entity, class_4208 destination) {
      if (entity.method_6113()) {
         return Optional.empty();
      }

      class_2338 bedPos = destination.method_19446();
      class_2680 bedState = world.method_8320(bedPos);
      if (!bedState.method_26164(class_3481.field_16443)) {
         return Optional.empty();
      }

      class_2350 facing = bedState.method_28498(class_2244.field_11177) ? (class_2350)bedState.method_11654(class_2244.field_11177) : class_2350.field_11043;
      class_2338 footPos;
      class_2338 headPos;
      if (bedState.method_28498(class_2244.field_9967)) {
         footPos = bedState.method_11654(class_2244.field_9967) == class_2742.field_12557 ? bedPos : bedPos.method_10093(facing.method_10153());
         headPos = bedState.method_11654(class_2244.field_9967) == class_2742.field_12560 ? bedPos : bedPos.method_10093(facing);
      } else {
         footPos = bedPos;
         headPos = bedPos;
      }

      return Stream.of(
            footPos.method_10093(facing.method_10170()),
            footPos.method_10093(facing.method_10160()),
            headPos.method_10093(facing.method_10170()),
            headPos.method_10093(facing.method_10160()),
            footPos.method_10093(facing.method_10153()),
            headPos.method_10093(facing)
         )
         .distinct()
         .filter(candidate -> bedPos.method_19769(class_243.method_24953(candidate), 2.0))
         .filter(candidate -> entity.method_5942().method_6333(candidate))
         .filter(candidate -> world.method_8587(entity, entity.method_5829().method_997(class_243.method_24955(candidate).method_1020(entity.method_19538()))))
         .min(Comparator.comparingInt(candidate -> candidate.method_19455(entity.method_24515())));
   }

   @FunctionalInterface
   public interface WalkTargetResolver {
      Optional<class_2338> resolve(class_3218 var1, VillagerEntityMCA var2, class_4208 var3);
   }
}
