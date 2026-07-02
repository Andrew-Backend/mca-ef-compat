package forge.net.mca.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import forge.net.mca.Config;
import forge.net.mca.ProfessionsMCA;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.EquipmentSet;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.ActivityMCA;
import forge.net.mca.entity.ai.MemoryModuleTypeMCA;
import forge.net.mca.entity.ai.SchedulesMCA;
import forge.net.mca.entity.ai.brain.tasks.BowTask;
import forge.net.mca.entity.ai.brain.tasks.ConditionalSingleTickTask;
import forge.net.mca.entity.ai.brain.tasks.ConditionalTask;
import forge.net.mca.entity.ai.brain.tasks.DeliverMessageTask;
import forge.net.mca.entity.ai.brain.tasks.EnterBuildingTask;
import forge.net.mca.entity.ai.brain.tasks.EnterFavoredBuildingTask;
import forge.net.mca.entity.ai.brain.tasks.EquipmentTask;
import forge.net.mca.entity.ai.brain.tasks.ExtendedFindPointOfInterestTask;
import forge.net.mca.entity.ai.brain.tasks.ExtendedForgetCompletedPointOfInterestTask;
import forge.net.mca.entity.ai.brain.tasks.ExtendedMeleeAttackTask;
import forge.net.mca.entity.ai.brain.tasks.ExtendedWalkTowardsTask;
import forge.net.mca.entity.ai.brain.tasks.FollowTask;
import forge.net.mca.entity.ai.brain.tasks.GreetPlayerTask;
import forge.net.mca.entity.ai.brain.tasks.GrieveTask;
import forge.net.mca.entity.ai.brain.tasks.HoldItemTask;
import forge.net.mca.entity.ai.brain.tasks.InteractTask;
import forge.net.mca.entity.ai.brain.tasks.LambdaTask;
import forge.net.mca.entity.ai.brain.tasks.LazyFindPointOfInterestTask;
import forge.net.mca.entity.ai.brain.tasks.LoseUnimportantJobTask;
import forge.net.mca.entity.ai.brain.tasks.PatrolVillageTask;
import forge.net.mca.entity.ai.brain.tasks.SayTask;
import forge.net.mca.entity.ai.brain.tasks.SequenceTask;
import forge.net.mca.entity.ai.brain.tasks.SmarterOpenDoorsTask;
import forge.net.mca.entity.ai.brain.tasks.StayTask;
import forge.net.mca.entity.ai.brain.tasks.WanderOrTeleportToTargetTask;
import forge.net.mca.entity.ai.brain.tasks.chore.ChoppingTask;
import forge.net.mca.entity.ai.brain.tasks.chore.FishingTask;
import forge.net.mca.entity.ai.brain.tasks.chore.HarvestingTask;
import forge.net.mca.entity.ai.brain.tasks.chore.HuntingTask;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.server.world.data.VillageManager;
import forge.net.mca.server.world.data.villageComponents.VillageGuardsManager;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain.Provider;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.CelebrateVillagersSurvivedRaid;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.behavior.GoToClosestVillage;
import net.minecraft.world.entity.ai.behavior.GoToPotentialJobSite;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.behavior.InsideBrownianWalk;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.JumpOnBed;
import net.minecraft.world.entity.ai.behavior.LocateHidingPlace;
import net.minecraft.world.entity.ai.behavior.LookAndFollowTradingPlayerSink;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToSkySeeingSpot;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.PlayTagWithOtherKids;
import net.minecraft.world.entity.ai.behavior.PoiCompetitorScan;
import net.minecraft.world.entity.ai.behavior.ReactToBell;
import net.minecraft.world.entity.ai.behavior.ResetRaidStatus;
import net.minecraft.world.entity.ai.behavior.RingBell;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetHiddenState;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetRaidStatus;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.ShowTradesToPlayer;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.entity.ai.behavior.SocializeAtBell;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StrollAroundPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoi;
import net.minecraft.world.entity.ai.behavior.StrollToPoiList;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.TradeWithVillager;
import net.minecraft.world.entity.ai.behavior.UpdateActivityFromSchedule;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import net.minecraft.world.entity.ai.behavior.VillageBoundRandomStroll;
import net.minecraft.world.entity.ai.behavior.VillagerCalmDown;
import net.minecraft.world.entity.ai.behavior.VillagerPanicTrigger;
import net.minecraft.world.entity.ai.behavior.WakeUp;
import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.ai.behavior.YieldJobSite;
import net.minecraft.world.entity.ai.behavior.GateBehavior.OrderPolicy;
import net.minecraft.world.entity.ai.behavior.GateBehavior.RunningPolicy;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VillagerTasksMCA {
   public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
      MemoryModuleType.f_26359_,
      MemoryModuleType.f_26360_,
      MemoryModuleType.f_26361_,
      MemoryModuleType.f_26362_,
      MemoryModuleType.f_148204_,
      MemoryModuleType.f_148205_,
      MemoryModuleType.f_26366_,
      MemoryModuleType.f_26367_,
      MemoryModuleType.f_26368_,
      MemoryModuleType.f_148206_,
      MemoryModuleType.f_26332_,
      MemoryModuleType.f_26370_,
      new MemoryModuleType[]{
         MemoryModuleType.f_26371_,
         MemoryModuleType.f_26374_,
         MemoryModuleType.f_26375_,
         MemoryModuleType.f_26377_,
         MemoryModuleType.f_26379_,
         MemoryModuleType.f_26380_,
         MemoryModuleType.f_26381_,
         MemoryModuleType.f_26382_,
         MemoryModuleType.f_26323_,
         MemoryModuleType.f_26363_,
         MemoryModuleType.f_26324_,
         MemoryModuleType.f_26325_,
         MemoryModuleType.f_26326_,
         MemoryModuleType.f_26328_,
         MemoryModuleType.f_26329_,
         MemoryModuleType.f_26330_,
         MemoryModuleType.f_26327_,
         MemoryModuleType.f_26372_,
         MemoryModuleType.f_26373_,
         (MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get(),
         (MemoryModuleType)MemoryModuleTypeMCA.STAYING.get(),
         (MemoryModuleType)MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY.get(),
         (MemoryModuleType)MemoryModuleTypeMCA.WEARS_ARMOR.get(),
         (MemoryModuleType)MemoryModuleTypeMCA.SMALL_BOUNTY.get(),
         (MemoryModuleType)MemoryModuleTypeMCA.HIT_BY_PLAYER.get(),
         (MemoryModuleType)MemoryModuleTypeMCA.LAST_GRIEVE.get(),
         (MemoryModuleType)MemoryModuleTypeMCA.FORCED_HOME.get()
      }
   );
   public static final ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES = ImmutableList.of(
      SensorType.f_26812_,
      SensorType.f_26810_,
      SensorType.f_26813_,
      SensorType.f_26814_,
      SensorType.f_26815_,
      SensorType.f_26817_,
      SensorType.f_26818_,
      (SensorType)ActivityMCA.VILLAGER_BABIES.get(),
      (SensorType)ActivityMCA.EXPLODING_CREEPER.get(),
      (SensorType)ActivityMCA.GUARD_ENEMIES.get()
   );

   public static Provider<VillagerEntityMCA> createProfile() {
      return Brain.m_21923_(MEMORY_TYPES, SENSOR_TYPES);
   }

   public static Brain<VillagerEntityMCA> initializeTasks(VillagerEntityMCA villager, Brain<VillagerEntityMCA> brain) {
      VillagerProfession profession = villager.m_7141_().m_35571_();
      AgeState age = AgeState.byCurrentAge(villager.m_146764_());
      boolean noDefault = false;
      if (brain.m_257414_((MemoryModuleType)MemoryModuleTypeMCA.STAYING.get()).isPresent()) {
         brain.m_21900_(Activity.f_37978_, getStayingPackage());
         brain.m_21900_(Activity.f_37978_, getImportantCorePackage(0.5F));
         brain.m_21900_(Activity.f_37978_, getSelfDefencePackage());
         brain.m_21900_(Activity.f_37984_, getPanicPackage(0.5F));
         noDefault = true;
      } else if (brain.m_257414_((MemoryModuleType)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get()).isPresent()) {
         brain.m_21900_(Activity.f_37978_, getFollowingPackage());
         brain.m_21900_(Activity.f_37978_, getImportantCorePackage(0.5F));
         brain.m_21900_(Activity.f_37978_, getSelfDefencePackage());
         brain.m_21900_(Activity.f_37984_, getPanicPackage(0.5F));
         noDefault = true;
      } else if (profession == ProfessionsMCA.MERCENARY.get()) {
         brain.m_21912_(SchedulesMCA.GUESTS);
         brain.m_21900_(Activity.f_37978_, getImportantCorePackage(0.5F));
         brain.m_21900_(Activity.f_37979_, getMercenaryPackage(0.5F));
         brain.m_21900_(Activity.f_37978_, getGuardCorePackage(villager));
         brain.m_21900_(Activity.f_37984_, getPanicPackage(0.5F));
         brain.m_21903_(Activity.f_37982_, getRestPackage(0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.f_26372_, MemoryStatus.VALUE_ABSENT)));
         brain.m_21900_((Activity)ActivityMCA.CHORE.get(), getChorePackage());
         noDefault = true;
      } else if (!villager.requiresHome()) {
         brain.m_21912_(SchedulesMCA.GUESTS);
         brain.m_21900_(Activity.f_37978_, getImportantCorePackage(0.5F));
         brain.m_21900_(Activity.f_37979_, getAdventurerPackage(0.5F));
         brain.m_21900_(Activity.f_37978_, getSelfDefencePackage());
         brain.m_21900_(Activity.f_37984_, getPanicPackage(0.5F));
         brain.m_21903_(Activity.f_37982_, getRestPackage(0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.f_26372_, MemoryStatus.VALUE_ABSENT)));
         noDefault = true;
      } else {
         if (age == AgeState.BABY) {
            brain.m_21912_(Schedule.f_38014_);
            return brain;
         }

         if (age != AgeState.ADULT) {
            brain.m_21912_(Schedule.f_38014_);
            brain.m_21900_(Activity.f_37981_, getPlayPackage(1.0F));
            brain.m_21900_(Activity.f_37978_, getSelfDefencePackage());
         } else if (villager.isGuard()) {
            brain.m_21912_(SchedulesMCA.getTypeSchedule(villager, SchedulesMCA.GUARD, SchedulesMCA.GUARD_NIGHT));
            brain.m_21900_(Activity.f_37978_, getGuardCorePackage(villager));
            brain.m_21900_(Activity.f_37980_, getGuardWorkPackage());
            brain.m_21900_(Activity.f_37984_, getGuardPanicPackage(0.5F));
            brain.m_21900_(Activity.f_37985_, getGuardWorkPackage());
         } else {
            brain.m_21912_(SchedulesMCA.getTypeSchedule(villager));
            brain.m_21900_(Activity.f_37978_, getWorkingCorePackage(profession, 0.5F));
            brain.m_21903_(Activity.f_37980_, getWorkPackage(profession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.f_26360_, MemoryStatus.VALUE_PRESENT)));
            brain.m_21900_(Activity.f_37978_, getSelfDefencePackage());
            brain.m_21900_(Activity.f_37985_, getRaidPackage(0.5F));
         }
      }

      brain.m_21900_((Activity)ActivityMCA.GRIEVE.get(), getGrievingPackage());
      if (!noDefault) {
         brain.m_21900_(Activity.f_37978_, getImportantCorePackage(0.5F));
         brain.m_21900_(Activity.f_37978_, getCorePackage(0.5F));
         brain.m_21903_(Activity.f_37983_, getMeetPackage(0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.f_26362_, MemoryStatus.VALUE_PRESENT)));
         brain.m_21903_(Activity.f_37982_, getRestPackage(0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.f_26372_, MemoryStatus.VALUE_ABSENT)));
         brain.m_21900_(Activity.f_37979_, getIdlePackage(0.5F));
         brain.m_21900_(Activity.f_37984_, getPanicPackage(0.5F));
         brain.m_21900_(Activity.f_37986_, getPreRaidPackage(0.5F));
         brain.m_21900_(Activity.f_37987_, getHidePackage(0.5F));
         brain.m_21900_((Activity)ActivityMCA.CHORE.get(), getChorePackage());
      }

      brain.m_21930_(ImmutableSet.of(Activity.f_37978_));
      brain.m_21944_(Activity.f_37979_);
      brain.m_21889_(Activity.f_37979_);
      brain.m_21862_(villager.m_9236_().m_46468_(), villager.m_9236_().m_46467_());
      return brain;
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getStayingPackage() {
      return ImmutableList.of(Pair.of(0, new StayTask()), getFullLookBehavior());
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getFollowingPackage() {
      return ImmutableList.of(Pair.of(0, new FollowTask()), getMinimalLookBehavior());
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getImportantCorePackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, new Swim(0.8F)),
         Config.getInstance().useSmarterDoorAI ? Pair.of(0, new SmarterOpenDoorsTask()) : Pair.of(0, InteractWithDoor.m_257893_()),
         Pair.of(0, new LookAtTargetSink(45, 90)),
         Pair.of(0, WakeUp.m_257779_()),
         Pair.of(0, new DeliverMessageTask()),
         Pair.of(1, new WanderOrTeleportToTargetTask()),
         Pair.of(3, new InteractTask(speedModifier)),
         Pair.of(
            10,
            new ExtendedFindPointOfInterestTask(
               registryEntry -> registryEntry.m_203565_(PoiTypes.f_218060_),
               MemoryModuleType.f_26359_,
               false,
               Optional.of((byte)14),
               villager -> villager.getResidency().seekHome(),
               (entity, pos) -> {
                  VillageManager manager = VillageManager.get((ServerLevel)entity.m_9236_());
                  return entity.requiresHome()
                     ? manager.findNearestVillage(entity).filter(v -> !v.isPositionValidBed(pos)).isEmpty()
                     : manager.findNearestVillage(entity)
                        .filter(v -> v.getBuildingAt(pos).filter(b -> b.getBuildingType().name().equals("inn")).isPresent())
                        .isPresent();
               }
            )
         )
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getCorePackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, new GreetPlayerTask()),
         Pair.of(0, ReactToBell.m_258068_()),
         Pair.of(0, SetRaidStatus.m_257923_()),
         Pair.of(5, GoToWantedItem.m_257526_(speedModifier, false, 4)),
         Pair.of(
            10,
            new ExtendedFindPointOfInterestTask(
               registryEntry -> registryEntry.m_203565_(PoiTypes.f_218060_),
               MemoryModuleType.f_26359_,
               false,
               Optional.of((byte)14),
               villager -> villager.getResidency().seekHome(),
               (entity, pos) -> {
                  VillageManager manager = VillageManager.get((ServerLevel)entity.m_9236_());
                  return manager.findNearestVillage(entity).filter(v -> v.getBuildingAt(pos).filter(b -> b.getBuildingType().noBeds()).isPresent()).isEmpty();
               }
            )
         ),
         Pair.of(
            10,
            new ExtendedFindPointOfInterestTask(
               registryEntry -> registryEntry.m_203565_(PoiTypes.f_218061_),
               MemoryModuleType.f_26362_,
               true,
               Optional.of((byte)14),
               villager -> villager.m_6274_().m_257414_(MemoryModuleType.f_26362_).ifPresent(p -> {
                  if (villager.m_9236_().m_46472_() == p.m_122640_()) {
                     VillageManager manager = VillageManager.get((ServerLevel)villager.m_9236_());
                     if (!manager.cache.contains(p.m_122646_())) {
                        manager.cache.add(p.m_122646_());
                        manager.processBuilding(p.m_122646_());
                     }

                     villager.getResidency().seekHome();
                  }
               })
            )
         )
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getWorkingCorePackage(
      VillagerProfession profession, float speedModifier
   ) {
      return ImmutableList.of(
         Pair.of(0, ValidateNearbyPoi.m_257857_(profession.f_219628_(), MemoryModuleType.f_26360_)),
         Pair.of(0, ValidateNearbyPoi.m_257857_(profession.f_219629_(), MemoryModuleType.f_26361_)),
         Pair.of(2, PoiCompetitorScan.m_257502_()),
         Pair.of(3, new LookAndFollowTradingPlayerSink(speedModifier)),
         Pair.of(6, LazyFindPointOfInterestTask.create(profession.f_219629_(), MemoryModuleType.f_26360_, MemoryModuleType.f_26361_, true, Optional.empty())),
         Pair.of(7, new GoToPotentialJobSite(speedModifier)),
         Pair.of(8, YieldJobSite.m_257788_(speedModifier)),
         Pair.of(10, AssignProfessionFromJobSite.m_257634_()),
         Pair.of(10, LoseUnimportantJobTask.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getSelfDefencePackage() {
      return ImmutableList.of(
         Pair.of(0, new VillagerPanicTrigger()),
         Pair.of(1, new EquipmentTask(VillagerTasksMCA::isInDanger, v -> EquipmentSet.NAKED)),
         Pair.of(2, new ExtendedMeleeAttackTask(15, 2.5F, MemoryModuleType.f_26323_))
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getGuardCorePackage(VillagerEntityMCA villager) {
      return ImmutableList.of(
         Pair.of(0, new ConditionalTask(new VillagerPanicTrigger(), VillagerTasksMCA::guardTooHurt)),
         Pair.of(0, new SayTask("villager.retreat", 100, e -> guardTooHurt(e) && e.getVillagerBrain().isPanicking())),
         Pair.of(0, new SayTask("villager.attack", 160, e -> !guardTooHurt(e) && getPreferredTarget(e).isPresent())),
         Pair.of(0, new ConditionalTask(new ExtendedMeleeAttackTask(15, 2.5F, MemoryModuleType.f_26323_), VillagerTasksMCA::guardTooHurt)),
         Pair.of(
            1,
            new EquipmentTask(
               VillagerTasksMCA::isOnDuty,
               v -> v.getResidency()
                  .getHomeVillage()
                  .map(vil -> vil.getVillageGuardsManager().getGuardEquipment(v.getProfession(), v.getDominantHand()))
                  .orElseGet(
                     () -> v.getProfession() == ProfessionsMCA.ARCHER.get()
                        ? VillageGuardsManager.getEquipmentFor(v.getDominantHand(), EquipmentSet.ARCHER_0, EquipmentSet.ARCHER_0_LEFT)
                        : VillageGuardsManager.getEquipmentFor(v.getDominantHand(), EquipmentSet.GUARD_0, EquipmentSet.GUARD_0_LEFT)
                  )
            )
         ),
         Pair.of(2, StartAttacking.m_257741_(t -> true, VillagerTasksMCA::getPreferredTarget)),
         Pair.of(3, StopAttackingIfTargetInvalid.m_257990_(livingEntity -> !isPreferredTarget(villager, livingEntity))),
         Pair.of(4, new BowTask(20, 12)),
         Pair.of(5, BehaviorBuilder.m_257845_(v -> v.m_21055_(Items.f_42717_), BackUpIfTooClose.m_257698_(5, 0.75F))),
         Pair.of(6, SetWalkTargetFromAttackTargetIfTargetOutOfReach.m_257469_(0.75F)),
         Pair.of(7, new ExtendedMeleeAttackTask(20, 2.0F)),
         Pair.of(8, new CrossbowAttack()),
         new Pair[0]
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getGuardWorkPackage() {
      return ImmutableList.of(Pair.of(10, new PatrolVillageTask(4, 0.4F)), Pair.of(99, UpdateActivityFromSchedule.m_257835_()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getGuardPanicPackage(float speedModifier) {
      float f = speedModifier * 1.5F;
      return ImmutableList.of(
         Pair.of(1, VillagerCalmDown.m_257666_()),
         Pair.of(2, SetWalkTargetAwayFrom.m_257370_(MemoryModuleType.f_26323_, f, 6, false)),
         Pair.of(2, SetWalkTargetAwayFrom.m_257370_(MemoryModuleType.f_26382_, f, 6, false)),
         Pair.of(3, VillageBoundRandomStroll.m_258010_(f, 2, 2)),
         getMinimalLookBehavior()
      );
   }

   private static boolean guardTooHurt(VillagerEntityMCA villager) {
      return villager.m_21223_() < villager.m_21233_() * 0.25;
   }

   private static Optional<? extends LivingEntity> getPreferredTarget(VillagerEntityMCA villager) {
      if (guardTooHurt(villager)) {
         return Optional.empty();
      }

      Optional<LivingEntity> current = villager.m_6274_().m_257414_(MemoryModuleType.f_26372_);
      if (current.isPresent() && shouldKeepAttackTarget(villager, current.get())) {
         return current;
      }

      Optional<LivingEntity> primary = villager.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY.get());
      return primary.isPresent() && shouldRespondToGuardEnemy(villager, primary.get()) ? primary : Optional.empty();
   }

   private static boolean shouldKeepAttackTarget(VillagerEntityMCA villager, LivingEntity target) {
      return target.m_6084_()
         && !target.m_213877_()
         && target.m_9236_() == villager.m_9236_()
         && villager.m_6779_(target)
         && shouldRespondToGuardEnemy(villager, target);
   }

   private static boolean shouldRespondToGuardEnemy(VillagerEntityMCA villager, LivingEntity target) {
      return getActivity(villager) != Activity.f_37982_
         || target.m_20270_(villager) < 8.0
         || villager.getResidency().getHomeVillage().filter(village -> village.isWithinBorder(villager)).isEmpty();
   }

   private static boolean isPreferredTarget(VillagerEntityMCA villager, LivingEntity entity) {
      Optional<? extends LivingEntity> target = getPreferredTarget(villager);
      return target.filter(livingEntity -> livingEntity == entity).isPresent();
   }

   public static boolean isOnDuty(VillagerEntityMCA villager) {
      return getActivity(villager) == Activity.f_37980_
         || villager.m_6274_().m_257414_(MemoryModuleType.f_26372_).isPresent()
         || getPreferredTarget(villager).isPresent();
   }

   public static boolean isInDanger(VillagerEntityMCA villager) {
      return villager.getVillagerBrain().isPanicking() || villager.m_6274_().m_257414_(MemoryModuleType.f_26372_).isPresent();
   }

   private static Activity getActivity(VillagerEntityMCA villager) {
      return villager.m_6274_().m_21932_().m_38019_((int)(villager.m_9236_().m_46468_() % 24000L));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getGrievingPackage() {
      return ImmutableList.of(
         Pair.of(
            0,
            new SequenceTask(
               ImmutableMap.of(MemoryModuleType.f_26370_, MemoryStatus.VALUE_ABSENT),
               ImmutableList.of(
                  new EnterBuildingTask("graveyard", 0.5F),
                  new RunOne(
                     ImmutableList.of(
                        Pair.of(new HoldItemTask(InteractionHand.MAIN_HAND, Items.f_41946_), 1),
                        Pair.of(new HoldItemTask(InteractionHand.MAIN_HAND, Items.f_41944_), 1),
                        Pair.of(new HoldItemTask(InteractionHand.MAIN_HAND, Items.f_41945_), 1),
                        Pair.of(new HoldItemTask(InteractionHand.MAIN_HAND, Items.f_41947_), 1)
                     )
                  ),
                  new WanderOrTeleportToTargetTask(),
                  new DoNothing(100, 300),
                  new SayTask("villager.grieving"),
                  new DoNothing(100, 300),
                  new SayTask("villager.grieving"),
                  new DoNothing(100, 300),
                  new SayTask("villager.grieving"),
                  new HoldItemTask(InteractionHand.MAIN_HAND, ItemStack.f_41583_),
                  new LambdaTask<>(v -> {
                     v.getVillagerBrain().justGrieved();
                     v.m_6274_().m_21862_(v.m_9236_().m_46468_(), v.m_9236_().m_46467_());
                  })
               )
            )
         )
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getWorkPackage(
      VillagerProfession profession, float speedModifier
   ) {
      WorkAtPoi villagerWorkTask;
      if (profession == VillagerProfession.f_35590_) {
         villagerWorkTask = new WorkAtComposter();
      } else {
         villagerWorkTask = new WorkAtPoi();
      }

      return ImmutableList.of(
         getMinimalLookBehavior(),
         Pair.of(
            5,
            new RunOne(
               ImmutableList.of(
                  Pair.of(villagerWorkTask, 7),
                  Pair.of(StrollAroundPoi.m_257894_(MemoryModuleType.f_26360_, 0.4F, 4), 2),
                  Pair.of(StrollToPoi.m_258086_(MemoryModuleType.f_26360_, 0.4F, 1, 10), 5),
                  Pair.of(StrollToPoiList.m_257487_(MemoryModuleType.f_26363_, speedModifier, 1, 6, MemoryModuleType.f_26360_), 5),
                  Pair.of(new HarvestFarmland(), profession == VillagerProfession.f_35590_ ? 2 : 5),
                  Pair.of(new UseBonemeal(), profession == VillagerProfession.f_35590_ ? 4 : 7)
               )
            )
         ),
         Pair.of(10, new ShowTradesToPlayer(400, 1600)),
         Pair.of(10, SetLookAndInteract.m_257430_(EntityType.f_20532_, 4)),
         Pair.of(2, SetWalkTargetFromBlockMemory.m_257972_(MemoryModuleType.f_26360_, speedModifier, 9, 100, 1200)),
         Pair.of(3, new GiveGiftToHero(100)),
         Pair.of(99, UpdateActivityFromSchedule.m_257835_())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getPlayPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, new MoveToTargetSink(80, 120)),
         getFullLookBehavior(),
         Pair.of(5, PlayTagWithOtherKids.m_257585_()),
         Pair.of(
            5,
            new RunOne(
               ImmutableMap.of(MemoryModuleType.f_26366_, MemoryStatus.VALUE_ABSENT),
               ImmutableList.of(
                  Pair.of(InteractWith.m_258079_(EntityType.f_20492_, 8, MemoryModuleType.f_26374_, speedModifier, 2), 2),
                  Pair.of(InteractWith.m_258079_(EntityType.f_20553_, 8, MemoryModuleType.f_26374_, speedModifier, 2), 1),
                  Pair.of(VillageBoundRandomStroll.m_257910_(speedModifier), 1),
                  Pair.of(SetWalkTargetFromLookTarget.m_257764_(speedModifier, 2), 1),
                  Pair.of(new JumpOnBed(speedModifier), 2),
                  Pair.of(new DoNothing(20, 40), 2)
               )
            )
         ),
         Pair.of(99, UpdateActivityFromSchedule.m_257835_())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getRestPackage(float speed) {
      return ImmutableList.of(
         Pair.of(2, ExtendedWalkTowardsTask.create(MemoryModuleType.f_26359_, speed, 1, Config.getInstance().getVillagerPathfindingDistance(), 1200, v -> {
            Optional<Boolean> memory = v.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.FORCED_HOME.get());
            boolean forced = memory != null && memory.isPresent();
            if (forced) {
               v.sendChatToAllAround("villager.cant_find_bed");
            }

            return !forced;
         }, v -> v.getResidency().seekHome(), ExtendedWalkTowardsTask::findBedStandPosition)),
         Pair.of(
            3,
            new ConditionalSingleTickTask(
               ExtendedForgetCompletedPointOfInterestTask.create(
                  registryEntry -> registryEntry.m_203565_(PoiTypes.f_218060_), MemoryModuleType.f_26359_, entity -> {
                     if (entity instanceof VillagerEntityMCA villager) {
                        villager.getResidency().seekHome();
                     }
                  }
               ),
               v -> {
                  Optional<Boolean> memory = v.m_6274_().m_257414_((MemoryModuleType)MemoryModuleTypeMCA.FORCED_HOME.get());
                  return memory == null || memory.isEmpty();
               }
            )
         ),
         Pair.of(3, new SleepInBed()),
         Pair.of(
            5,
            new RunOne(
               ImmutableMap.of(MemoryModuleType.f_26359_, MemoryStatus.VALUE_ABSENT),
               ImmutableList.of(
                  Pair.of(SetClosestHomeAsWalkTarget.m_257524_(speed), 1),
                  Pair.of(InsideBrownianWalk.m_258053_(speed), 4),
                  Pair.of(GoToClosestVillage.m_257375_(speed, 4), 2),
                  Pair.of(new DoNothing(20, 40), 2)
               )
            )
         ),
         Pair.of(99, UpdateActivityFromSchedule.m_257835_())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getMeetPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(
            2,
            new RunOne(ImmutableList.of(Pair.of(StrollAroundPoi.m_257894_(MemoryModuleType.f_26362_, 0.4F, 40), 2), Pair.of(SocializeAtBell.m_257875_(), 2)))
         ),
         Pair.of(10, new ShowTradesToPlayer(400, 1600)),
         Pair.of(10, SetLookAndInteract.m_257430_(EntityType.f_20532_, 4)),
         Pair.of(2, SetWalkTargetFromBlockMemory.m_257972_(MemoryModuleType.f_26362_, speedModifier, 6, 100, 200)),
         Pair.of(3, new GiveGiftToHero(100)),
         Pair.of(3, ValidateNearbyPoi.m_257857_(registryEntry -> registryEntry.m_203565_(PoiTypes.f_218061_), MemoryModuleType.f_26362_)),
         Pair.of(
            3,
            new GateBehavior(
               ImmutableMap.of(),
               ImmutableSet.of(MemoryModuleType.f_26374_),
               OrderPolicy.ORDERED,
               RunningPolicy.RUN_ONE,
               ImmutableList.of(Pair.of(new TradeWithVillager(), 1))
            )
         ),
         getFullLookBehavior(),
         Pair.of(99, UpdateActivityFromSchedule.m_257835_())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getIdlePackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(1, new EnterFavoredBuildingTask(0.5F)),
         Pair.of(
            2,
            new RunOne(
               ImmutableList.of(
                  Pair.of(InteractWith.m_258079_((EntityType)EntitiesMCA.FEMALE_VILLAGER.get(), 8, MemoryModuleType.f_26374_, speedModifier, 2), 2),
                  Pair.of(InteractWith.m_258079_((EntityType)EntitiesMCA.MALE_VILLAGER.get(), 8, MemoryModuleType.f_26374_, speedModifier, 2), 2),
                  Pair.of(InteractWith.m_258079_(EntityType.f_20553_, 8, MemoryModuleType.f_26374_, speedModifier, 2), 1),
                  Pair.of(VillageBoundRandomStroll.m_257910_(speedModifier), 1),
                  Pair.of(SetWalkTargetFromLookTarget.m_257764_(speedModifier, 2), 1),
                  Pair.of(new JumpOnBed(speedModifier), 1),
                  Pair.of(new DoNothing(30, 60), 1)
               )
            )
         ),
         Pair.of(3, new GiveGiftToHero(100)),
         Pair.of(3, SetLookAndInteract.m_257430_(EntityType.f_20532_, 4)),
         Pair.of(3, new ShowTradesToPlayer(400, 1600)),
         Pair.of(3, new GrieveTask()),
         Pair.of(
            3,
            new GateBehavior(
               ImmutableMap.of(),
               ImmutableSet.of(MemoryModuleType.f_26374_),
               OrderPolicy.ORDERED,
               RunningPolicy.RUN_ONE,
               ImmutableList.of(Pair.of(new TradeWithVillager(), 1))
            )
         ),
         getFullLookBehavior(),
         Pair.of(99, UpdateActivityFromSchedule.m_257835_())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getPanicPackage(float speedModifier) {
      float f = speedModifier * 1.5F;
      return ImmutableList.of(
         Pair.of(0, VillagerCalmDown.m_257666_()),
         Pair.of(1, SetWalkTargetAwayFrom.m_257370_(MemoryModuleType.f_26323_, f, 6, false)),
         Pair.of(1, SetWalkTargetAwayFrom.m_257370_(MemoryModuleType.f_26382_, f, 6, false)),
         Pair.of(3, VillageBoundRandomStroll.m_258010_(f, 2, 2)),
         getMinimalLookBehavior()
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getPreRaidPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, RingBell.m_257471_()),
         Pair.of(
            0,
            new RunOne(
               ImmutableList.of(
                  Pair.of(SetWalkTargetFromBlockMemory.m_257972_(MemoryModuleType.f_26362_, speedModifier * 1.5F, 2, 150, 200), 6),
                  Pair.of(VillageBoundRandomStroll.m_257910_(speedModifier * 1.5F), 2)
               )
            )
         ),
         getMinimalLookBehavior(),
         Pair.of(99, ResetRaidStatus.m_257468_())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getRaidPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(
            0,
            new RunOne(
               ImmutableList.of(Pair.of(MoveToSkySeeingSpot.m_257507_(speedModifier), 5), Pair.of(VillageBoundRandomStroll.m_257910_(speedModifier * 1.1F), 2))
            )
         ),
         Pair.of(0, new CelebrateVillagersSurvivedRaid(600, 600)),
         Pair.of(2, LocateHidingPlace.m_258090_(24, speedModifier * 1.4F, 1)),
         getMinimalLookBehavior(),
         Pair.of(99, ResetRaidStatus.m_257468_())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getHidePackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, SetHiddenState.m_257713_(15, 3)), Pair.of(1, LocateHidingPlace.m_258090_(32, speedModifier * 1.25F, 2)), getMinimalLookBehavior()
      );
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getChorePackage() {
      return ImmutableList.of(Pair.of(0, new ChoppingTask()), Pair.of(0, new FishingTask()), Pair.of(0, new HarvestingTask()), Pair.of(0, new HuntingTask()));
   }

   private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getAdventurerPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(5, InteractWith.m_258079_((EntityType)EntitiesMCA.FEMALE_VILLAGER.get(), 8, MemoryModuleType.f_26374_, speedModifier, 2)),
         Pair.of(5, InteractWith.m_258079_((EntityType)EntitiesMCA.MALE_VILLAGER.get(), 8, MemoryModuleType.f_26374_, speedModifier, 2)),
         Pair.of(5, InteractWith.m_258079_(EntityType.f_20553_, 8, MemoryModuleType.f_26374_, speedModifier, 2)),
         Pair.of(5, VillageBoundRandomStroll.m_257910_(speedModifier)),
         Pair.of(5, SetWalkTargetFromLookTarget.m_257764_(speedModifier, 2)),
         Pair.of(5, new EnterBuildingTask("inn", 0.5F))
      );
   }

   private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super VillagerEntityMCA>>> getMercenaryPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(5, VillageBoundRandomStroll.m_257910_(speedModifier)), Pair.of(5, SetWalkTargetFromLookTarget.m_257764_(speedModifier, 2))
      );
   }

   private static Pair<Integer, BehaviorControl<LivingEntity>> getFullLookBehavior() {
      return Pair.of(
         5,
         new RunOne(
            ImmutableList.of(
               Pair.of(SetEntityLookTarget.m_258096_(EntityType.f_20553_, 8.0F), 8),
               Pair.of(SetEntityLookTarget.m_258096_(EntityType.f_20492_, 8.0F), 2),
               Pair.of(SetEntityLookTarget.m_258096_(EntityType.f_20532_, 8.0F), 2),
               Pair.of(SetEntityLookTarget.m_257381_(MobCategory.CREATURE, 8.0F), 1),
               Pair.of(SetEntityLookTarget.m_257381_(MobCategory.WATER_CREATURE, 8.0F), 1),
               Pair.of(SetEntityLookTarget.m_257381_(MobCategory.WATER_AMBIENT, 8.0F), 1),
               Pair.of(SetEntityLookTarget.m_257381_(MobCategory.MONSTER, 8.0F), 1),
               Pair.of(new DoNothing(30, 60), 2)
            )
         )
      );
   }

   private static Pair<Integer, BehaviorControl<LivingEntity>> getMinimalLookBehavior() {
      return Pair.of(
         5,
         new RunOne(
            ImmutableList.of(
               Pair.of(SetEntityLookTarget.m_258096_(EntityType.f_20492_, 8.0F), 2),
               Pair.of(SetEntityLookTarget.m_258096_(EntityType.f_20532_, 8.0F), 2),
               Pair.of(new DoNothing(30, 60), 8)
            )
         )
      );
   }
}
