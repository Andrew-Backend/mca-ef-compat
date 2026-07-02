package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.s2c.GetVillagerResponse;
import fabric.net.mca.server.world.data.FamilyTree;
import fabric.net.mca.server.world.data.FamilyTreeNode;
import fabric.net.mca.server.world.data.PlayerSaveData;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_2487;
import net.minecraft.class_3218;
import net.minecraft.class_3222;

public class GetVillagerRequest implements Message {
   private static final long serialVersionUID = -4415670234855916259L;
   private final UUID uuid;

   public GetVillagerRequest(UUID uuid) {
      this.uuid = uuid;
   }

   @Override
   public void receive(class_3222 player) {
      class_1297 e = player.method_51469().method_14190(this.uuid);
      class_2487 villagerData = getVillagerData(e);
      if (villagerData != null) {
         NetworkHandler.sendToPlayer(new GetVillagerResponse(villagerData), player);
      }
   }

   private static void storeNode(class_2487 data, Optional<FamilyTreeNode> entry, String prefix) {
      if (entry.isPresent()) {
         data.method_10582("tree_" + prefix + "_name", entry.get().getName());
         data.method_25927("tree_" + prefix + "_uuid", entry.get().id());
      } else {
         data.method_10582("tree_" + prefix + "_name", "");
         data.method_25927("tree_" + prefix + "_uuid", class_156.field_25140);
      }
   }

   public static class_2487 getVillagerData(class_1297 e) {
      class_2487 data;
      if (e instanceof class_3222 serverPlayer) {
         data = PlayerSaveData.get(serverPlayer).getEntityData();
      } else {
         if (!(e instanceof class_1309)) {
            return null;
         }

         data = new class_2487();
         ((class_1308)e).method_5652(data);
      }

      FamilyTree tree = FamilyTree.get((class_3218)e.method_37908());
      FamilyTreeNode entry = tree.getOrCreate(e);
      storeNode(data, tree.getOrEmpty(entry.partner()), "spouse");
      storeNode(data, tree.getOrEmpty(entry.father()), "father");
      storeNode(data, tree.getOrEmpty(entry.mother()), "mother");
      return data;
   }
}
