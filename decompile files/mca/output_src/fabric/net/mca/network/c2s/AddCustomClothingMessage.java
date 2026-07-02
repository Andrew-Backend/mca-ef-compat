package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.resources.data.skin.Clothing;
import fabric.net.mca.resources.data.skin.Hair;
import fabric.net.mca.resources.data.skin.SkinListEntry;
import fabric.net.mca.server.world.data.CustomClothingManager;
import net.minecraft.class_3222;

public class AddCustomClothingMessage implements Message {
   private static final long serialVersionUID = 4620788389788045910L;
   final SkinListEntry entry;

   public AddCustomClothingMessage(SkinListEntry entry) {
      this.entry = entry;
   }

   @Override
   public void receive(class_3222 player) {
      if (this.entry instanceof Clothing clothing) {
         CustomClothingManager.getClothing().addEntry(this.entry.getIdentifier(), clothing);
      } else if (this.entry instanceof Hair hair) {
         CustomClothingManager.getHair().addEntry(this.entry.getIdentifier(), hair);
      }
   }
}
