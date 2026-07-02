package fabric.net.mca.entity.ai.chatAI.modules;

import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.server.world.data.Building;
import fabric.net.mca.server.world.data.Village;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.class_3222;

public class VillageModule {
   private static final Map<String, String> nameExceptions = Map.of(
      "fishermans_hut",
      "fisherman's hut",
      "weaving_mill",
      "weaving mill",
      "big_house",
      "big house",
      "music_store",
      "music store",
      "town_center",
      "town center",
      "building",
      "",
      "blocked",
      "",
      "house",
      ""
   );

   public static void apply(List<String> input, VillagerEntityMCA villager, class_3222 player) {
      Optional<Village> village = villager.getResidency().getHomeVillage();
      String biome = villager.method_37908().method_23753(villager.method_24515()).method_40230().map(v -> v.method_29177().method_12832()).orElse("plains");
      String size = "small";
      if (village.isPresent()) {
         int population = village.get().getPopulation();
         if (population > 45) {
            size = "huge";
         } else if (population > 30) {
            size = "large";
         } else if (population > 15) {
            size = "medium-sized";
         }
      }

      input.add("$villager lives in a " + size + ", medieval village in a " + biome.replace("_", " ") + " biom. ");
      village.ifPresent(
         v -> {
            String buildings = v.getBuildings()
               .values()
               .stream()
               .map(Building::getType)
               .map(b -> nameExceptions.getOrDefault(b, b))
               .filter(s -> !s.isEmpty())
               .distinct()
               .collect(Collectors.joining(", "));
            if (!buildings.isEmpty()) {
               input.add("The village has a " + buildings + ". ");
            }
         }
      );
   }
}
