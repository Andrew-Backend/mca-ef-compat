package forge.net.mca.entity.ai.brain.tasks;

import forge.net.mca.ProfessionsMCA;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.server.world.data.Building;
import java.util.Optional;
import net.minecraft.core.BlockPos;

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
   protected Optional<BlockPos> getNextPosition(VillagerEntityMCA villager) {
      Optional<Building> b = this.getNearestBuilding(villager);
      if (b.isPresent()) {
         if (!b.get().containsPos(villager.m_20183_())) {
            return this.getRandomPositionIn(b.get(), villager.m_9236_());
         }

         if (villager.f_19797_ > this.lastMoodIncrease + 1200 && villager.getVillagerBrain().getMoodValue() < 0) {
            this.lastMoodIncrease = villager.f_19797_;
            villager.getVillagerBrain().modifyMoodValue(1);
         }
      }

      return Optional.empty();
   }
}
