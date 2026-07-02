package forge.net.mca.server;

import forge.net.mca.Config;
import forge.net.mca.ducks.IVillagerEntity;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.VillagerFactory;
import forge.net.mca.entity.ZombieVillagerEntityMCA;
import forge.net.mca.entity.ZombieVillagerFactory;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.server.world.data.Nationality;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public class SpawnQueue {
   private static final SpawnQueue INSTANCE = new SpawnQueue();
   private final ConcurrentLinkedQueue<Villager> villagerSpawnQueue = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<ZombieVillager> zombieVillagerSpawnQueue = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<Zombie> zombieSpawnList = new ConcurrentLinkedQueue<>();

   public static SpawnQueue getInstance() {
      return INSTANCE;
   }

   public void tick() {
      Villager ve = this.villagerSpawnQueue.poll();
      if (ve != null) {
         ve.m_146870_();
         VillagerEntityMCA villager = VillagerFactory.newVillager(ve.m_9236_())
            .withName(ve.m_8077_() ? ve.m_7755_().getString() : null)
            .withGender(Gender.getRandom())
            .withAge(ve.m_146764_())
            .withPosition(ve)
            .withType(ve.m_7141_().m_35560_())
            .withProfession(ve.m_7141_().m_35571_(), ve.m_7141_().m_35576_(), ve.m_6616_())
            .spawn(((IVillagerEntity)ve).getSpawnReason());
         this.copyPastaIntensifies(villager, ve);
      }

      ZombieVillager zve = this.zombieVillagerSpawnQueue.poll();
      if (zve != null) {
         zve.m_146870_();
         ZombieVillagerEntityMCA villager = ZombieVillagerFactory.newVillager(zve.m_9236_())
            .withName(zve.m_8077_() ? zve.m_7755_().getString() : null)
            .withGender(Gender.getRandom())
            .withPosition(zve)
            .withType(zve.m_7141_().m_35560_())
            .withProfession(zve.m_7141_().m_35571_(), zve.m_7141_().m_35576_())
            .spawn(((IVillagerEntity)zve).getSpawnReason());
         this.copyPastaIntensifies(villager, zve);
      }

      Zombie ze = this.zombieSpawnList.poll();
      if (ze != null) {
         ze.m_146870_();
         ZombieVillagerEntityMCA villager = ZombieVillagerFactory.newVillager(ze.m_9236_())
            .withName(ze.m_8077_() ? ze.m_7755_().getString() : null)
            .withGender(Gender.getRandom())
            .withPosition(ze)
            .withType(VillagerType.m_204073_(ze.m_9236_().m_204166_(ze.m_20183_())))
            .withProfession(
               BuiltInRegistries.f_256735_.m_213642_(ze.m_217043_()).<VillagerProfession>map(Holder::m_203334_).orElse(VillagerProfession.f_35585_)
            )
            .spawn(MobSpawnType.NATURAL);
         this.copyPastaIntensifies(villager, ze);
      }
   }

   private void copyPastaIntensifies(PathfinderMob villager, PathfinderMob entity) {
      if (entity.m_21532_()) {
         villager.m_21530_();
      }

      if (entity.m_20147_()) {
         villager.m_20331_(true);
      }

      if (entity.m_21525_()) {
         villager.m_21557_(true);
      }

      for (String tag : entity.m_19880_()) {
         villager.m_20049_(tag);
      }
   }

   public static boolean shouldGetConverted(Entity entity) {
      if (Config.getInstance().fractionOfVanillaVillages <= 0.0F) {
         return true;
      }

      int i = Nationality.get((ServerLevel)entity.m_9236_()).getRegionId(entity.m_20183_());
      return Math.floorMod(i, 100) >= Config.getInstance().fractionOfVanillaVillages * 100.0;
   }

   public boolean addVillager(Entity entity) {
      if (entity instanceof IVillagerEntity villagerEntity && !this.handlesSpawnReason(villagerEntity.getSpawnReason())) {
         return false;
      } else if (Config.getInstance().villagerDimensionBlacklist.contains(entity.m_20193_().m_46472_().m_135782_().toString())) {
         return false;
      } else if (Config.getInstance().overwriteOriginalVillagers
         && (
            entity.getClass().equals(Villager.class)
               || Config.getInstance().moddedVillagerWhitelist.contains(BuiltInRegistries.f_256780_.m_7981_(entity.m_6095_()).toString())
                  && entity instanceof Villager
         )
         && shouldGetConverted(entity)
         && !this.villagerSpawnQueue.contains(entity)) {
         return this.villagerSpawnQueue.add((Villager)entity);
      } else if (Config.getInstance().overwriteOriginalZombieVillagers
         && (
            entity.getClass().equals(ZombieVillager.class)
               || Config.getInstance().moddedZombieVillagerWhitelist.contains(BuiltInRegistries.f_256780_.m_7981_(entity.m_6095_()).toString())
                  && entity instanceof ZombieVillager
         )
         && Config.getInstance().fractionOfVanillaZombies < ((ZombieVillager)entity).m_217043_().m_188501_()
         && !this.zombieVillagerSpawnQueue.contains(entity)) {
         return this.zombieVillagerSpawnQueue.add((ZombieVillager)entity);
      } else {
         return Config.getInstance().overwriteAllZombiesWithZombieVillagers && entity.getClass().equals(Zombie.class) && !this.zombieSpawnList.contains(entity)
            ? this.zombieSpawnList.add((Zombie)entity)
            : false;
      }
   }

   private boolean handlesSpawnReason(MobSpawnType reason) {
      return Config.getInstance().allowedSpawnReasons.contains(reason.name().toLowerCase(Locale.ROOT));
   }

   public void convert(Villager villager) {
      this.villagerSpawnQueue.add(villager);
   }
}
