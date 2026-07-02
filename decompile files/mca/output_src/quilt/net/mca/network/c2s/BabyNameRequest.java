package quilt.net.mca.network.c2s;

import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.ai.relationship.Gender;
import quilt.net.mca.network.s2c.BabyNameResponse;
import quilt.net.mca.resources.Names;

public class BabyNameRequest implements Message {
   private static final long serialVersionUID = 4965378949498898298L;
   private final Gender gender;

   public BabyNameRequest(Gender gender) {
      this.gender = gender;
   }

   @Override
   public void receive(class_3222 player) {
      String name = Names.pickCitizenName(this.gender);
      NetworkHandler.sendToPlayer(new BabyNameResponse(name), player);
   }
}
