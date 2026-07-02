package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.server.world.data.FamilyTreeNode;
import java.util.Map;
import java.util.UUID;

public class GetFamilyTreeResponse implements Message {
   private static final long serialVersionUID = 1371939319244994642L;
   public final UUID uuid;
   public final Map<UUID, FamilyTreeNode> family;

   public GetFamilyTreeResponse(UUID uuid, Map<UUID, FamilyTreeNode> family) {
      this.uuid = uuid;
      this.family = family;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleFamilyTreeResponse(this);
   }
}
