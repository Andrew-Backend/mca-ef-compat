package quilt.net.mca.network.s2c;

import java.util.List;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.client.gui.FamilyTreeSearchScreen;
import quilt.net.mca.cobalt.network.Message;

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
