package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.GetFamilyTreeResponse;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerPlayer;

public class GetFamilyTreeRequest implements Message {
   private static final long serialVersionUID = -6232925305386763715L;
   final UUID uuid;

   public GetFamilyTreeRequest(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void receive(ServerPlayer player) {
      FamilyTree.get(player.m_284548_())
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
