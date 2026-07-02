package quilt.net.mca.server.world.data.villageComponents;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_3218;
import net.minecraft.class_3852;
import quilt.net.mca.Config;
import quilt.net.mca.ProfessionsMCA;
import quilt.net.mca.entity.EquipmentSet;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.server.world.data.Village;

public class VillageGuardsManager {
   private final Village village;

   public VillageGuardsManager(Village village) {
      this.village = village;
   }

   public void spawnGuards(class_3218 world) {
      int guardCapacity = (int)Math.ceil(this.village.getPopulation() * Config.getInstance().guardSpawnFraction);
      int guards = 0;
      int citizen = 0;
      List<VillagerEntityMCA> villagers = this.village.getResidents(world);
      List<VillagerEntityMCA> nonGuards = new LinkedList<>();

      for (VillagerEntityMCA villager : villagers) {
         if (villager.isGuard()) {
            guards++;
         } else {
            if (!villager.method_6109() && !villager.isProfessionImportant() && villager.method_19269() == 0 && villager.method_7231().method_16925() <= 1) {
               nonGuards.add(villager);
            }

            citizen++;
         }
      }

      guards = (int)(guards + Math.ceil((this.village.getPopulation() - guards - citizen) * Config.getInstance().guardSpawnFraction));
      if (nonGuards.size() > 0 && guards < guardCapacity) {
         VillagerEntityMCA villager = nonGuards.get(world.field_9229.method_43048(nonGuards.size()));
         villager.setProfession(guards % 2 == 0 ? (class_3852)ProfessionsMCA.GUARD.get() : (class_3852)ProfessionsMCA.ARCHER.get());
      }
   }

   public EquipmentSet getGuardEquipment(class_3852 profession, class_1268 dominantHand) {
      if (profession == ProfessionsMCA.ARCHER.get()) {
         if (this.village.hasBuilding("armory")) {
            return this.village.hasBuilding("blacksmith")
               ? getEquipmentFor(dominantHand, EquipmentSet.ARCHER_2, EquipmentSet.ARCHER_2_LEFT)
               : getEquipmentFor(dominantHand, EquipmentSet.ARCHER_1, EquipmentSet.ARCHER_1_LEFT);
         } else {
            return getEquipmentFor(dominantHand, EquipmentSet.ARCHER_0, EquipmentSet.ARCHER_0_LEFT);
         }
      } else if (this.village.hasBuilding("armory")) {
         return this.village.hasBuilding("blacksmith") ? EquipmentSet.GUARD_2 : EquipmentSet.GUARD_1;
      } else {
         return getEquipmentFor(dominantHand, EquipmentSet.GUARD_0, EquipmentSet.GUARD_0_LEFT);
      }
   }

   public static EquipmentSet getEquipmentFor(class_1268 dominantHand, EquipmentSet rightSet, EquipmentSet leftSet) {
      return dominantHand == class_1268.field_5810 && leftSet != null ? leftSet : rightSet;
   }
}
