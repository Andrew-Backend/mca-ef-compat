package quilt.net.mca.network.c2s;

import java.util.UUID;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.entity.VillagerEntityMCA;

public class InteractionCloseRequest implements Message {
   private static final long serialVersionUID = 5410526074172819931L;
   private final UUID villagerUUID;

   public InteractionCloseRequest(UUID uuid) {
      this.villagerUUID = uuid;
   }

   @Override
   public void receive(class_3222 player) {
      if (player.method_51469().method_14190(this.villagerUUID) instanceof VillagerEntityMCA villager) {
         villager.getInteractions().stopInteracting();
      }
   }
}
