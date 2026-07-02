package quilt.net.mca.network.c2s;

import java.util.Optional;
import java.util.Set;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.s2c.GetVillageFailedResponse;
import quilt.net.mca.network.s2c.GetVillageResponse;
import quilt.net.mca.resources.Rank;
import quilt.net.mca.resources.Tasks;
import quilt.net.mca.server.world.data.GraveyardManager;
import quilt.net.mca.server.world.data.Village;

public class GetVillageRequest implements Message {
   private static final long serialVersionUID = -1302412553466016247L;

   @Override
   public void receive(class_3222 player) {
      Optional<Village> village = Village.findNearest(player);
      if (village.isPresent()) {
         GraveyardManager.get(player.method_51469()).reportToVillageManager(player);
         village.get().updateMaxPopulation();
         int reputation = village.get().getReputation(player);
         boolean isVillage = village.get().isVillage();
         Rank rank = Tasks.getRank(village.get(), player);
         Set<String> ids = Tasks.getCompletedIds(village.get(), player);
         NetworkHandler.sendToPlayer(new GetVillageResponse(village.get(), rank, reputation, isVillage, ids), player);
      } else {
         NetworkHandler.sendToPlayer(new GetVillageFailedResponse(), player);
      }
   }
}
