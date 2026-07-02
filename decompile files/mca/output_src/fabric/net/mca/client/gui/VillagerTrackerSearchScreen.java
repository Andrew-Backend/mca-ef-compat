package fabric.net.mca.client.gui;

import fabric.net.mca.MCA;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.c2s.SetTargetMessage;
import java.util.UUID;

public class VillagerTrackerSearchScreen extends FamilyTreeSearchScreen {
   @Override
   void selectVillager(String name, UUID villager) {
      NetworkHandler.sendToServer(new SetTargetMessage(MCA.locate("villager_tracker"), name, villager));
      this.method_25419();
   }
}
