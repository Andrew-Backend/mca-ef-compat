package quilt.net.mca.server.world.data.villageComponents;

import net.minecraft.class_3218;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.resources.PoolUtil;
import quilt.net.mca.server.world.data.FamilyTree;
import quilt.net.mca.server.world.data.Village;

public class VillageProcreationManager {
   private final Village village;

   public VillageProcreationManager(Village village) {
      this.village = village;
   }

   public void procreate(class_3218 world) {
      if (!(world.field_9229.method_43057() >= Config.getInstance().villagerProcreationChancePerMinute)) {
         int population = this.village.getPopulation();
         int maxPopulation = this.village.getMaxPopulation();
         if (!(population >= maxPopulation * this.village.getPopulationThreshold())) {
            PoolUtil.pick(this.village.getResidents(world), world.field_9229)
               .filter(villager -> villager.getGenetics().getGender() == Gender.FEMALE)
               .filter(villager -> world.field_9229.method_43057() < 1.0 / (FamilyTree.get(world).getOrCreate(villager).getChildren().count() + 0.1))
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
