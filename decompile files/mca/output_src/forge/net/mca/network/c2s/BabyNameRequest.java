package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.network.s2c.BabyNameResponse;
import forge.net.mca.resources.Names;
import net.minecraft.server.level.ServerPlayer;

public class BabyNameRequest implements Message {
   private static final long serialVersionUID = 4965378949498898298L;
   private final Gender gender;

   public BabyNameRequest(Gender gender) {
      this.gender = gender;
   }

   @Override
   public void receive(ServerPlayer player) {
      String name = Names.pickCitizenName(this.gender);
      NetworkHandler.sendToPlayer(new BabyNameResponse(name), player);
   }
}
