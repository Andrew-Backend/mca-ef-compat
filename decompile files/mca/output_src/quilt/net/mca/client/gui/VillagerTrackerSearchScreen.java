package quilt.net.mca.client.gui;

import java.util.UUID;
import quilt.net.mca.MCA;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.c2s.SetTargetMessage;

public class VillagerTrackerSearchScreen extends FamilyTreeSearchScreen {
   @Override
   void selectVillager(String name, UUID villager) {
      NetworkHandler.sendToServer(new SetTargetMessage(MCA.locate("villager_tracker"), name, villager));
      this.method_25419();
   }
}
