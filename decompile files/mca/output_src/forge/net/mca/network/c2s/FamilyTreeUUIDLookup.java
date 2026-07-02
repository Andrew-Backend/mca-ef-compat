package forge.net.mca.network.c2s;

import forge.net.mca.client.gui.FamilyTreeSearchScreen;
import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.FamilyTreeUUIDResponse;
import forge.net.mca.server.world.data.FamilyTree;
import forge.net.mca.server.world.data.FamilyTreeNode;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;

public class FamilyTreeUUIDLookup implements Message {
   private static final long serialVersionUID = 3458196476082270702L;
   private final String search;

   public FamilyTreeUUIDLookup(String search) {
      this.search = search;
   }

   @Override
   public void receive(ServerPlayer player) {
      FamilyTree tree = FamilyTree.get(player.m_284548_());
      List<FamilyTreeSearchScreen.Entry> list = tree.getAllWithName(this.search)
         .map(
            entry -> new FamilyTreeSearchScreen.Entry(
               entry.id(),
               entry.getName(),
               tree.getOrEmpty(entry.father()).map(FamilyTreeNode::getName).orElse(""),
               tree.getOrEmpty(entry.mother()).map(FamilyTreeNode::getName).orElse("")
            )
         )
         .limit(16L)
         .collect(Collectors.toList());
      NetworkHandler.sendToPlayer(new FamilyTreeUUIDResponse(list), player);
   }
}
