package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.server.world.data.CustomClothingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class RemoveCustomClothingMessage implements Message {
   private static final long serialVersionUID = 8751716740579401345L;
   final RemoveCustomClothingMessage.Type type;
   final String identifier;

   public RemoveCustomClothingMessage(RemoveCustomClothingMessage.Type type, ResourceLocation identifier) {
      this.type = type;
      this.identifier = String.valueOf(identifier);
   }

   @Override
   public void receive(ServerPlayer player) {
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
