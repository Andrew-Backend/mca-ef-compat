package forge.net.mca.entity.ai;

import forge.net.mca.Config;
import forge.net.mca.advancement.criterion.CriterionMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.item.BabyItem;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.util.WorldUtils;
import forge.net.mca.util.network.datasync.CDataManager;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CParameter;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Pregnancy {
   private static final CDataParameter<Boolean> HAS_BABY = CParameter.create("hasBaby", false);
   private static final CDataParameter<Boolean> IS_BABY_MALE = CParameter.create("isBabyMale", false);
   private static final CDataParameter<Integer> BABY_AGE = CParameter.create("babyAge", 0);
   private final VillagerEntityMCA mother;

   public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
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
               child.m_6034_(this.mother.m_20185_(), this.mother.m_20186_(), this.mother.m_20189_());
               WorldUtils.spawnEntity(this.mother.m_9236_(), child, MobSpawnType.BREEDING);
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
         this.mother.setTrackedValue(IS_BABY_MALE, this.mother.m_9236_().f_46441_.m_188499_());
         return true;
      }).orElse(false);
   }

   public VillagerEntityMCA createChild(Gender gender, VillagerEntityMCA partner) {
      VillagerEntityMCA child = Objects.requireNonNull((VillagerEntityMCA)gender.getVillagerType().m_20615_(this.mother.m_9236_()));
      child.getGenetics().combine(partner.getGenetics(), this.mother.getGenetics());
      child.getTraits().inherit(partner.getTraits());
      child.getTraits().inherit(this.mother.getTraits());
      child.m_6863_(true);
      child.setAgeState(AgeState.TODDLER);
      child.getRelationships().getFamilyEntry().replaceParents(this.mother.getRelationships(), partner.getRelationships());
      child.getRelationships().getFamily(2, 0).filter(ServerPlayer.class::isInstance).map(ServerPlayer.class::cast).forEach(CriterionMCA.FAMILY::trigger);
      this.mother
         .getResidency()
         .getHomeVillage()
         .flatMap(Village::getCivilRegistry)
         .ifPresent(r -> r.addText(Component.m_237110_("events.baby", new Object[]{this.mother.m_7755_(), partner.m_7755_()})));
      return child;
   }

   public VillagerEntityMCA createChild(Gender gender) {
      return this.createChild(gender, this.mother);
   }

   private Optional<VillagerEntityMCA> getFather() {
      return this.mother.getRelationships().getPartner().filter(VillagerEntityMCA.class::isInstance).map(VillagerEntityMCA.class::cast);
   }

   public void procreate(Entity spouse) {
      RandomSource random = this.mother.m_217043_();
      int count = 1;

      while (random.m_188501_() < Config.getInstance().twinBabyChance && count < 8) {
         count++;
      }

      if (spouse instanceof ServerPlayer player) {
         CriterionMCA.BABY_CRITERION.trigger(player, count);
      }

      long seed = random.m_188505_();

      for (int i = 0; i < count; i++) {
         boolean flip = this.mother.getGenetics().getGender() == Gender.MALE;
         ItemStack stack = BabyItem.createItem((Entity)(flip ? spouse : this.mother), (Entity)(flip ? this.mother : spouse), seed);
         if (!(spouse instanceof Player player && player.m_36356_(stack))) {
            this.mother.m_35311_().m_19173_(stack);
         }
      }
   }
}
