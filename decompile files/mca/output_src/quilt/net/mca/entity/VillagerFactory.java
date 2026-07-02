package quilt.net.mca.entity;

import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_1297;
import net.minecraft.class_1916;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3730;
import net.minecraft.class_3850;
import net.minecraft.class_3852;
import net.minecraft.class_3854;
import quilt.net.mca.MCA;
import quilt.net.mca.entity.ai.relationship.AgeState;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.resources.Names;
import quilt.net.mca.util.WorldUtils;

public class VillagerFactory {
   private final class_1937 world;
   private Optional<String> name = Optional.empty();
   private Optional<Gender> gender = Optional.empty();
   private Optional<class_3852> profession = Optional.empty();
   private Optional<class_3854> type = Optional.empty();
   private OptionalInt level = OptionalInt.empty();
   private Optional<class_1916> offers = Optional.empty();
   private OptionalInt age = OptionalInt.empty();
   private Optional<class_243> position = Optional.empty();

   private VillagerFactory(class_1937 world) {
      this.world = world;
   }

   public static VillagerFactory newVillager(class_1937 world) {
      return new VillagerFactory(world);
   }

   public VillagerFactory withGender(Gender gender) {
      this.gender = Optional.ofNullable(gender);
      return this;
   }

   public VillagerFactory withType(class_3854 type) {
      this.type = Optional.ofNullable(type);
      return this;
   }

   public VillagerFactory withProfession(class_3852 prof) {
      this.profession = Optional.ofNullable(prof);
      return this;
   }

   public VillagerFactory withProfession(class_3852 prof, int level) {
      this.withProfession(prof);
      this.level = OptionalInt.of(level);
      return this;
   }

   public VillagerFactory withProfession(class_3852 prof, int level, class_1916 offers) {
      this.withProfession(prof, level);
      this.offers = Optional.of(offers);
      return this;
   }

   public VillagerFactory withName(String name) {
      this.name = Optional.ofNullable(name);
      return this;
   }

   public VillagerFactory withPosition(double x, double y, double z) {
      return this.withPosition(new class_243(x, y, z));
   }

   public VillagerFactory withPosition(class_1297 entity) {
      return this.withPosition(entity.method_23317(), entity.method_23318(), entity.method_23321());
   }

   public VillagerFactory withPosition(class_243 pos) {
      this.position = Optional.of(pos);
      return this;
   }

   public VillagerFactory withPosition(class_2338 pos) {
      return this.withPosition(class_243.method_24955(pos.method_10084()));
   }

   public VillagerFactory withAge(int age) {
      this.age = OptionalInt.of(age);
      return this;
   }

   public VillagerEntityMCA spawn(class_3730 reason) {
      if (this.position.isEmpty()) {
         MCA.LOGGER.info("Attempted to spawn villager without a position being set!");
      }

      VillagerEntityMCA villager = this.build();
      WorldUtils.spawnEntity(this.world, villager, reason);
      return villager;
   }

   public VillagerEntityMCA build() {
      Gender gender = this.gender.orElseGet(Gender::getRandom);
      VillagerEntityMCA villager = (VillagerEntityMCA)gender.getVillagerType().method_5883(this.world);
      assert villager != null;
      villager.getGenetics().setGender(gender);
      villager.method_5614(this.age.orElseGet(() -> villager.method_6051().method_43048(AgeState.getMaxAge() * 3) - AgeState.getMaxAge()));
      this.position.ifPresent(pos -> villager.method_30634(pos.method_10216(), pos.method_10214(), pos.method_10215()));
      villager.setName(this.name.orElseGet(() -> Names.pickCitizenName(gender, villager)));
      class_3850 data = villager.method_7231();
      villager.method_7195(
         new class_3850(this.type.orElseGet(data::method_16919), this.profession.orElse(class_3852.field_17051), this.level.orElseGet(data::method_16925))
      );
      this.offers.ifPresent(villager::method_16917);
      return villager;
   }
}
