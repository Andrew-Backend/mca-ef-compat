package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import forge.net.mca.entity.VillagerEntityMCA;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

public class ExtendedFindPointOfInterestTask extends Behavior<VillagerEntityMCA> {
   private static final int MAX_POSITIONS_PER_RUN = 5;
   private static final int POSITION_EXPIRE_INTERVAL = 200;
   public static final int POI_SORTING_RADIUS = 48;
   private final Consumer<VillagerEntityMCA> onFinish;
   private final BiPredicate<VillagerEntityMCA, BlockPos> predicate;
   private final Predicate<Holder<PoiType>> poiType;
   private final MemoryModuleType<GlobalPos> targetMemoryModuleType;
   private final boolean onlyRunIfAdult;
   private final Optional<Byte> entityStatus;
   private long positionExpireTimeLimit;
   private final Long2ObjectMap<ExtendedFindPointOfInterestTask.RetryMarker> foundPositionsToExpiry = new Long2ObjectOpenHashMap();

   public ExtendedFindPointOfInterestTask(
      Predicate<Holder<PoiType>> poiType,
      MemoryModuleType<GlobalPos> moduleType,
      boolean onlyRunIfAdult,
      Optional<Byte> entityStatus,
      Consumer<VillagerEntityMCA> onFinish
   ) {
      this(poiType, moduleType, onlyRunIfAdult, entityStatus, onFinish, (e, p) -> true);
   }

   public ExtendedFindPointOfInterestTask(
      Predicate<Holder<PoiType>> poiType,
      MemoryModuleType<GlobalPos> moduleType,
      boolean onlyRunIfAdult,
      Optional<Byte> entityStatus,
      Consumer<VillagerEntityMCA> onFinish,
      BiPredicate<VillagerEntityMCA, BlockPos> predicate
   ) {
      super(create(moduleType));
      this.onFinish = onFinish;
      this.predicate = predicate;
      this.poiType = poiType;
      this.targetMemoryModuleType = moduleType;
      this.onlyRunIfAdult = onlyRunIfAdult;
      this.entityStatus = entityStatus;
   }

   private static ImmutableMap<MemoryModuleType<?>, MemoryStatus> create(MemoryModuleType<GlobalPos> firstModule) {
      Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
      builder.put(firstModule, MemoryStatus.VALUE_ABSENT);
      return builder.build();
   }

   protected boolean shouldRun(ServerLevel serverWorld, VillagerEntityMCA pathAwareEntity) {
      if (this.onlyRunIfAdult && pathAwareEntity.m_6162_()) {
         return false;
      } else if (this.positionExpireTimeLimit == 0L) {
         this.positionExpireTimeLimit = pathAwareEntity.m_9236_().m_46467_() + serverWorld.f_46441_.m_188503_(200);
         return false;
      } else {
         return serverWorld.m_46467_() >= this.positionExpireTimeLimit;
      }
   }

   protected void run(ServerLevel serverWorld, VillagerEntityMCA villager, long l) {
      this.positionExpireTimeLimit = l + 200L + serverWorld.m_213780_().m_188503_(200);
      PoiManager pointOfInterestStorage = serverWorld.m_8904_();
      this.foundPositionsToExpiry.long2ObjectEntrySet().removeIf(entry -> !((ExtendedFindPointOfInterestTask.RetryMarker)entry.getValue()).isAttempting(l));
      Predicate<BlockPos> predicate = blockPos -> {
         ExtendedFindPointOfInterestTask.RetryMarker retryMarker = (ExtendedFindPointOfInterestTask.RetryMarker)this.foundPositionsToExpiry
            .get(blockPos.m_121878_());
         if (retryMarker != null) {
            if (!retryMarker.shouldRetry(l)) {
               return false;
            }

            retryMarker.setAttemptTime(l);
         }

         return this.isBedOccupiedByOthers(serverWorld, blockPos, villager) ? false : this.predicate.test(villager, blockPos);
      };
      Set<Pair<Holder<PoiType>, BlockPos>> set = pointOfInterestStorage.m_217994_(this.poiType, predicate, villager.m_20183_(), 48, Occupancy.HAS_SPACE)
         .limit(5L)
         .collect(Collectors.toSet());
      Path path = AcquirePoi.m_217097_(villager, set);
      if (path != null && path.m_77403_()) {
         BlockPos blockPos2 = path.m_77406_();
         pointOfInterestStorage.m_27177_(blockPos2).ifPresent(pointOfInterestType -> {
            pointOfInterestStorage.m_217946_(this.poiType, (typeRegistryEntry, otherPos) -> otherPos.equals(blockPos2), blockPos2, 1);
            villager.m_6274_().m_21879_(this.targetMemoryModuleType, GlobalPos.m_122643_(serverWorld.m_46472_(), blockPos2));
            this.entityStatus.ifPresent(statusByte -> serverWorld.m_7605_(villager, statusByte));
            this.foundPositionsToExpiry.clear();
            DebugPackets.m_133719_(serverWorld, blockPos2);
            this.onFinish.accept(villager);
         });
      } else {
         for (Pair<Holder<PoiType>, BlockPos> blockPos2 : set) {
            this.foundPositionsToExpiry
               .computeIfAbsent(
                  ((BlockPos)blockPos2.getSecond()).m_121878_(), m -> new ExtendedFindPointOfInterestTask.RetryMarker(villager.m_9236_().f_46441_, l)
               );
         }
      }
   }

   private boolean isBedOccupiedByOthers(ServerLevel world, BlockPos pos, LivingEntity entity) {
      BlockState blockState = world.m_8055_(pos);
      return blockState.m_204336_(BlockTags.f_13038_) && (Boolean)blockState.m_61143_(BedBlock.f_49441_) && !entity.m_5803_();
   }

   static class RetryMarker {
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
   }
}
