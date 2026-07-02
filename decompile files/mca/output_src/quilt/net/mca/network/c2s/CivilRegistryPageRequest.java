package quilt.net.mca.network.c2s;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.s2c.CivilRegistryResponse;
import quilt.net.mca.server.world.data.PlayerSaveData;
import quilt.net.mca.server.world.data.Village;
import quilt.net.mca.server.world.data.VillageManager;

public class CivilRegistryPageRequest implements Message {
   private static final long serialVersionUID = 7108115056986169352L;
   private final int index;
   private final int from;
   private final int to;

   public CivilRegistryPageRequest(int index, int from, int to) {
      this.index = index;
      this.from = from;
      this.to = to;
   }

   @Override
   public void receive(class_3222 player) {
      PlayerSaveData.get(player)
         .getLastSeenVillage(VillageManager.get((class_3218)player.method_37908()))
         .flatMap(Village::getCivilRegistry)
         .ifPresentOrElse(c -> {
            List<class_2561> page = c.getPage(this.from, this.to);
            NetworkHandler.sendToPlayer(new CivilRegistryResponse(this.index, page), player);
         }, () -> NetworkHandler.sendToPlayer(new CivilRegistryResponse(this.index, List.of(class_2561.method_43471("civil_registry.empty"))), player));
   }
}
