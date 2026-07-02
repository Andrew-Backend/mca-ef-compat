package forge.net.mca.client.gui;

import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.c2s.SetTargetMessage;
import java.util.UUID;

public class VillagerTrackerSearchScreen extends FamilyTreeSearchScreen {
   @Override
   void selectVillager(String name, UUID villager) {
      NetworkHandler.sendToServer(new SetTargetMessage(MCA.locate("villager_tracker"), name, villager));
      this.m_7379_();
   }
}
