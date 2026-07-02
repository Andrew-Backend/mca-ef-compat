package forge.net.mca.entity.ai.brain.tasks;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.server.world.data.Building;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.Level;

public class EnterBuildingTask extends Behavior<VillagerEntityMCA> {
   private final String building;
   private final float speed;

   public EnterBuildingTask(String building, float speed) {
      super(
         Map.of(
            MemoryModuleType.f_26372_,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.f_26370_,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.f_26371_,
            MemoryStatus.REGISTERED
         )
      );
      this.building = building;
      this.speed = speed;
   }

   protected void run(ServerLevel serverWorld, VillagerEntityMCA villager, long l) {
      Optional<BlockPos> blockPos = this.getNextPosition(villager);
      blockPos.ifPresent(pos -> BehaviorUtils.m_22617_(villager, pos, this.speed, 1));
   }

   protected Optional<Building> getNearestBuilding(VillagerEntityMCA villager) {
      return villager.getResidency()
         .getHomeVillage()
         .flatMap(
            buildings -> buildings.getBuildings()
               .values()
               .stream()
               .filter(a -> a.getType().equals(this.getBuilding(villager)))
               .min(Comparator.comparingInt(a -> a.getCenter().m_123333_(villager.m_20183_())))
         );
   }

   protected Optional<BlockPos> getRandomPositionIn(Building b, Level world) {
      if (b.getBuildingType().grouped()) {
         return Optional.ofNullable(b.getCenter());
      }

      RandomSource r = world.m_213780_();
      BlockPos pos0 = b.getPos0();
      BlockPos pos1 = b.getPos1();
      BlockPos diff = pos1.m_121996_(pos0);
      int margin = 2;

      for (int attempt = 0; attempt < 16; attempt++) {
         BlockPos p = pos0.m_121955_(
            new BlockPos(
               r.m_188503_(Math.max(1, diff.m_123341_() - margin * 2)) + margin,
               r.m_188503_(Math.max(1, diff.m_123342_() - margin * 2)) + margin,
               r.m_188503_(Math.max(1, diff.m_123343_() - margin * 2)) + margin
            )
         );
         if (!world.m_45527_(p)) {
            return Optional.of(p);
         }
      }

      return Optional.empty();
   }

   protected Optional<BlockPos> getNextPosition(VillagerEntityMCA villager) {
      Optional<Building> b = this.getNearestBuilding(villager);
      return b.isPresent() && !b.get().containsPos(villager.m_20183_()) ? this.getRandomPositionIn(b.get(), villager.m_9236_()) : Optional.empty();
   }

   public String getBuilding(VillagerEntityMCA villager) {
      return this.building;
   }
}
