package quilt.net.mca.client.gui;

import java.util.UUID;
import quilt.net.mca.MCA;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.c2s.DamageItemMessage;

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
