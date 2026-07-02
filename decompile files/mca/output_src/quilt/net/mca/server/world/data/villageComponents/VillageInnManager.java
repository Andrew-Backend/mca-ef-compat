package quilt.net.mca.server.world.data.villageComponents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_1299;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3730;
import net.minecraft.class_3852;
import net.minecraft.class_3989;
import quilt.net.mca.Config;
import quilt.net.mca.ProfessionsMCA;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.resources.Names;
import quilt.net.mca.server.world.data.Village;

public class VillageInnManager {
   private final Village village;

   public VillageInnManager(Village village) {
      this.village = village;
   }

   public void updateInn(class_3218 world) {
      this.village.getBuildingsOfType("inn").forEach(b -> {
         if (world.field_9229.method_43057() < Config.getInstance().adventurerAtInnChancePerMinute) {
            List<class_2338> values = new ArrayList<>(b.getBlocks().values().stream().flatMap(Collection::stream).toList());
            Collections.shuffle(values);

            for (class_2338 p : values) {
               if (this.trySpawnAdventurer(world, p.method_10084())) {
                  break;
               }
            }
         }
      });
   }

   private boolean doesNotSuffocateAt(class_1922 world, class_2338 pos) {
      for (class_2338 blockPos : class_2338.method_10097(pos, pos.method_10084())) {
         if (!world.method_8320(blockPos).method_26220(world, blockPos).method_1110()) {
            return false;
         }
      }

      return true;
   }

   private boolean trySpawnAdventurer(class_3218 world, class_2338 blockPos) {
      if (!world.method_37118(blockPos)) {
         return true;
      }

      String name = null;
      if (this.doesNotSuffocateAt(world, blockPos)) {
         int i = world.field_9229.method_43048(10);
         if (i == 0 && Config.getInstance().innSpawnsWanderingTraders) {
            class_3989 trader = (class_3989)class_1299.field_17713.method_47821(world, blockPos, class_3730.field_16467);
            if (trader != null) {
               name = trader.method_5477().getString();
               trader.method_18013(Config.getInstance().adventurerStayTime);
            }
         } else if (i == 1 && Config.getInstance().innSpawnsCultists) {
            VillagerEntityMCA adventurer = this.spawnInnVillager(world, blockPos, Gender.getRandom());
            if (adventurer != null) {
               name = adventurer.method_5477().getString();
               adventurer.setProfession((class_3852)ProfessionsMCA.CULTIST.get());
               adventurer.setDespawnDelay(Config.getInstance().adventurerStayTime);
            }
         } else if (Config.getInstance().innSpawnsAdventurers) {
            VillagerEntityMCA adventurer = this.spawnInnVillager(world, blockPos, Gender.getRandom());
            if (adventurer != null) {
               name = adventurer.method_5477().getString();
               adventurer.setProfession((class_3852)ProfessionsMCA.ADVENTURER.get());
               adventurer.setDespawnDelay(Config.getInstance().adventurerStayTime);
            }
         }

         if (name != null) {
            if (Config.getInstance().innArrivalNotification) {
               this.village.broadCastMessage(world, "events.arrival.inn", name);
            }

            return true;
         }
      }

      return false;
   }

   private VillagerEntityMCA spawnInnVillager(class_3218 world, class_2338 blockPos, Gender gender) {
      VillagerEntityMCA adventurer = (VillagerEntityMCA)gender.getVillagerType().method_47821(world, blockPos, class_3730.field_16467);
      if (adventurer != null) {
         adventurer.method_5665(class_2561.method_43470(Names.pickCitizenName(gender)));
      }

      return adventurer;
   }
}
