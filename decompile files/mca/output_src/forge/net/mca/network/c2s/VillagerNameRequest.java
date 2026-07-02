package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.ai.relationship.Gender;
import forge.net.mca.network.s2c.VillagerNameResponse;
import forge.net.mca.resources.Names;
import net.minecraft.server.level.ServerPlayer;

public class VillagerNameRequest implements Message {
   private static final long serialVersionUID = -7850240766540487322L;
   private final Gender gender;

   public VillagerNameRequest(Gender gender) {
      this.gender = gender;
   }

   @Override
   public void receive(ServerPlayer player) {
      String name = Names.pickCitizenName(this.gender);
      NetworkHandler.sendToPlayer(new VillagerNameResponse(name), player);
   }
}
