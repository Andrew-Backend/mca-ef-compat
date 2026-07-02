package quilt.net.mca.network.c2s;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.s2c.GetFamilyTreeResponse;
import quilt.net.mca.server.world.data.FamilyTree;
import quilt.net.mca.server.world.data.FamilyTreeNode;

public class GetFamilyTreeRequest implements Message {
   private static final long serialVersionUID = -6232925305386763715L;
   final UUID uuid;

   public GetFamilyTreeRequest(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void receive(class_3222 player) {
      FamilyTree.get(player.method_51469())
         .getOrEmpty(this.uuid)
         .ifPresent(
            entry -> {
               Map<UUID, FamilyTreeNode> familyEntries = Stream.concat(
                     entry.lookup(Stream.of(entry.id(), entry.partner())), entry.lookup(entry.getRelatives(2, 1))
                  )
                  .distinct()
                  .collect(Collectors.toMap(FamilyTreeNode::id, Function.identity()));
               NetworkHandler.sendToPlayer(new GetFamilyTreeResponse(this.uuid, familyEntries), player);
            }
         );
   }
}
