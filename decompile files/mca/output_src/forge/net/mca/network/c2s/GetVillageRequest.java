package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.GetVillageFailedResponse;
import forge.net.mca.network.s2c.GetVillageResponse;
import forge.net.mca.resources.Rank;
import forge.net.mca.resources.Tasks;
import forge.net.mca.server.world.data.GraveyardManager;
import forge.net.mca.server.world.data.Village;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;

public class GetVillageRequest implements Message {
   private static final long serialVersionUID = -1302412553466016247L;

   @Override
   public void receive(ServerPlayer player) {
      Optional<Village> village = Village.findNearest(player);
      if (village.isPresent()) {
         GraveyardManager.get(player.m_284548_()).reportToVillageManager(player);
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
