package quilt.net.mca.network.c2s;

import java.util.UUID;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.entity.VillagerLike;

public class InteractionVillagerMessage implements Message {
   private static final long serialVersionUID = 2563941495766992462L;
   private final String command;
   private final UUID villagerUUID;

   public InteractionVillagerMessage(String command, UUID villagerUUID) {
      this.command = command.replace("gui.button.", "");
      this.villagerUUID = villagerUUID;
   }

   @Override
   public void receive(class_3222 player) {
      if (player.method_51469().method_14190(this.villagerUUID) instanceof VillagerLike<?> villager && villager.getInteractions().handle(player, this.command)) {
         villager.getInteractions().stopInteracting();
      }
   }
}
