package quilt.net.mca.network.s2c;

import java.util.Map;
import java.util.UUID;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.server.world.data.FamilyTreeNode;

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
