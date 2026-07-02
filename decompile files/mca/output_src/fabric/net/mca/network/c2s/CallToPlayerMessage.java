package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.entity.VillagerEntityMCA;
import java.util.UUID;
import net.minecraft.class_3222;

public class CallToPlayerMessage implements Message {
   private static final long serialVersionUID = 2556280539773400447L;
   private final UUID uuid;

   public CallToPlayerMessage(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void receive(class_3222 player) {
      if (player.method_51469().method_14190(this.uuid) instanceof VillagerEntityMCA v) {
         if (v.method_6113()) {
            v.method_18400();
         }

         v.method_5848();
         v.method_5814(player.method_23317(), player.method_23318(), player.method_23321());
      }
   }
}
