package quilt.net.mca.server;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.class_1297;
import net.minecraft.class_1314;
import net.minecraft.class_1641;
import net.minecraft.class_1642;
import net.minecraft.class_1646;
import net.minecraft.class_3218;
import net.minecraft.class_3730;
import net.minecraft.class_3852;
import net.minecraft.class_3854;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import quilt.net.mca.Config;
import quilt.net.mca.ducks.IVillagerEntity;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.VillagerFactory;
import quilt.net.mca.entity.ZombieVillagerEntityMCA;
import quilt.net.mca.entity.ZombieVillagerFactory;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.server.world.data.Nationality;

public class SpawnQueue {
   private static final SpawnQueue INSTANCE = new SpawnQueue();
   private final ConcurrentLinkedQueue<class_1646> villagerSpawnQueue = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<class_1641> zombieVillagerSpawnQueue = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<class_1642> zombieSpawnList = new ConcurrentLinkedQueue<>();

   public static SpawnQueue getInstance() {
      return INSTANCE;
   }

   public void tick() {
      class_1646 ve = this.villagerSpawnQueue.poll();
      if (ve != null) {
         ve.method_31472();
         VillagerEntityMCA villager = VillagerFactory.newVillager(ve.method_37908())
            .withName(ve.method_16914() ? ve.method_5477().getString() : null)
            .withGender(Gender.getRandom())
            .withAge(ve.method_5618())
            .withPosition(ve)
            .withType(ve.method_7231().method_16919())
            .withProfession(ve.method_7231().method_16924(), ve.method_7231().method_16925(), ve.method_8264())
            .spawn(((IVillagerEntity)ve).getSpawnReason());
         this.copyPastaIntensifies(villager, ve);
      }

      class_1641 zve = this.zombieVillagerSpawnQueue.poll();
      if (zve != null) {
         zve.method_31472();
         ZombieVillagerEntityMCA villager = ZombieVillagerFactory.newVillager(zve.method_37908())
            .withName(zve.method_16914() ? zve.method_5477().getString() : null)
            .withGender(Gender.getRandom())
            .withPosition(zve)
            .withType(zve.method_7231().method_16919())
            .withProfession(zve.method_7231().method_16924(), zve.method_7231().method_16925())
            .spawn(((IVillagerEntity)zve).getSpawnReason());
         this.copyPastaIntensifies(villager, zve);
      }

      class_1642 ze = this.zombieSpawnList.poll();
      if (ze != null) {
         ze.method_31472();
         ZombieVillagerEntityMCA villager = ZombieVillagerFactory.newVillager(ze.method_37908())
            .withName(ze.method_16914() ? ze.method_5477().getString() : null)
            .withGender(Gender.getRandom())
            .withPosition(ze)
            .withType(class_3854.method_16930(ze.method_37908().method_23753(ze.method_24515())))
            .withProfession(class_7923.field_41195.method_10240(ze.method_6051()).<class_3852>map(class_6880::comp_349).orElse(class_3852.field_17051))
            .spawn(class_3730.field_16459);
         this.copyPastaIntensifies(villager, ze);
      }
   }

   private void copyPastaIntensifies(class_1314 villager, class_1314 entity) {
      if (entity.method_5947()) {
         villager.method_5971();
      }

      if (entity.method_5655()) {
         villager.method_5684(true);
      }

      if (entity.method_5987()) {
         villager.method_5977(true);
      }

      for (String tag : entity.method_5752()) {
         villager.method_5780(tag);
      }
   }

   public static boolean shouldGetConverted(class_1297 entity) {
      if (Config.getInstance().fractionOfVanillaVillages <= 0.0F) {
         return true;
      }

      int i = Nationality.get((class_3218)entity.method_37908()).getRegionId(entity.method_24515());
      return Math.floorMod(i, 100) >= Config.getInstance().fractionOfVanillaVillages * 100.0;
   }

   public boolean addVillager(class_1297 entity) {
      if (entity instanceof IVillagerEntity villagerEntity && !this.handlesSpawnReason(villagerEntity.getSpawnReason())) {
         return false;
      } else if (Config.getInstance().villagerDimensionBlacklist.contains(entity.method_5770().method_27983().method_29177().toString())) {
         return false;
      } else if (Config.getInstance().overwriteOriginalVillagers
         && (
            entity.getClass().equals(class_1646.class)
               || Config.getInstance().moddedVillagerWhitelist.contains(class_7923.field_41177.method_10221(entity.method_5864()).toString())
                  && entity instanceof class_1646
         )
         && shouldGetConverted(entity)
         && !this.villagerSpawnQueue.contains(entity)) {
         return this.villagerSpawnQueue.add((class_1646)entity);
      } else if (Config.getInstance().overwriteOriginalZombieVillagers
         && (
            entity.getClass().equals(class_1641.class)
               || Config.getInstance().moddedZombieVillagerWhitelist.contains(class_7923.field_41177.method_10221(entity.method_5864()).toString())
                  && entity instanceof class_1641
         )
         && Config.getInstance().fractionOfVanillaZombies < ((class_1641)entity).method_6051().method_43057()
         && !this.zombieVillagerSpawnQueue.contains(entity)) {
         return this.zombieVillagerSpawnQueue.add((class_1641)entity);
      } else {
         return Config.getInstance().overwriteAllZombiesWithZombieVillagers
               && entity.getClass().equals(class_1642.class)
               && !this.zombieSpawnList.contains(entity)
            ? this.zombieSpawnList.add((class_1642)entity)
            : false;
      }
   }

   private boolean handlesSpawnReason(class_3730 reason) {
      return Config.getInstance().allowedSpawnReasons.contains(reason.name().toLowerCase(Locale.ROOT));
   }

   public void convert(class_1646 villager) {
      this.villagerSpawnQueue.add(villager);
   }
}
