package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.class_11;
import net.minecraft.class_1309;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3481;
import net.minecraft.class_4096;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4153;
import net.minecraft.class_4158;
import net.minecraft.class_4208;
import net.minecraft.class_4209;
import net.minecraft.class_5819;
import net.minecraft.class_6880;
import net.minecraft.class_4153.class_4155;
import quilt.net.mca.entity.VillagerEntityMCA;

public class ExtendedFindPointOfInterestTask extends class_4097<VillagerEntityMCA> {
   private static final int MAX_POSITIONS_PER_RUN = 5;
   private static final int POSITION_EXPIRE_INTERVAL = 200;
   public static final int POI_SORTING_RADIUS = 48;
   private final Consumer<VillagerEntityMCA> onFinish;
   private final BiPredicate<VillagerEntityMCA, class_2338> predicate;
   private final Predicate<class_6880<class_4158>> poiType;
   private final class_4140<class_4208> targetMemoryModuleType;
   private final boolean onlyRunIfAdult;
   private final Optional<Byte> entityStatus;
   private long positionExpireTimeLimit;
   private final Long2ObjectMap<ExtendedFindPointOfInterestTask.RetryMarker> foundPositionsToExpiry = new Long2ObjectOpenHashMap();

   public ExtendedFindPointOfInterestTask(
      Predicate<class_6880<class_4158>> poiType,
      class_4140<class_4208> moduleType,
      boolean onlyRunIfAdult,
      Optional<Byte> entityStatus,
      Consumer<VillagerEntityMCA> onFinish
   ) {
      this(poiType, moduleType, onlyRunIfAdult, entityStatus, onFinish, (e, p) -> true);
   }

   public ExtendedFindPointOfInterestTask(
      Predicate<class_6880<class_4158>> poiType,
      class_4140<class_4208> moduleType,
      boolean onlyRunIfAdult,
      Optional<Byte> entityStatus,
      Consumer<VillagerEntityMCA> onFinish,
      BiPredicate<VillagerEntityMCA, class_2338> predicate
   ) {
      super(create(moduleType));
      this.onFinish = onFinish;
      this.predicate = predicate;
      this.poiType = poiType;
      this.targetMemoryModuleType = moduleType;
      this.onlyRunIfAdult = onlyRunIfAdult;
      this.entityStatus = entityStatus;
   }

   private static ImmutableMap<class_4140<?>, class_4141> create(class_4140<class_4208> firstModule) {
      Builder<class_4140<?>, class_4141> builder = ImmutableMap.builder();
      builder.put(firstModule, class_4141.field_18457);
      return builder.build();
   }

   protected boolean shouldRun(class_3218 serverWorld, VillagerEntityMCA pathAwareEntity) {
      if (this.onlyRunIfAdult && pathAwareEntity.method_6109()) {
         return false;
      } else if (this.positionExpireTimeLimit == 0L) {
         this.positionExpireTimeLimit = pathAwareEntity.method_37908().method_8510() + serverWorld.field_9229.method_43048(200);
         return false;
      } else {
         return serverWorld.method_8510() >= this.positionExpireTimeLimit;
      }
   }

   protected void run(class_3218 serverWorld, VillagerEntityMCA villager, long l) {
      this.positionExpireTimeLimit = l + 200L + serverWorld.method_8409().method_43048(200);
      class_4153 pointOfInterestStorage = serverWorld.method_19494();
      this.foundPositionsToExpiry.long2ObjectEntrySet().removeIf(entry -> !((ExtendedFindPointOfInterestTask.RetryMarker)entry.getValue()).isAttempting(l));
      Predicate<class_2338> predicate = blockPos -> {
         ExtendedFindPointOfInterestTask.RetryMarker retryMarker = (ExtendedFindPointOfInterestTask.RetryMarker)this.foundPositionsToExpiry
            .get(blockPos.method_10063());
         if (retryMarker != null) {
            if (!retryMarker.shouldRetry(l)) {
               return false;
            }

            retryMarker.setAttemptTime(l);
         }

         return this.isBedOccupiedByOthers(serverWorld, blockPos, villager) ? false : this.predicate.test(villager, blockPos);
      };
      Set<Pair<class_6880<class_4158>, class_2338>> set = pointOfInterestStorage.method_30957(
            this.poiType, predicate, villager.method_24515(), 48, class_4155.field_18487
         )
         .limit(5L)
         .collect(Collectors.toSet());
      class_11 path = class_4096.method_43965(villager, set);
      if (path != null && path.method_21655()) {
         class_2338 blockPos2 = path.method_48();
         pointOfInterestStorage.method_19132(blockPos2).ifPresent(pointOfInterestType -> {
            pointOfInterestStorage.method_19126(this.poiType, (typeRegistryEntry, otherPos) -> otherPos.equals(blockPos2), blockPos2, 1);
            villager.method_18868().method_18878(this.targetMemoryModuleType, class_4208.method_19443(serverWorld.method_27983(), blockPos2));
            this.entityStatus.ifPresent(statusByte -> serverWorld.method_8421(villager, statusByte));
            this.foundPositionsToExpiry.clear();
            class_4209.method_19778(serverWorld, blockPos2);
            this.onFinish.accept(villager);
         });
      } else {
         for (Pair<class_6880<class_4158>, class_2338> blockPos2 : set) {
            this.foundPositionsToExpiry
               .computeIfAbsent(
                  ((class_2338)blockPos2.getSecond()).method_10063(),
                  m -> new ExtendedFindPointOfInterestTask.RetryMarker(villager.method_37908().field_9229, l)
               );
         }
      }
   }

   private boolean isBedOccupiedByOthers(class_3218 world, class_2338 pos, class_1309 entity) {
      class_2680 blockState = world.method_8320(pos);
      return blockState.method_26164(class_3481.field_16443) && (Boolean)blockState.method_11654(class_2244.field_9968) && !entity.method_6113();
   }

   static class RetryMarker {
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
   }
}
