package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.entity.VillagerEntityMCA;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class CallToPlayerMessage implements Message {
   private static final long serialVersionUID = 2556280539773400447L;
   private final UUID uuid;

   public CallToPlayerMessage(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void receive(ServerPlayer player) {
      if (player.m_284548_().m_8791_(this.uuid) instanceof VillagerEntityMCA v) {
         if (v.m_5803_()) {
            v.m_5796_();
         }

         v.m_8127_();
         v.m_6034_(player.m_20185_(), player.m_20186_(), player.m_20189_());
      }
   }
}
