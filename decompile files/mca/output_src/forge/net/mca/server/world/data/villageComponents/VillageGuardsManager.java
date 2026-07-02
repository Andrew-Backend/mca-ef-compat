package forge.net.mca.server.world.data.villageComponents;

import forge.net.mca.Config;
import forge.net.mca.ProfessionsMCA;
import forge.net.mca.entity.EquipmentSet;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.server.world.data.Village;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.VillagerProfession;

public class VillageGuardsManager {
   private final Village village;

   public VillageGuardsManager(Village village) {
      this.village = village;
   }

   public void spawnGuards(ServerLevel world) {
      int guardCapacity = (int)Math.ceil(this.village.getPopulation() * Config.getInstance().guardSpawnFraction);
      int guards = 0;
      int citizen = 0;
      List<VillagerEntityMCA> villagers = this.village.getResidents(world);
      List<VillagerEntityMCA> nonGuards = new LinkedList<>();

      for (VillagerEntityMCA villager : villagers) {
         if (villager.isGuard()) {
            guards++;
         } else {
            if (!villager.m_6162_() && !villager.isProfessionImportant() && villager.m_7809_() == 0 && villager.m_7141_().m_35576_() <= 1) {
               nonGuards.add(villager);
            }

            citizen++;
         }
      }

      guards = (int)(guards + Math.ceil((this.village.getPopulation() - guards - citizen) * Config.getInstance().guardSpawnFraction));
      if (nonGuards.size() > 0 && guards < guardCapacity) {
         VillagerEntityMCA villager = nonGuards.get(world.f_46441_.m_188503_(nonGuards.size()));
         villager.setProfession(guards % 2 == 0 ? (VillagerProfession)ProfessionsMCA.GUARD.get() : (VillagerProfession)ProfessionsMCA.ARCHER.get());
      }
   }

   public EquipmentSet getGuardEquipment(VillagerProfession profession, InteractionHand dominantHand) {
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

   public static EquipmentSet getEquipmentFor(InteractionHand dominantHand, EquipmentSet rightSet, EquipmentSet leftSet) {
      return dominantHand == InteractionHand.OFF_HAND && leftSet != null ? leftSet : rightSet;
   }
}
