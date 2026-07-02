package fabric.net.mca.client.gui;

import fabric.net.mca.MCA;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.c2s.DamageItemMessage;
import java.util.UUID;

public class CombScreen extends VillagerEditorScreen {
   public CombScreen(UUID playerUUID) {
      super(playerUUID, playerUUID);
   }

   public CombScreen(UUID villagerUUID, UUID playerUUID) {
      super(villagerUUID, playerUUID);
   }

   @Override
   protected boolean shouldShowPageSelection() {
      return false;
   }

   @Override
   protected void eventCallback(String event) {
      if (event.equals("hair")) {
         NetworkHandler.sendToServer(new DamageItemMessage(MCA.locate("comb")));
      }
   }

   @Override
   protected void setPage(String page) {
      if (page.equals("loading")) {
         super.setPage("loading");
      } else if (page.equals("head")) {
         this.syncVillagerData();
         this.method_25419();
      } else {
         super.setPage("hair");
      }
   }
}
