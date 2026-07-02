package fabric.net.mca.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import fabric.net.mca.Config;
import fabric.net.mca.ProfessionsMCA;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.entity.EquipmentSet;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.ActivityMCA;
import fabric.net.mca.entity.ai.MemoryModuleTypeMCA;
import fabric.net.mca.entity.ai.SchedulesMCA;
import fabric.net.mca.entity.ai.brain.tasks.BowTask;
import fabric.net.mca.entity.ai.brain.tasks.ConditionalSingleTickTask;
import fabric.net.mca.entity.ai.brain.tasks.ConditionalTask;
import fabric.net.mca.entity.ai.brain.tasks.DeliverMessageTask;
import fabric.net.mca.entity.ai.brain.tasks.EnterBuildingTask;
import fabric.net.mca.entity.ai.brain.tasks.EnterFavoredBuildingTask;
import fabric.net.mca.entity.ai.brain.tasks.EquipmentTask;
import fabric.net.mca.entity.ai.brain.tasks.ExtendedFindPointOfInterestTask;
import fabric.net.mca.entity.ai.brain.tasks.ExtendedForgetCompletedPointOfInterestTask;
import fabric.net.mca.entity.ai.brain.tasks.ExtendedMeleeAttackTask;
import fabric.net.mca.entity.ai.brain.tasks.ExtendedWalkTowardsTask;
import fabric.net.mca.entity.ai.brain.tasks.FollowTask;
import fabric.net.mca.entity.ai.brain.tasks.GreetPlayerTask;
import fabric.net.mca.entity.ai.brain.tasks.GrieveTask;
import fabric.net.mca.entity.ai.brain.tasks.HoldItemTask;
import fabric.net.mca.entity.ai.brain.tasks.InteractTask;
import fabric.net.mca.entity.ai.brain.tasks.LambdaTask;
import fabric.net.mca.entity.ai.brain.tasks.LazyFindPointOfInterestTask;
import fabric.net.mca.entity.ai.brain.tasks.LoseUnimportantJobTask;
import fabric.net.mca.entity.ai.brain.tasks.PatrolVillageTask;
import fabric.net.mca.entity.ai.brain.tasks.SayTask;
import fabric.net.mca.entity.ai.brain.tasks.SequenceTask;
import fabric.net.mca.entity.ai.brain.tasks.SmarterOpenDoorsTask;
import fabric.net.mca.entity.ai.brain.tasks.StayTask;
import fabric.net.mca.entity.ai.brain.tasks.WanderOrTeleportToTargetTask;
import fabric.net.mca.entity.ai.brain.tasks.chore.ChoppingTask;
import fabric.net.mca.entity.ai.brain.tasks.chore.FishingTask;
import fabric.net.mca.entity.ai.brain.tasks.chore.HarvestingTask;
import fabric.net.mca.entity.ai.brain.tasks.chore.HuntingTask;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.server.world.data.VillageManager;
import fabric.net.mca.server.world.data.villageComponents.VillageGuardsManager;
import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1311;
import net.minecraft.class_1646;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_3218;
import net.minecraft.class_3852;
import net.minecraft.class_4095;
import net.minecraft.class_4100;
import net.minecraft.class_4101;
import net.minecraft.class_4103;
import net.minecraft.class_4106;
import net.minecraft.class_4107;
import net.minecraft.class_4108;
import net.minecraft.class_4109;
import net.minecraft.class_4110;
import net.minecraft.class_4112;
import net.minecraft.class_4113;
import net.minecraft.class_4114;
import net.minecraft.class_4116;
import net.minecraft.class_4117;
import net.minecraft.class_4118;
import net.minecraft.class_4119;
import net.minecraft.class_4120;
import net.minecraft.class_4121;
import net.minecraft.class_4122;
import net.minecraft.class_4123;
import net.minecraft.class_4124;
import net.minecraft.class_4125;
import net.minecraft.class_4126;
import net.minecraft.class_4127;
import net.minecraft.class_4128;
import net.minecraft.class_4130;
import net.minecraft.class_4133;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4148;
import net.minecraft.class_4149;
import net.minecraft.class_4168;
import net.minecraft.class_4170;
import net.minecraft.class_4214;
import net.minecraft.class_4217;
import net.minecraft.class_4218;
import net.minecraft.class_4219;
import net.minecraft.class_4220;
import net.minecraft.class_4242;
import net.minecraft.class_4243;
import net.minecraft.class_4245;
import net.minecraft.class_4246;
import net.minecraft.class_4248;
import net.minecraft.class_4249;
import net.minecraft.class_4250;
import net.minecraft.class_4251;
import net.minecraft.class_4252;
import net.minecraft.class_4253;
import net.minecraft.class_4289;
import net.minecraft.class_4290;
import net.minecraft.class_4458;
import net.minecraft.class_4807;
import net.minecraft.class_4810;
import net.minecraft.class_4815;
import net.minecraft.class_4822;
import net.minecraft.class_4824;
import net.minecraft.class_4828;
import net.minecraft.class_4982;
import net.minecraft.class_4983;
import net.minecraft.class_5325;
import net.minecraft.class_5326;
import net.minecraft.class_5327;
import net.minecraft.class_7477;
import net.minecraft.class_7893;
import net.minecraft.class_7898;
import net.minecraft.class_4095.class_5303;
import net.minecraft.class_4103.class_4104;
import net.minecraft.class_4103.class_4216;

