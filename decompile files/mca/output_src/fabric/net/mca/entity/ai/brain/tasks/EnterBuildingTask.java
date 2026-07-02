package fabric.net.mca.entity.ai.brain.tasks;

import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.server.world.data.Building;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4215;
import net.minecraft.class_5819;

public class EnterBuildingTask extends class_4097<VillagerEntityMCA> {
   private final String building;
   private final float speed;

   public EnterBuildingTask(String building, float speed) {
      super(
         Map.of(class_4140.field_22355, class_4141.field_18457, class_4140.field_18445, class_4141.field_18457, class_4140.field_18446, class_4141.field_18458)
      );
      this.building = building;
      this.speed = speed;
   }

   protected void run(class_3218 serverWorld, VillagerEntityMCA villager, long l) {
      Optional<class_2338> blockPos = this.getNextPosition(villager);
      blockPos.ifPresent(pos -> class_4215.method_24561(villager, pos, this.speed, 1));
   }

   protected Optional<Building> getNearestBuilding(VillagerEntityMCA villager) {
      return villager.getResidency()
         .getHomeVillage()
         .flatMap(
            buildings -> buildings.getBuildings()
               .values()
               .stream()
               .filter(a -> a.getType().equals(this.getBuilding(villager)))
               .min(Comparator.comparingInt(a -> a.getCenter().method_19455(villager.method_24515())))
         );
   }

   protected Optional<class_2338> getRandomPositionIn(Building b, class_1937 world) {
      if (b.getBuildingType().grouped()) {
         return Optional.ofNullable(b.getCenter());
      }

      class_5819 r = world.method_8409();
      class_2338 pos0 = b.getPos0();
      class_2338 pos1 = b.getPos1();
      class_2338 diff = pos1.method_10059(pos0);
      int margin = 2;

      for (int attempt = 0; attempt < 16; attempt++) {
         class_2338 p = pos0.method_10081(
            new class_2338(
               r.method_43048(Math.max(1, diff.method_10263() - margin * 2)) + margin,
               r.method_43048(Math.max(1, diff.method_10264() - margin * 2)) + margin,
               r.method_43048(Math.max(1, diff.method_10260() - margin * 2)) + margin
            )
         );
         if (!world.method_8311(p)) {
            return Optional.of(p);
         }
      }

      return Optional.empty();
   }

   protected Optional<class_2338> getNextPosition(VillagerEntityMCA villager) {
      Optional<Building> b = this.getNearestBuilding(villager);
      return b.isPresent() && !b.get().containsPos(villager.method_24515()) ? this.getRandomPositionIn(b.get(), villager.method_37908()) : Optional.empty();
   }

   public String getBuilding(VillagerEntityMCA villager) {
      return this.building;
   }
}
