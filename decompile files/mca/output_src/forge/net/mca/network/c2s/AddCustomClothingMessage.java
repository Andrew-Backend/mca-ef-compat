package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.resources.data.skin.Clothing;
import forge.net.mca.resources.data.skin.Hair;
import forge.net.mca.resources.data.skin.SkinListEntry;
import forge.net.mca.server.world.data.CustomClothingManager;
import net.minecraft.server.level.ServerPlayer;

public class AddCustomClothingMessage implements Message {
   private static final long serialVersionUID = 4620788389788045910L;
   final SkinListEntry entry;

   public AddCustomClothingMessage(SkinListEntry entry) {
      this.entry = entry;
   }

   @Override
   public void receive(ServerPlayer player) {
      if (this.entry instanceof Clothing clothing) {
         CustomClothingManager.getClothing().addEntry(this.entry.getIdentifier(), clothing);
      } else if (this.entry instanceof Hair hair) {
         CustomClothingManager.getHair().addEntry(this.entry.getIdentifier(), hair);
      }
   }
}
