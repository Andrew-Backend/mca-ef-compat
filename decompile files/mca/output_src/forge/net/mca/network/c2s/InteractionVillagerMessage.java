package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.entity.VillagerLike;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class InteractionVillagerMessage implements Message {
   private static final long serialVersionUID = 2563941495766992462L;
   private final String command;
   private final UUID villagerUUID;

   public InteractionVillagerMessage(String command, UUID villagerUUID) {
      this.command = command.replace("gui.button.", "");
      this.villagerUUID = villagerUUID;
   }

   @Override
   public void receive(ServerPlayer player) {
      if (player.m_284548_().m_8791_(this.villagerUUID) instanceof VillagerLike<?> villager && villager.getInteractions().handle(player, this.command)) {
         villager.getInteractions().stopInteracting();
      }
   }
}
