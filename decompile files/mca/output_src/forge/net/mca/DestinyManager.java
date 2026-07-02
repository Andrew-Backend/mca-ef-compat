package forge.net.mca;

import forge.net.mca.client.gui.DestinyScreen;
import net.minecraft.client.Minecraft;

public class DestinyManager {
   private boolean openDestiny;
   private boolean allowTeleportation;

   public void tick(Minecraft client) {
      if (this.openDestiny && client.f_91080_ == null) {
         assert client.f_91074_ != null;
         client.m_91152_(new DestinyScreen(client.f_91074_.m_20148_(), this.allowTeleportation));
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
