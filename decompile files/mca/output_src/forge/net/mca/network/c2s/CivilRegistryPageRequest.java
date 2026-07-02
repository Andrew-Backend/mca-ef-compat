package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.CivilRegistryResponse;
import forge.net.mca.server.world.data.PlayerSaveData;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.server.world.data.VillageManager;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

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
   public void receive(ServerPlayer player) {
      PlayerSaveData.get(player)
         .getLastSeenVillage(VillageManager.get((ServerLevel)player.m_9236_()))
         .flatMap(Village::getCivilRegistry)
         .ifPresentOrElse(c -> {
            List<Component> page = c.getPage(this.from, this.to);
            NetworkHandler.sendToPlayer(new CivilRegistryResponse(this.index, page), player);
         }, () -> NetworkHandler.sendToPlayer(new CivilRegistryResponse(this.index, List.of(Component.m_237115_("civil_registry.empty"))), player));
   }
}
