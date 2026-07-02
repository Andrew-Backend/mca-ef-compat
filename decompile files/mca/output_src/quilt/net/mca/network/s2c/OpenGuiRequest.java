package quilt.net.mca.network.s2c;

import net.minecraft.class_1297;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;

public class OpenGuiRequest implements Message {
   private static final long serialVersionUID = -2371116419166251497L;
   public final int gui;
   public final int villager;

   public OpenGuiRequest(OpenGuiRequest.Type gui, class_1297 villager) {
      this(gui, villager.method_5628());
   }

   public OpenGuiRequest(OpenGuiRequest.Type gui, int villager) {
      this.gui = gui.ordinal();
      this.villager = villager;
   }

   public OpenGuiRequest(OpenGuiRequest.Type gui) {
      this(gui, 0);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleGuiRequest(this);
   }

   public OpenGuiRequest.Type getGui() {
      return OpenGuiRequest.Type.values()[this.gui];
   }

   public enum Type {
      BABY_NAME,
      WHISTLE,
      BLUEPRINT,
      INTERACT,
      VILLAGER_EDITOR,
      LIMITED_VILLAGER_EDITOR,
      BOOK,
      FAMILY_TREE,
      VILLAGER_TRACKER,
      NEEDLE_AND_THREAD,
      COMB,
      CLOSE;
   }
}
