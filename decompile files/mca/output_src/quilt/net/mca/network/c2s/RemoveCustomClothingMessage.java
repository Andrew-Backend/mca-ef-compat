package quilt.net.mca.network.c2s;

import net.minecraft.class_2960;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.server.world.data.CustomClothingManager;

public class RemoveCustomClothingMessage implements Message {
   private static final long serialVersionUID = 8751716740579401345L;
   final RemoveCustomClothingMessage.Type type;
   final String identifier;

   public RemoveCustomClothingMessage(RemoveCustomClothingMessage.Type type, class_2960 identifier) {
      this.type = type;
      this.identifier = String.valueOf(identifier);
   }

   @Override
   public void receive(class_3222 player) {
      if (this.type == RemoveCustomClothingMessage.Type.CLOTHING) {
         CustomClothingManager.getClothing().removeEntry(this.identifier);
      } else if (this.type == RemoveCustomClothingMessage.Type.HAIR) {
         CustomClothingManager.getHair().removeEntry(this.identifier);
      }
   }

   public enum Type {
      CLOTHING,
      HAIR;
   }
}
