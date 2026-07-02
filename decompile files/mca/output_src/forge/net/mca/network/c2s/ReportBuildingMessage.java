package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.server.world.data.Building;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.server.world.data.VillageManager;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

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
   public void receive(ServerPlayer player) {
      VillageManager villages = VillageManager.get(player.m_284548_());
      switch (this.action) {
         case AUTO_SCAN:
            villages.findNearestVillage(player).ifPresent(Village::toggleAutoScan);
            break;
         case ADD_ROOM:
         case ADD:
            Building.validationResult result = villages.processBuilding(player.m_20183_(), true, this.action == ReportBuildingMessage.Action.ADD_ROOM);
            player.m_5661_(Component.m_237115_("blueprint.scan." + result.name().toLowerCase(Locale.ENGLISH)), true);
            break;
         case REMOVE:
         case FORCE_TYPE:
            Optional<Village> village = villages.findNearestVillage(player);
            Optional<Building> building = village.flatMap(
               v -> v.getBuildings()
                  .values()
                  .stream()
                  .filter(b -> b.containsPos(player.m_20183_()))
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
            }, () -> player.m_5661_(Component.m_237115_("blueprint.noBuilding"), true));
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
