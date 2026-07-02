package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.ai.relationship.Gender;
import fabric.net.mca.network.s2c.BabyNameResponse;
import fabric.net.mca.resources.Names;
import net.minecraft.class_3222;

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
