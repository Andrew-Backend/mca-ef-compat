package quilt.net.mca;

import net.minecraft.class_310;
import quilt.net.mca.client.gui.DestinyScreen;

public class DestinyManager {
   private boolean openDestiny;
   private boolean allowTeleportation;

   public void tick(class_310 client) {
      if (this.openDestiny && client.field_1755 == null) {
         assert client.field_1724 != null;
         client.method_1507(new DestinyScreen(client.field_1724.method_5667(), this.allowTeleportation));
      }
   }

   public void requestOpen(boolean allowTeleportation) {
      this.openDestiny = true;
      this.allowTeleportation = allowTeleportation;
   }

   public void allowClosing() {
      this.openDestiny = false;
   }
}
