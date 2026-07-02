package forge.net.mca.entity;

import forge.net.mca.MCA;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.resources.Names;
import forge.net.mca.util.WorldUtils;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ZombieVillagerFactory {
   private final Level world;
   private Optional<String> name = Optional.empty();
   private Optional<Gender> gender = Optional.empty();
   private Optional<VillagerProfession> profession = Optional.empty();
   private Optional<VillagerType> type = Optional.empty();
   private OptionalInt level = OptionalInt.empty();
   private Optional<Vec3> position = Optional.empty();

   private ZombieVillagerFactory(Level world) {
      this.world = world;
   }

   public static ZombieVillagerFactory newVillager(Level world) {
      return new ZombieVillagerFactory(world);
   }

   public ZombieVillagerFactory withGender(Gender gender) {
      this.gender = Optional.ofNullable(gender);
      return this;
   }

   public ZombieVillagerFactory withType(VillagerType type) {
      this.type = Optional.ofNullable(type);
      return this;
   }

   public ZombieVillagerFactory withProfession(VillagerProfession prof) {
      this.profession = Optional.ofNullable(prof);
      return this;
   }

   public ZombieVillagerFactory withProfession(VillagerProfession prof, int level) {
      this.withProfession(prof);
      this.level = OptionalInt.of(level);
      return this;
   }

   public ZombieVillagerFactory withName(String name) {
      this.name = Optional.ofNullable(name);
      return this;
   }

   public ZombieVillagerFactory withPosition(double x, double y, double z) {
      return this.withPosition(new Vec3(x, y, z));
   }

   public ZombieVillagerFactory withPosition(Entity entity) {
      return this.withPosition(entity.m_20185_(), entity.m_20186_(), entity.m_20189_());
   }

   public ZombieVillagerFactory withPosition(Vec3 pos) {
      this.position = Optional.of(pos);
      return this;
   }

   public ZombieVillagerFactory withPosition(BlockPos pos) {
      return this.withPosition(Vec3.m_82539_(pos.m_7494_()));
   }

   public ZombieVillagerEntityMCA spawn(MobSpawnType reason) {
      if (this.position.isEmpty()) {
         MCA.LOGGER.info("Attempted to spawn villager without a position being set!");
      }

      ZombieVillagerEntityMCA build = this.build();
      WorldUtils.spawnEntity(this.world, build, reason);
      return build;
   }

   public ZombieVillagerEntityMCA build() {
      Gender gender = this.gender.orElseGet(Gender::getRandom);
      ZombieVillagerEntityMCA zombie = (ZombieVillagerEntityMCA)gender.getZombieType().m_20615_(this.world);
      zombie.getGenetics().setGender(gender);
      zombie.setName(this.name.orElseGet(() -> Names.pickCitizenName(gender, zombie)));
      this.position.ifPresent(pos -> zombie.m_20248_(pos.m_7096_(), pos.m_7098_(), pos.m_7094_()));
      VillagerData data = zombie.m_7141_();
      zombie.m_34375_(
         new VillagerData(this.type.orElseGet(data::m_35560_), this.profession.orElse(VillagerProfession.f_35585_), this.level.orElseGet(data::m_35576_))
      );
      return zombie;
   }
}
