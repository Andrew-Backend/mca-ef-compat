package fabric.net.mca.entity.ai.brain.tasks;

import fabric.net.mca.ProfessionsMCA;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.server.world.data.Building;
import java.util.Optional;
import net.minecraft.class_2338;

public class EnterFavoredBuildingTask extends EnterBuildingTask {
   private int lastMoodIncrease = 0;
   private static final int TICKS_PER_MOOD = 1200;

   public EnterFavoredBuildingTask(float speed) {
      super("", speed);
   }

   @Override
   public String getBuilding(VillagerEntityMCA villager) {
      String building = villager.getVillagerBrain().getMood().getBuilding();
      return building != null ? building : ProfessionsMCA.getFavoredBuilding(villager.getProfession());
   }

   @Override
   protected Optional<class_2338> getNextPosition(VillagerEntityMCA villager) {
      Optional<Building> b = this.getNearestBuilding(villager);
      if (b.isPresent()) {
         if (!b.get().containsPos(villager.method_24515())) {
            return this.getRandomPositionIn(b.get(), villager.method_37908());
         }

         if (villager.field_6012 > this.lastMoodIncrease + 1200 && villager.getVillagerBrain().getMoodValue() < 0) {
            this.lastMoodIncrease = villager.field_6012;
            villager.getVillagerBrain().modifyMoodValue(1);
         }
      }

      return Optional.empty();
   }
}
