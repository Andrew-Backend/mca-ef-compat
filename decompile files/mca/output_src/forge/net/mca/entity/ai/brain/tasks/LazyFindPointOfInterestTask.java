package forge.net.mca.entity.ai.brain.tasks;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;

public class LazyFindPointOfInterestTask extends AcquirePoi {
   private static final int MIN_DELAY = 200;

   public static BehaviorControl<PathfinderMob> create(
      Predicate<Holder<PoiType>> poiPredicate,
      MemoryModuleType<GlobalPos> poiPosModule,
      MemoryModuleType<GlobalPos> potentialPoiPosModule,
      boolean onlyRunIfChild,
      Optional<Byte> entityStatus
   ) {
      MutableLong cooldown = new MutableLong(0L);
      Long2ObjectMap<LazyFindPointOfInterestTask.RetryMarker> long2ObjectMap = new Long2ObjectOpenHashMap();
      OneShot<PathfinderMob> singleTickTask = BehaviorBuilder.m_258034_(
         taskContext -> taskContext.group(taskContext.m_258080_(potentialPoiPosModule))
            .apply(
               taskContext,
               queryResult -> (world, entity, time) -> {
                  if (onlyRunIfChild && entity.m_6162_()) {
                     return false;
                  }

                  if (cooldown.getValue() == 0L) {
                     cooldown.setValue(world.m_46467_() + world.f_46441_.m_188503_(200));
                     return false;
                  }

                  if (world.m_46467_() < cooldown.getValue()) {
                     return false;
                  }

                  cooldown.setValue(time + 200L + world.m_213780_().m_188503_(200));
                  PoiManager pointOfInterestStorage = world.m_8904_();
                  long2ObjectMap.long2ObjectEntrySet().removeIf(entry -> !((LazyFindPointOfInterestTask.RetryMarker)entry.getValue()).isAttempting(time));
                  Predicate<BlockPos> predicate2 = pos -> {
                     LazyFindPointOfInterestTask.RetryMarker retryMarker = (LazyFindPointOfInterestTask.RetryMarker)long2ObjectMap.get(pos.m_121878_());
                     if (retryMarker == null) {
                        return true;
                     }

                     if (!retryMarker.shouldRetry(time)) {
                        return false;
                     }

                     retryMarker.setAttemptTime(time);
                     return true;
                  };
                  Set<Pair<Holder<PoiType>, BlockPos>> set = pointOfInterestStorage.m_217994_(
                        poiPredicate, predicate2, entity.m_20183_(), 48, Occupancy.HAS_SPACE
                     )
                     .limit(5L)
                     .collect(Collectors.toSet());
                  Path path = m_217097_(entity, set);
                  if (path != null && path.m_77403_()) {
                     BlockPos blockPos = path.m_77406_();
                     pointOfInterestStorage.m_27177_(blockPos).ifPresent(poiType -> {
                        pointOfInterestStorage.m_217946_(poiPredicate, (registryEntry, blockPos2) -> blockPos2.equals(blockPos), blockPos, 1);
                        queryResult.m_257512_(GlobalPos.m_122643_(world.m_46472_(), blockPos));
                        entityStatus.ifPresent(status -> world.m_7605_(entity, status));
                        long2ObjectMap.clear();
                        DebugPackets.m_133719_(world, blockPos);
                     });
                  } else {
                     for (Pair<Holder<PoiType>, BlockPos> registryEntryBlockPosPair : set) {
                        long2ObjectMap.computeIfAbsent(
                           ((BlockPos)registryEntryBlockPosPair.getSecond()).m_121878_(),
                           m -> new LazyFindPointOfInterestTask.RetryMarker(world.f_46441_, time)
                        );
                     }
                  }

                  return true;
               }
            )
      );
      return potentialPoiPosModule == poiPosModule
         ? singleTickTask
         : BehaviorBuilder.m_258034_(context -> context.group(context.m_258080_(poiPosModule)).apply(context, poiPos -> singleTickTask));
   }

   private static class RetryMarker {
      private static final int MIN_DELAY = 40;
      private static final int ATTEMPT_DURATION = 400;
      private final RandomSource random;
      private long previousAttemptAt;
      private long nextScheduledAttemptAt;
      private int currentDelay;

      RetryMarker(RandomSource random, long time) {
         this.random = random;
         this.setAttemptTime(time);
      }

      public void setAttemptTime(long time) {
         this.previousAttemptAt = time;
         int i = this.currentDelay + this.random.m_188503_(40) + 40;
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