public class VillagerTasksMCA {
   public static final ImmutableList<class_4140<?>> MEMORY_TYPES = ImmutableList.of(
      class_4140.field_18438,
      class_4140.field_18439,
      class_4140.field_25160,
      class_4140.field_18440,
      class_4140.field_18441,
      class_4140.field_18442,
      class_4140.field_19006,
      class_4140.field_18443,
      class_4140.field_18444,
      class_4140.field_22354,
      class_4140.field_22332,
      class_4140.field_18445,
      new class_4140[]{
         class_4140.field_18446,
         class_4140.field_18447,
         class_4140.field_18448,
         class_4140.field_18449,
         class_4140.field_26389,
         class_4140.field_19007,
         class_4140.field_18451,
         class_4140.field_18452,
         class_4140.field_18453,
         class_4140.field_18873,
         class_4140.field_19008,
         class_4140.field_19009,
         class_4140.field_19293,
         class_4140.field_19385,
         class_4140.field_20616,
         class_4140.field_19386,
         class_4140.field_25754,
         class_4140.field_22355,
         class_4140.field_22475,
         (class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get(),
         (class_4140)MemoryModuleTypeMCA.STAYING.get(),
         (class_4140)MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY.get(),
         (class_4140)MemoryModuleTypeMCA.WEARS_ARMOR.get(),
         (class_4140)MemoryModuleTypeMCA.SMALL_BOUNTY.get(),
         (class_4140)MemoryModuleTypeMCA.HIT_BY_PLAYER.get(),
         (class_4140)MemoryModuleTypeMCA.LAST_GRIEVE.get(),
         (class_4140)MemoryModuleTypeMCA.FORCED_HOME.get()
      }
   );
   public static final ImmutableList<class_4149<? extends class_4148<? super class_1646>>> SENSOR_TYPES = ImmutableList.of(
      class_4149.field_18467,
      class_4149.field_22358,
      class_4149.field_19010,
      class_4149.field_18469,
      class_4149.field_18470,
      class_4149.field_18875,
      class_4149.field_25756,
      (class_4149)ActivityMCA.VILLAGER_BABIES.get(),
      (class_4149)ActivityMCA.EXPLODING_CREEPER.get(),
      (class_4149)ActivityMCA.GUARD_ENEMIES.get()
   );

   public static class_5303<VillagerEntityMCA> createProfile() {
      return class_4095.method_28311(MEMORY_TYPES, SENSOR_TYPES);
   }

   public static class_4095<VillagerEntityMCA> initializeTasks(VillagerEntityMCA villager, class_4095<VillagerEntityMCA> brain) {
      class_3852 profession = villager.method_7231().method_16924();
      AgeState age = AgeState.byCurrentAge(villager.method_5618());
      boolean noDefault = false;
      if (brain.method_46873((class_4140)MemoryModuleTypeMCA.STAYING.get()).isPresent()) {
         brain.method_18881(class_4168.field_18594, getStayingPackage());
         brain.method_18881(class_4168.field_18594, getImportantCorePackage(0.5F));
         brain.method_18881(class_4168.field_18594, getSelfDefencePackage());
         brain.method_18881(class_4168.field_18599, getPanicPackage(0.5F));
         noDefault = true;
      } else if (brain.method_46873((class_4140)MemoryModuleTypeMCA.PLAYER_FOLLOWING.get()).isPresent()) {
         brain.method_18881(class_4168.field_18594, getFollowingPackage());
         brain.method_18881(class_4168.field_18594, getImportantCorePackage(0.5F));
         brain.method_18881(class_4168.field_18594, getSelfDefencePackage());
         brain.method_18881(class_4168.field_18599, getPanicPackage(0.5F));
         noDefault = true;
      } else if (profession == ProfessionsMCA.MERCENARY.get()) {
         brain.method_18884(SchedulesMCA.GUESTS);
         brain.method_18881(class_4168.field_18594, getImportantCorePackage(0.5F));
         brain.method_18881(class_4168.field_18595, getMercenaryPackage(0.5F));
         brain.method_18881(class_4168.field_18594, getGuardCorePackage(villager));
         brain.method_18881(class_4168.field_18599, getPanicPackage(0.5F));
         brain.method_24529(class_4168.field_18597, getRestPackage(0.5F), ImmutableSet.of(Pair.of(class_4140.field_22355, class_4141.field_18457)));
         brain.method_18881((class_4168)ActivityMCA.CHORE.get(), getChorePackage());
         noDefault = true;
      } else if (!villager.requiresHome()) {
         brain.method_18884(SchedulesMCA.GUESTS);
         brain.method_18881(class_4168.field_18594, getImportantCorePackage(0.5F));
         brain.method_18881(class_4168.field_18595, getAdventurerPackage(0.5F));
         brain.method_18881(class_4168.field_18594, getSelfDefencePackage());
         brain.method_18881(class_4168.field_18599, getPanicPackage(0.5F));
         brain.method_24529(class_4168.field_18597, getRestPackage(0.5F), ImmutableSet.of(Pair.of(class_4140.field_22355, class_4141.field_18457)));
         noDefault = true;
      } else {
         if (age == AgeState.BABY) {
            brain.method_18884(class_4170.field_18605);
            return brain;
         }

         if (age != AgeState.ADULT) {
            brain.method_18884(class_4170.field_18605);
            brain.method_18881(class_4168.field_18885, getPlayPackage(1.0F));
            brain.method_18881(class_4168.field_18594, getSelfDefencePackage());
         } else if (villager.isGuard()) {
            brain.method_18884(SchedulesMCA.getTypeSchedule(villager, SchedulesMCA.GUARD, SchedulesMCA.GUARD_NIGHT));
            brain.method_18881(class_4168.field_18594, getGuardCorePackage(villager));
            brain.method_18881(class_4168.field_18596, getGuardWorkPackage());
            brain.method_18881(class_4168.field_18599, getGuardPanicPackage(0.5F));
            brain.method_18881(class_4168.field_19041, getGuardWorkPackage());
         } else {
            brain.method_18884(SchedulesMCA.getTypeSchedule(villager));
            brain.method_18881(class_4168.field_18594, getWorkingCorePackage(profession, 0.5F));
            brain.method_24529(
               class_4168.field_18596, getWorkPackage(profession, 0.5F), ImmutableSet.of(Pair.of(class_4140.field_18439, class_4141.field_18456))
            );
            brain.method_18881(class_4168.field_18594, getSelfDefencePackage());
            brain.method_18881(class_4168.field_19041, getRaidPackage(0.5F));
         }
      }

      brain.method_18881((class_4168)ActivityMCA.GRIEVE.get(), getGrievingPackage());
      if (!noDefault) {
         brain.method_18881(class_4168.field_18594, getImportantCorePackage(0.5F));
         brain.method_18881(class_4168.field_18594, getCorePackage(0.5F));
         brain.method_24529(class_4168.field_18598, getMeetPackage(0.5F), ImmutableSet.of(Pair.of(class_4140.field_18440, class_4141.field_18456)));
         brain.method_24529(class_4168.field_18597, getRestPackage(0.5F), ImmutableSet.of(Pair.of(class_4140.field_22355, class_4141.field_18457)));
         brain.method_18881(class_4168.field_18595, getIdlePackage(0.5F));
         brain.method_18881(class_4168.field_18599, getPanicPackage(0.5F));
         brain.method_18881(class_4168.field_19042, getPreRaidPackage(0.5F));
         brain.method_18881(class_4168.field_19043, getHidePackage(0.5F));
         brain.method_18881((class_4168)ActivityMCA.CHORE.get(), getChorePackage());
      }

      brain.method_18890(ImmutableSet.of(class_4168.field_18594));
      brain.method_18897(class_4168.field_18595);
      brain.method_24526(class_4168.field_18595);
      brain.method_18871(villager.method_37908().method_8532(), villager.method_37908().method_8510());
      return brain;
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getStayingPackage() {
      return ImmutableList.of(Pair.of(0, new StayTask()), getFullLookBehavior());
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getFollowingPackage() {
      return ImmutableList.of(Pair.of(0, new FollowTask()), getMinimalLookBehavior());
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getImportantCorePackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, new class_4125(0.8F)),
         Config.getInstance().useSmarterDoorAI ? Pair.of(0, new SmarterOpenDoorsTask()) : Pair.of(0, class_4107.method_46964()),
         Pair.of(0, new class_4110(45, 90)),
         Pair.of(0, class_4214.method_47204()),
         Pair.of(0, new DeliverMessageTask()),
         Pair.of(1, new WanderOrTeleportToTargetTask()),
         Pair.of(3, new InteractTask(speedModifier)),
         Pair.of(
            10,
            new ExtendedFindPointOfInterestTask(
               registryEntry -> registryEntry.method_40225(class_7477.field_39291),
               class_4140.field_18438,
               false,
               Optional.of((byte)14),
               villager -> villager.getResidency().seekHome(),
               (entity, pos) -> {
                  VillageManager manager = VillageManager.get((class_3218)entity.method_37908());
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

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getCorePackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, new GreetPlayerTask()),
         Pair.of(0, class_4249.method_47033()),
         Pair.of(0, class_4253.method_47086()),
         Pair.of(5, class_4815.method_46943(speedModifier, false, 4)),
         Pair.of(
            10,
            new ExtendedFindPointOfInterestTask(
               registryEntry -> registryEntry.method_40225(class_7477.field_39291),
               class_4140.field_18438,
               false,
               Optional.of((byte)14),
               villager -> villager.getResidency().seekHome(),
               (entity, pos) -> {
                  VillageManager manager = VillageManager.get((class_3218)entity.method_37908());
                  return manager.findNearestVillage(entity).filter(v -> v.getBuildingAt(pos).filter(b -> b.getBuildingType().noBeds()).isPresent()).isEmpty();
               }
            )
         ),
         Pair.of(
            10,
            new ExtendedFindPointOfInterestTask(
               registryEntry -> registryEntry.method_40225(class_7477.field_39292),
               class_4140.field_18440,
               true,
               Optional.of((byte)14),
               villager -> villager.method_18868().method_46873(class_4140.field_18440).ifPresent(p -> {
                  if (villager.method_37908().method_27983() == p.method_19442()) {
                     VillageManager manager = VillageManager.get((class_3218)villager.method_37908());
                     if (!manager.cache.contains(p.method_19446())) {
                        manager.cache.add(p.method_19446());
                        manager.processBuilding(p.method_19446());
                     }

                     villager.getResidency().seekHome();
                  }
               })
            )
         )
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getWorkingCorePackage(class_3852 profession, float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, class_4128.method_47190(profession.comp_819(), class_4140.field_18439)),
         Pair.of(0, class_4128.method_47190(profession.comp_820(), class_4140.field_25160)),
         Pair.of(2, class_5326.method_47006()),
         Pair.of(3, new class_4108(speedModifier)),
         Pair.of(6, LazyFindPointOfInterestTask.create(profession.comp_820(), class_4140.field_18439, class_4140.field_25160, true, Optional.empty())),
         Pair.of(7, new class_5325(speedModifier)),
         Pair.of(8, class_5327.method_47207(speedModifier)),
         Pair.of(10, class_4114.method_46887()),
         Pair.of(10, LoseUnimportantJobTask.create())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getSelfDefencePackage() {
      return ImmutableList.of(
         Pair.of(0, new class_4113()),
         Pair.of(1, new EquipmentTask(VillagerTasksMCA::isInDanger, v -> EquipmentSet.NAKED)),
         Pair.of(2, new ExtendedMeleeAttackTask(15, 2.5F, class_4140.field_18453))
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getGuardCorePackage(VillagerEntityMCA villager) {
      return ImmutableList.of(
         Pair.of(0, new ConditionalTask(new class_4113(), VillagerTasksMCA::guardTooHurt)),
         Pair.of(0, new SayTask("villager.retreat", 100, e -> guardTooHurt(e) && e.getVillagerBrain().isPanicking())),
         Pair.of(0, new SayTask("villager.attack", 160, e -> !guardTooHurt(e) && getPreferredTarget(e).isPresent())),
         Pair.of(0, new ConditionalTask(new ExtendedMeleeAttackTask(15, 2.5F, class_4140.field_18453), VillagerTasksMCA::guardTooHurt)),
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
         Pair.of(2, class_4824.method_47120(t -> true, VillagerTasksMCA::getPreferredTarget)),
         Pair.of(3, class_4828.method_47138(livingEntity -> !isPreferredTarget(villager, livingEntity))),
         Pair.of(4, new BowTask(20, 12)),
         Pair.of(5, class_7898.method_47227(v -> v.method_24518(class_1802.field_8399), class_4807.method_46901(5, 0.75F))),
         Pair.of(6, class_4822.method_47094(0.75F)),
         Pair.of(7, new ExtendedMeleeAttackTask(20, 2.0F)),
         Pair.of(8, new class_4810()),
         new Pair[0]
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getGuardWorkPackage() {
      return ImmutableList.of(Pair.of(10, new PatrolVillageTask(4, 0.4F)), Pair.of(99, class_4127.method_47184()));
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getGuardPanicPackage(float speedModifier) {
      float f = speedModifier * 1.5F;
      return ImmutableList.of(
         Pair.of(1, class_4100.method_47197()),
         Pair.of(2, class_4121.method_24603(class_4140.field_18453, f, 6, false)),
         Pair.of(2, class_4121.method_24603(class_4140.field_18452, f, 6, false)),
         Pair.of(3, class_4117.method_47192(f, 2, 2)),
         getMinimalLookBehavior()
      );
   }

   private static boolean guardTooHurt(VillagerEntityMCA villager) {
      return villager.method_6032() < villager.method_6063() * 0.25;
   }

   private static Optional<? extends class_1309> getPreferredTarget(VillagerEntityMCA villager) {
      if (guardTooHurt(villager)) {
         return Optional.empty();
      }

      Optional<class_1309> current = villager.method_18868().method_46873(class_4140.field_22355);
      if (current.isPresent() && shouldKeepAttackTarget(villager, current.get())) {
         return current;
      }

      Optional<class_1309> primary = villager.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.NEAREST_GUARD_ENEMY.get());
      return primary.isPresent() && shouldRespondToGuardEnemy(villager, primary.get()) ? primary : Optional.empty();
   }

   private static boolean shouldKeepAttackTarget(VillagerEntityMCA villager, class_1309 target) {
      return target.method_5805()
         && !target.method_31481()
         && target.method_37908() == villager.method_37908()
         && villager.method_18395(target)
         && shouldRespondToGuardEnemy(villager, target);
   }

   private static boolean shouldRespondToGuardEnemy(VillagerEntityMCA villager, class_1309 target) {
      return getActivity(villager) != class_4168.field_18597
         || target.method_5739(villager) < 8.0
         || villager.getResidency().getHomeVillage().filter(village -> village.isWithinBorder(villager)).isEmpty();
   }

   private static boolean isPreferredTarget(VillagerEntityMCA villager, class_1309 entity) {
      Optional<? extends class_1309> target = getPreferredTarget(villager);
      return target.filter(livingEntity -> livingEntity == entity).isPresent();
   }

   public static boolean isOnDuty(VillagerEntityMCA villager) {
      return getActivity(villager) == class_4168.field_18596
         || villager.method_18868().method_46873(class_4140.field_22355).isPresent()
         || getPreferredTarget(villager).isPresent();
   }

   public static boolean isInDanger(VillagerEntityMCA villager) {
      return villager.getVillagerBrain().isPanicking() || villager.method_18868().method_46873(class_4140.field_22355).isPresent();
   }

   private static class_4168 getActivity(VillagerEntityMCA villager) {
      return villager.method_18868().method_18894().method_19213((int)(villager.method_37908().method_8532() % 24000L));
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getGrievingPackage() {
      return ImmutableList.of(
         Pair.of(
            0,
            new SequenceTask(
               ImmutableMap.of(class_4140.field_18445, class_4141.field_18457),
               ImmutableList.of(
                  new EnterBuildingTask("graveyard", 0.5F),
                  new class_4118(
                     ImmutableList.of(
                        Pair.of(new HoldItemTask(class_1268.field_5808, class_1802.field_17510), 1),
                        Pair.of(new HoldItemTask(class_1268.field_5808, class_1802.field_17502), 1),
                        Pair.of(new HoldItemTask(class_1268.field_5808, class_1802.field_17509), 1),
                        Pair.of(new HoldItemTask(class_1268.field_5808, class_1802.field_17511), 1)
                     )
                  ),
                  new WanderOrTeleportToTargetTask(),
                  new class_4101(100, 300),
                  new SayTask("villager.grieving"),
                  new class_4101(100, 300),
                  new SayTask("villager.grieving"),
                  new class_4101(100, 300),
                  new SayTask("villager.grieving"),
                  new HoldItemTask(class_1268.field_5808, class_1799.field_8037),
                  new LambdaTask<>(v -> {
                     v.getVillagerBrain().justGrieved();
                     v.method_18868().method_18871(v.method_37908().method_8532(), v.method_37908().method_8510());
                  })
               )
            )
         )
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getWorkPackage(class_3852 profession, float speedModifier) {
      class_4133 villagerWorkTask;
      if (profession == class_3852.field_17056) {
         villagerWorkTask = new class_4983();
      } else {
         villagerWorkTask = new class_4133();
      }

      return ImmutableList.of(
         getMinimalLookBehavior(),
         Pair.of(
            5,
            new class_4118(
               ImmutableList.of(
                  Pair.of(villagerWorkTask, 7),
                  Pair.of(class_4116.method_47153(class_4140.field_18439, 0.4F, 4), 2),
                  Pair.of(class_4219.method_47157(class_4140.field_18439, 0.4F, 1, 10), 5),
                  Pair.of(class_4220.method_47161(class_4140.field_18873, speedModifier, 1, 6, class_4140.field_18439), 5),
                  Pair.of(new class_4217(), profession == class_3852.field_17056 ? 2 : 5),
                  Pair.of(new class_4982(), profession == class_3852.field_17056 ? 4 : 7)
               )
            )
         ),
         Pair.of(10, new class_4130(400, 1600)),
         Pair.of(10, class_4109.method_47082(class_1299.field_6097, 4)),
         Pair.of(2, class_4122.method_47102(class_4140.field_18439, speedModifier, 9, 100, 1200)),
         Pair.of(3, new class_4243(100)),
         Pair.of(99, class_4127.method_47184())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getPlayPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, new class_4112(80, 120)),
         getFullLookBehavior(),
         Pair.of(5, class_4218.method_47000()),
         Pair.of(
            5,
            new class_4118(
               ImmutableMap.of(class_4140.field_19006, class_4141.field_18457),
               ImmutableList.of(
                  Pair.of(class_4106.method_18941(class_1299.field_6077, 8, class_4140.field_18447, speedModifier, 2), 2),
                  Pair.of(class_4106.method_18941(class_1299.field_16281, 8, class_4140.field_18447, speedModifier, 2), 1),
                  Pair.of(class_4117.method_47191(speedModifier), 1),
                  Pair.of(class_4120.method_47104(speedModifier, 2), 1),
                  Pair.of(new class_4245(speedModifier), 2),
                  Pair.of(new class_4101(20, 40), 2)
               )
            )
         ),
         Pair.of(99, class_4127.method_47184())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getRestPackage(float speed) {
      return ImmutableList.of(
         Pair.of(2, ExtendedWalkTowardsTask.create(class_4140.field_18438, speed, 1, Config.getInstance().getVillagerPathfindingDistance(), 1200, v -> {
            Optional<Boolean> memory = v.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.FORCED_HOME.get());
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
                  registryEntry -> registryEntry.method_40225(class_7477.field_39291), class_4140.field_18438, entity -> {
                     if (entity instanceof VillagerEntityMCA villager) {
                        villager.getResidency().seekHome();
                     }
                  }
               ),
               v -> {
                  Optional<Boolean> memory = v.method_18868().method_46873((class_4140)MemoryModuleTypeMCA.FORCED_HOME.get());
                  return memory == null || memory.isEmpty();
               }
            )
         ),
         Pair.of(3, new class_4123()),
         Pair.of(
            5,
            new class_4118(
               ImmutableMap.of(class_4140.field_18438, class_4141.field_18457),
               ImmutableList.of(
                  Pair.of(class_4290.method_47048(speed), 1),
                  Pair.of(class_4289.method_46949(speed), 4),
                  Pair.of(class_4458.method_46934(speed, 4), 2),
                  Pair.of(new class_4101(20, 40), 2)
               )
            )
         ),
         Pair.of(99, class_4127.method_47184())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getMeetPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(
            2, new class_4118(ImmutableList.of(Pair.of(class_4116.method_47153(class_4140.field_18440, 0.4F, 40), 2), Pair.of(class_4124.method_47111(), 2)))
         ),
         Pair.of(10, new class_4130(400, 1600)),
         Pair.of(10, class_4109.method_47082(class_1299.field_6097, 4)),
         Pair.of(2, class_4122.method_47102(class_4140.field_18440, speedModifier, 6, 100, 200)),
         Pair.of(3, new class_4243(100)),
         Pair.of(3, class_4128.method_47190(registryEntry -> registryEntry.method_40225(class_7477.field_39292), class_4140.field_18440)),
         Pair.of(
            3,
            new class_4103(
               ImmutableMap.of(),
               ImmutableSet.of(class_4140.field_18447),
               class_4104.field_18348,
               class_4216.field_18855,
               ImmutableList.of(Pair.of(new class_4126(), 1))
            )
         ),
         getFullLookBehavior(),
         Pair.of(99, class_4127.method_47184())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getIdlePackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(1, new EnterFavoredBuildingTask(0.5F)),
         Pair.of(
            2,
            new class_4118(
               ImmutableList.of(
                  Pair.of(class_4106.method_18941((class_1299)EntitiesMCA.FEMALE_VILLAGER.get(), 8, class_4140.field_18447, speedModifier, 2), 2),
                  Pair.of(class_4106.method_18941((class_1299)EntitiesMCA.MALE_VILLAGER.get(), 8, class_4140.field_18447, speedModifier, 2), 2),
                  Pair.of(class_4106.method_18941(class_1299.field_16281, 8, class_4140.field_18447, speedModifier, 2), 1),
                  Pair.of(class_4117.method_47191(speedModifier), 1),
                  Pair.of(class_4120.method_47104(speedModifier, 2), 1),
                  Pair.of(new class_4245(speedModifier), 1),
                  Pair.of(new class_4101(30, 60), 1)
               )
            )
         ),
         Pair.of(3, new class_4243(100)),
         Pair.of(3, class_4109.method_47082(class_1299.field_6097, 4)),
         Pair.of(3, new class_4130(400, 1600)),
         Pair.of(3, new GrieveTask()),
         Pair.of(
            3,
            new class_4103(
               ImmutableMap.of(),
               ImmutableSet.of(class_4140.field_18447),
               class_4104.field_18348,
               class_4216.field_18855,
               ImmutableList.of(Pair.of(new class_4126(), 1))
            )
         ),
         getFullLookBehavior(),
         Pair.of(99, class_4127.method_47184())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getPanicPackage(float speedModifier) {
      float f = speedModifier * 1.5F;
      return ImmutableList.of(
         Pair.of(0, class_4100.method_47197()),
         Pair.of(1, class_4121.method_24603(class_4140.field_18453, f, 6, false)),
         Pair.of(1, class_4121.method_24603(class_4140.field_18452, f, 6, false)),
         Pair.of(3, class_4117.method_47192(f, 2, 2)),
         getMinimalLookBehavior()
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getPreRaidPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, class_4251.method_47044()),
         Pair.of(
            0,
            new class_4118(
               ImmutableList.of(
                  Pair.of(class_4122.method_47102(class_4140.field_18440, speedModifier * 1.5F, 2, 150, 200), 6),
                  Pair.of(class_4117.method_47191(speedModifier * 1.5F), 2)
               )
            )
         ),
         getMinimalLookBehavior(),
         Pair.of(99, class_4250.method_47041())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getRaidPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(
            0, new class_4118(ImmutableList.of(Pair.of(class_4248.method_46995(speedModifier), 5), Pair.of(class_4117.method_47191(speedModifier * 1.1F), 2)))
         ),
         Pair.of(0, new class_4242(600, 600)),
         Pair.of(2, class_4246.method_46975(24, speedModifier * 1.4F, 1)),
         getMinimalLookBehavior(),
         Pair.of(99, class_4250.method_47041())
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getHidePackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(0, class_4252.method_47077(15, 3)), Pair.of(1, class_4246.method_46975(32, speedModifier * 1.25F, 2)), getMinimalLookBehavior()
      );
   }

   public static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getChorePackage() {
      return ImmutableList.of(Pair.of(0, new ChoppingTask()), Pair.of(0, new FishingTask()), Pair.of(0, new HarvestingTask()), Pair.of(0, new HuntingTask()));
   }

   private static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getAdventurerPackage(float speedModifier) {
      return ImmutableList.of(
         Pair.of(5, class_4106.method_18941((class_1299)EntitiesMCA.FEMALE_VILLAGER.get(), 8, class_4140.field_18447, speedModifier, 2)),
         Pair.of(5, class_4106.method_18941((class_1299)EntitiesMCA.MALE_VILLAGER.get(), 8, class_4140.field_18447, speedModifier, 2)),
         Pair.of(5, class_4106.method_18941(class_1299.field_16281, 8, class_4140.field_18447, speedModifier, 2)),
         Pair.of(5, class_4117.method_47191(speedModifier)),
         Pair.of(5, class_4120.method_47104(speedModifier, 2)),
         Pair.of(5, new EnterBuildingTask("inn", 0.5F))
      );
   }

   private static ImmutableList<Pair<Integer, ? extends class_7893<? super VillagerEntityMCA>>> getMercenaryPackage(float speedModifier) {
      return ImmutableList.of(Pair.of(5, class_4117.method_47191(speedModifier)), Pair.of(5, class_4120.method_47104(speedModifier, 2)));
   }

   private static Pair<Integer, class_7893<class_1309>> getFullLookBehavior() {
      return Pair.of(
         5,
         new class_4118(
            ImmutableList.of(
               Pair.of(class_4119.method_47057(class_1299.field_16281, 8.0F), 8),
               Pair.of(class_4119.method_47057(class_1299.field_6077, 8.0F), 2),
               Pair.of(class_4119.method_47057(class_1299.field_6097, 8.0F), 2),
               Pair.of(class_4119.method_47061(class_1311.field_6294, 8.0F), 1),
               Pair.of(class_4119.method_47061(class_1311.field_6300, 8.0F), 1),
               Pair.of(class_4119.method_47061(class_1311.field_24460, 8.0F), 1),
               Pair.of(class_4119.method_47061(class_1311.field_6302, 8.0F), 1),
               Pair.of(new class_4101(30, 60), 2)
            )
         )
      );
   }

   private static Pair<Integer, class_7893<class_1309>> getMinimalLookBehavior() {
      return Pair.of(
         5,
         new class_4118(
            ImmutableList.of(
               Pair.of(class_4119.method_47057(class_1299.field_6077, 8.0F), 2),
               Pair.of(class_4119.method_47057(class_1299.field_6097, 8.0F), 2),
               Pair.of(new class_4101(30, 60), 8)
            )
         )
      );
   }
}
