package forge.net.mca.entity;

import forge.net.mca.MCA;
import forge.net.mca.entity.ai.relationship.AgeState;
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
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VillagerFactory {
   private final Level world;
   private Optional<String> name = Optional.empty();
   private Optional<Gender> gender = Optional.empty();
   private Optional<VillagerProfession> profession = Optional.empty();
   private Optional<VillagerType> type = Optional.empty();
   private OptionalInt level = OptionalInt.empty();
   private Optional<MerchantOffers> offers = Optional.empty();
   private OptionalInt age = OptionalInt.empty();
   private Optional<Vec3> position = Optional.empty();

   private VillagerFactory(Level world) {
      this.world = world;
   }

   public static VillagerFactory newVillager(Level world) {
      return new VillagerFactory(world);
   }

   public VillagerFactory withGender(Gender gender) {
      this.gender = Optional.ofNullable(gender);
      return this;
   }

   public VillagerFactory withType(VillagerType type) {
      this.type = Optional.ofNullable(type);
      return this;
   }

   public VillagerFactory withProfession(VillagerProfession prof) {
      this.profession = Optional.ofNullable(prof);
      return this;
   }

   public VillagerFactory withProfession(VillagerProfession prof, int level) {
      this.withProfession(prof);
      this.level = OptionalInt.of(level);
      return this;
   }

   public VillagerFactory withProfession(VillagerProfession prof, int level, MerchantOffers offers) {
      this.withProfession(prof, level);
      this.offers = Optional.of(offers);
      return this;
   }

   public VillagerFactory withName(String name) {
      this.name = Optional.ofNullable(name);
      return this;
   }

   public VillagerFactory withPosition(double x, double y, double z) {
      return this.withPosition(new Vec3(x, y, z));
   }

   public VillagerFactory withPosition(Entity entity) {
      return this.withPosition(entity.m_20185_(), entity.m_20186_(), entity.m_20189_());
   }

   public VillagerFactory withPosition(Vec3 pos) {
      this.position = Optional.of(pos);
      return this;
   }

   public VillagerFactory withPosition(BlockPos pos) {
      return this.withPosition(Vec3.m_82539_(pos.m_7494_()));
   }

   public VillagerFactory withAge(int age) {
      this.age = OptionalInt.of(age);
      return this;
   }

   public VillagerEntityMCA spawn(MobSpawnType reason) {
      if (this.position.isEmpty()) {
         MCA.LOGGER.info("Attempted to spawn villager without a position being set!");
      }

      VillagerEntityMCA villager = this.build();
      WorldUtils.spawnEntity(this.world, villager, reason);
      return villager;
   }

   public VillagerEntityMCA build() {
      Gender gender = this.gender.orElseGet(Gender::getRandom);
      VillagerEntityMCA villager = (VillagerEntityMCA)gender.getVillagerType().m_20615_(this.world);
      assert villager != null;
      villager.getGenetics().setGender(gender);
      villager.m_146762_(this.age.orElseGet(() -> villager.m_217043_().m_188503_(AgeState.getMaxAge() * 3) - AgeState.getMaxAge()));
      this.position.ifPresent(pos -> villager.m_20248_(pos.m_7096_(), pos.m_7098_(), pos.m_7094_()));
      villager.setName(this.name.orElseGet(() -> Names.pickCitizenName(gender, villager)));
      VillagerData data = villager.m_7141_();
      villager.m_34375_(
         new VillagerData(this.type.orElseGet(data::m_35560_), this.profession.orElse(VillagerProfession.f_35585_), this.level.orElseGet(data::m_35576_))
      );
      this.offers.ifPresent(villager::m_35476_);
      return villager;
   }
}
