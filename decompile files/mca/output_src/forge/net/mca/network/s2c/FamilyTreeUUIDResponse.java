package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.client.gui.FamilyTreeSearchScreen;
import forge.net.mca.cobalt.network.Message;
import java.util.List;

public class FamilyTreeUUIDResponse implements Message {
   private static final long serialVersionUID = 8216277949975695897L;
   private final List<FamilyTreeSearchScreen.Entry> list;

   public FamilyTreeUUIDResponse(List<FamilyTreeSearchScreen.Entry> list) {
      this.list = list;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleFamilyTreeUUIDResponse(this);
   }

   public List<FamilyTreeSearchScreen.Entry> getList() {
      return this.list;
   }
}
