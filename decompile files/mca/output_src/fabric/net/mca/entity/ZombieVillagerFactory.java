package fabric.net.mca.entity;

import fabric.net.mca.MCA;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.resources.Names;
import fabric.net.mca.util.WorldUtils;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3730;
import net.minecraft.class_3850;
import net.minecraft.class_3852;
import net.minecraft.class_3854;

public class ZombieVillagerFactory {
   private final class_1937 world;
   private Optional<String> name = Optional.empty();
   private Optional<Gender> gender = Optional.empty();
   private Optional<class_3852> profession = Optional.empty();
   private Optional<class_3854> type = Optional.empty();
   private OptionalInt level = OptionalInt.empty();
   private Optional<class_243> position = Optional.empty();

   private ZombieVillagerFactory(class_1937 world) {
      this.world = world;
   }

   public static ZombieVillagerFactory newVillager(class_1937 world) {
      return new ZombieVillagerFactory(world);
   }

   public ZombieVillagerFactory withGender(Gender gender) {
      this.gender = Optional.ofNullable(gender);
      return this;
   }

   public ZombieVillagerFactory withType(class_3854 type) {
      this.type = Optional.ofNullable(type);
      return this;
   }

   public ZombieVillagerFactory withProfession(class_3852 prof) {
      this.profession = Optional.ofNullable(prof);
      return this;
   }

   public ZombieVillagerFactory withProfession(class_3852 prof, int level) {
      this.withProfession(prof);
      this.level = OptionalInt.of(level);
      return this;
   }

   public ZombieVillagerFactory withName(String name) {
      this.name = Optional.ofNullable(name);
      return this;
   }

   public ZombieVillagerFactory withPosition(double x, double y, double z) {
      return this.withPosition(new class_243(x, y, z));
   }

   public ZombieVillagerFactory withPosition(class_1297 entity) {
      return this.withPosition(entity.method_23317(), entity.method_23318(), entity.method_23321());
   }

   public ZombieVillagerFactory withPosition(class_243 pos) {
      this.position = Optional.of(pos);
      return this;
   }

   public ZombieVillagerFactory withPosition(class_2338 pos) {
      return this.withPosition(class_243.method_24955(pos.method_10084()));
   }

   public ZombieVillagerEntityMCA spawn(class_3730 reason) {
      if (this.position.isEmpty()) {
         MCA.LOGGER.info("Attempted to spawn villager without a position being set!");
      }

      ZombieVillagerEntityMCA build = this.build();
      WorldUtils.spawnEntity(this.world, build, reason);
      return build;
   }

   public ZombieVillagerEntityMCA build() {
      Gender gender = this.gender.orElseGet(Gender::getRandom);
      ZombieVillagerEntityMCA zombie = (ZombieVillagerEntityMCA)gender.getZombieType().method_5883(this.world);
      zombie.getGenetics().setGender(gender);
      zombie.setName(this.name.orElseGet(() -> Names.pickCitizenName(gender, zombie)));
      this.position.ifPresent(pos -> zombie.method_30634(pos.method_10216(), pos.method_10214(), pos.method_10215()));
      class_3850 data = zombie.method_7231();
      zombie.method_7195(
         new class_3850(this.type.orElseGet(data::method_16919), this.profession.orElse(class_3852.field_17051), this.level.orElseGet(data::method_16925))
      );
      return zombie;
   }
}
