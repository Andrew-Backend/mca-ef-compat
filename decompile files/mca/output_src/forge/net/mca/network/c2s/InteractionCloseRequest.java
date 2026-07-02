package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.entity.VillagerEntityMCA;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class InteractionCloseRequest implements Message {
   private static final long serialVersionUID = 5410526074172819931L;
   private final UUID villagerUUID;

   public InteractionCloseRequest(UUID uuid) {
      this.villagerUUID = uuid;
   }

   @Override
   public void receive(ServerPlayer player) {
      if (player.m_284548_().m_8791_(this.villagerUUID) instanceof VillagerEntityMCA villager) {
         villager.getInteractions().stopInteracting();
      }
   }
}
