package forge.net.mca.server.world.data.villageComponents;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.resources.PoolUtil;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.Village;
import net.minecraft.server.level.ServerLevel;

public class VillageProcreationManager {
   private final Village village;

   public VillageProcreationManager(Village village) {
      this.village = village;
   }

   public void procreate(ServerLevel world) {
      if (!(world.f_46441_.m_188501_() >= Config.getInstance().villagerProcreationChancePerMinute)) {
         int population = this.village.getPopulation();
         int maxPopulation = this.village.getMaxPopulation();
         if (!(population >= maxPopulation * this.village.getPopulationThreshold())) {
            PoolUtil.pick(this.village.getResidents(world), world.f_46441_)
               .filter(villager -> villager.getGenetics().getGender() == Gender.FEMALE)
               .filter(villager -> world.f_46441_.m_188501_() < 1.0 / (FamilyTree.get(world).getOrCreate(villager).getChildren().count() + 0.1))
               .filter(villager -> villager.getRelationships().getPregnancy().tryStartGestation())
               .ifPresent(villager -> villager.getRelationships().getPartner().ifPresent(spouse -> {
                  if (Config.getInstance().villagerBirthNotification && spouse instanceof VillagerEntityMCA spouseVillager) {
                     this.village.broadCastMessage(world, "events.baby", villager, spouseVillager);
                  }
               }));
         }
      }
   }
}
