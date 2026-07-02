package fabric.net.mca.entity.ai.brain.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.class_11;
import net.minecraft.class_1314;
import net.minecraft.class_2338;
import net.minecraft.class_4096;
import net.minecraft.class_4140;
import net.minecraft.class_4153;
import net.minecraft.class_4158;
import net.minecraft.class_4208;
import net.minecraft.class_4209;
import net.minecraft.class_5819;
import net.minecraft.class_6880;
import net.minecraft.class_7893;
import net.minecraft.class_7894;
import net.minecraft.class_7898;
import net.minecraft.class_4153.class_4155;
import org.apache.commons.lang3.mutable.MutableLong;

public class LazyFindPointOfInterestTask extends class_4096 {
   private static final int MIN_DELAY = 200;

   public static class_7893<class_1314> create(
      Predicate<class_6880<class_4158>> poiPredicate,
      class_4140<class_4208> poiPosModule,
      class_4140<class_4208> potentialPoiPosModule,
      boolean onlyRunIfChild,
      Optional<Byte> entityStatus
   ) {
      MutableLong cooldown = new MutableLong(0L);
      Long2ObjectMap<LazyFindPointOfInterestTask.RetryMarker> long2ObjectMap = new Long2ObjectOpenHashMap();
      class_7894<class_1314> singleTickTask = class_7898.method_47224(
         taskContext -> taskContext.group(taskContext.method_47245(potentialPoiPosModule))
            .apply(
               taskContext,
               queryResult -> (world, entity, time) -> {
                  if (onlyRunIfChild && entity.method_6109()) {
                     return false;
                  }

                  if (cooldown.getValue() == 0L) {
                     cooldown.setValue(world.method_8510() + world.field_9229.method_43048(200));
                     return false;
                  }

                  if (world.method_8510() < cooldown.getValue()) {
                     return false;
                  }

                  cooldown.setValue(time + 200L + world.method_8409().method_43048(200));
                  class_4153 pointOfInterestStorage = world.method_19494();
                  long2ObjectMap.long2ObjectEntrySet().removeIf(entry -> !((LazyFindPointOfInterestTask.RetryMarker)entry.getValue()).isAttempting(time));
                  Predicate<class_2338> predicate2 = pos -> {
                     LazyFindPointOfInterestTask.RetryMarker retryMarker = (LazyFindPointOfInterestTask.RetryMarker)long2ObjectMap.get(pos.method_10063());
                     if (retryMarker == null) {
                        return true;
                     }

                     if (!retryMarker.shouldRetry(time)) {
                        return false;
                     }

                     retryMarker.setAttemptTime(time);
                     return true;
                  };
                  Set<Pair<class_6880<class_4158>, class_2338>> set = pointOfInterestStorage.method_30957(
                        poiPredicate, predicate2, entity.method_24515(), 48, class_4155.field_18487
                     )
                     .limit(5L)
                     .collect(Collectors.toSet());
                  class_11 path = method_43965(entity, set);
                  if (path != null && path.method_21655()) {
                     class_2338 blockPos = path.method_48();
                     pointOfInterestStorage.method_19132(blockPos).ifPresent(poiType -> {
                        pointOfInterestStorage.method_19126(poiPredicate, (registryEntry, blockPos2) -> blockPos2.equals(blockPos), blockPos, 1);
                        queryResult.method_47249(class_4208.method_19443(world.method_27983(), blockPos));
                        entityStatus.ifPresent(status -> world.method_8421(entity, status));
                        long2ObjectMap.clear();
                        class_4209.method_19778(world, blockPos);
                     });
                  } else {
                     for (Pair<class_6880<class_4158>, class_2338> registryEntryBlockPosPair : set) {
                        long2ObjectMap.computeIfAbsent(
                           ((class_2338)registryEntryBlockPosPair.getSecond()).method_10063(),
                           m -> new LazyFindPointOfInterestTask.RetryMarker(world.field_9229, time)
                        );
                     }
                  }

                  return true;
               }
            )
      );
      return potentialPoiPosModule == poiPosModule
         ? singleTickTask
         : class_7898.method_47224(context -> context.group(context.method_47245(poiPosModule)).apply(context, poiPos -> singleTickTask));
   }

   private static class RetryMarker {
      private static final int MIN_DELAY = 40;
      private static final int ATTEMPT_DURATION = 400;
      private final class_5819 random;
      private long previousAttemptAt;
      private long nextScheduledAttemptAt;
      private int currentDelay;

      RetryMarker(class_5819 random, long time) {
         this.random = random;
         this.setAttemptTime(time);
      }

      public void setAttemptTime(long time) {
         this.previousAttemptAt = time;
         int i = this.currentDelay + this.random.method_43048(40) + 40;
         this.currentDelay = Math.min(i, 400);
         this.nextScheduledAttemptAt = time + this.currentDelay;
      }

      public boolean isAttempting(long time) {
         return time - this.previousAttemptAt < 400L;
      }

      public boolean shouldRetry(long time) {
         return time >= this.nextScheduledAttemptAt;
      }

      @Override
      public String toString() {
         return "RetryMarker{, previousAttemptAt="
            + this.previousAttemptAt
            + ", nextScheduledAttemptAt="
            + this.nextScheduledAttemptAt
            + ", currentDelay="
            + this.currentDelay
            + "}";
      }
   }
}
