package quilt.net.mca.entity.ai;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_3730;
import net.minecraft.class_5819;
import quilt.net.mca.Config;
import quilt.net.mca.advancement.criterion.CriterionMCA;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.relationship.AgeState;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.item.BabyItem;
import quilt.net.mca.server.world.data.Village;
import quilt.net.mca.util.WorldUtils;
import quilt.net.mca.util.network.datasync.CDataManager;
import quilt.net.mca.util.network.datasync.CDataParameter;
import quilt.net.mca.util.network.datasync.CParameter;

public class Pregnancy {
   private static final CDataParameter<Boolean> HAS_BABY = CParameter.create("hasBaby", false);
   private static final CDataParameter<Boolean> IS_BABY_MALE = CParameter.create("isBabyMale", false);
   private static final CDataParameter<Integer> BABY_AGE = CParameter.create("babyAge", 0);
   private final VillagerEntityMCA mother;

   public static <E extends class_1297> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
      return builder.addAll(HAS_BABY, IS_BABY_MALE, BABY_AGE);
   }

   Pregnancy(VillagerEntityMCA entity) {
      this.mother = entity;
   }

   public boolean isPregnant() {
      return this.mother.getTrackedValue(HAS_BABY);
   }

   public void setPregnant(boolean pregnant) {
      this.mother.setTrackedValue(HAS_BABY, pregnant);
   }

   public int getBabyAge() {
      return this.mother.getTrackedValue(BABY_AGE);
   }

   public void setBabyAge(int age) {
      this.mother.setTrackedValue(BABY_AGE, age);
   }

   public Gender getGender() {
      return this.mother.getTrackedValue(IS_BABY_MALE) ? Gender.MALE : Gender.FEMALE;
   }

   public void tick() {
      if (this.isPregnant()) {
         this.setBabyAge(this.getBabyAge() + 60);
         if (this.getBabyAge() >= Config.getInstance().babyItemGrowUpTime) {
            this.setBabyAge(0);
            this.getFather().ifPresent(father -> {
               this.setPregnant(false);
               VillagerEntityMCA child = this.createChild(this.getGender(), father);
               child.method_5814(this.mother.method_23317(), this.mother.method_23318(), this.mother.method_23321());
               WorldUtils.spawnEntity(this.mother.method_37908(), child, class_3730.field_16466);
            });
         }
      }
   }

   public boolean tryStartGestation() {
      return this.isPregnant() ? false : this.getFather().map(father -> {
         if (this.mother.getGenetics().getGender() == Gender.MALE && father.getGenetics().getGender() != Gender.MALE) {
            return father.getRelationships().getPregnancy().tryStartGestation();
         }

         this.setPregnant(true);
         this.mother.setTrackedValue(IS_BABY_MALE, this.mother.method_37908().field_9229.method_43056());
         return true;
      }).orElse(false);
   }

   public VillagerEntityMCA createChild(Gender gender, VillagerEntityMCA partner) {
      VillagerEntityMCA child = Objects.requireNonNull((VillagerEntityMCA)gender.getVillagerType().method_5883(this.mother.method_37908()));
      child.getGenetics().combine(partner.getGenetics(), this.mother.getGenetics());
      child.getTraits().inherit(partner.getTraits());
      child.getTraits().inherit(this.mother.getTraits());
      child.method_7217(true);
      child.setAgeState(AgeState.TODDLER);
      child.getRelationships().getFamilyEntry().replaceParents(this.mother.getRelationships(), partner.getRelationships());
      child.getRelationships().getFamily(2, 0).filter(class_3222.class::isInstance).map(class_3222.class::cast).forEach(CriterionMCA.FAMILY::trigger);
      this.mother
         .getResidency()
         .getHomeVillage()
         .flatMap(Village::getCivilRegistry)
         .ifPresent(r -> r.addText(class_2561.method_43469("events.baby", new Object[]{this.mother.method_5477(), partner.method_5477()})));
      return child;
   }

   public VillagerEntityMCA createChild(Gender gender) {
      return this.createChild(gender, this.mother);
   }

   private Optional<VillagerEntityMCA> getFather() {
      return this.mother.getRelationships().getPartner().filter(VillagerEntityMCA.class::isInstance).map(VillagerEntityMCA.class::cast);
   }

   public void procreate(class_1297 spouse) {
      class_5819 random = this.mother.method_6051();
      int count = 1;

      while (random.method_43057() < Config.getInstance().twinBabyChance && count < 8) {
         count++;
      }

      if (spouse instanceof class_3222 player) {
         CriterionMCA.BABY_CRITERION.trigger(player, count);
      }

      long seed = random.method_43055();

      for (int i = 0; i < count; i++) {
         boolean flip = this.mother.getGenetics().getGender() == Gender.MALE;
         class_1799 stack = BabyItem.createItem((class_1297)(flip ? spouse : this.mother), (class_1297)(flip ? this.mother : spouse), seed);
         if (!(spouse instanceof class_1657 player && player.method_7270(stack))) {
            this.mother.method_35199().method_5491(stack);
         }
      }
   }
}
