package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.server.world.data.Building;
import fabric.net.mca.server.world.data.Village;
import fabric.net.mca.server.world.data.VillageManager;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_3222;

public class ReportBuildingMessage implements Message {
   private static final long serialVersionUID = 3510050513221709603L;
   private final ReportBuildingMessage.Action action;
   private final String data;

   public ReportBuildingMessage(ReportBuildingMessage.Action action, String data) {
      this.action = action;
      this.data = data;
   }

   public ReportBuildingMessage(ReportBuildingMessage.Action action) {
      this(action, null);
   }

   @Override
   public void receive(class_3222 player) {
      VillageManager villages = VillageManager.get(player.method_51469());
      switch (this.action) {
         case AUTO_SCAN:
            villages.findNearestVillage(player).ifPresent(Village::toggleAutoScan);
            break;
         case ADD_ROOM:
         case ADD:
            Building.validationResult result = villages.processBuilding(player.method_24515(), true, this.action == ReportBuildingMessage.Action.ADD_ROOM);
            player.method_7353(class_2561.method_43471("blueprint.scan." + result.name().toLowerCase(Locale.ENGLISH)), true);
            break;
         case REMOVE:
         case FORCE_TYPE:
            Optional<Village> village = villages.findNearestVillage(player);
            Optional<Building> building = village.flatMap(
               v -> v.getBuildings()
                  .values()
                  .stream()
                  .filter(b -> b.containsPos(player.method_24515()))
                  .filter(b -> this.action != ReportBuildingMessage.Action.FORCE_TYPE || !b.getBuildingType().grouped())
                  .findAny()
            );
            building.ifPresentOrElse(b -> {
               if (this.action == ReportBuildingMessage.Action.FORCE_TYPE) {
                  if (b.getType().equals(this.data)) {
                     b.setTypeForced(false);
                     b.determineType();
                  } else {
                     b.setTypeForced(true);
                     b.setType(this.data);
                  }
               } else {
                  village.get().removeBuilding(b.getId());
               }
            }, () -> player.method_7353(class_2561.method_43471("blueprint.noBuilding"), true));
            break;
         case FULL_SCAN:
            villages.findNearestVillage(player)
               .ifPresent(
                  buildings -> buildings.getBuildings()
                     .values()
                     .stream()
                     .toList()
                     .forEach(b -> villages.processBuilding(b.getCenter(), true, b.isStrictScan()))
               );
      }
   }

   public enum Action {
      AUTO_SCAN,
      ADD_ROOM,
      ADD,
      REMOVE,
      FORCE_TYPE,
      FULL_SCAN;
   }
}
