package fabric.net.mca.network.c2s;

import fabric.net.mca.client.gui.FamilyTreeSearchScreen;
import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.s2c.FamilyTreeUUIDResponse;
import fabric.net.mca.server.world.data.FamilyTree;
import fabric.net.mca.server.world.data.FamilyTreeNode;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.class_3222;

public class FamilyTreeUUIDLookup implements Message {
   private static final long serialVersionUID = 3458196476082270702L;
   private final String search;

   public FamilyTreeUUIDLookup(String search) {
      this.search = search;
   }

   @Override
   public void receive(class_3222 player) {
      FamilyTree tree = FamilyTree.get(player.method_51469());
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
