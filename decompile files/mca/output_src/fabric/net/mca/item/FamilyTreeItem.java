package fabric.net.mca.item;

import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.s2c.OpenGuiRequest;
import net.minecraft.class_1268;
import net.minecraft.class_1271;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;

public class FamilyTreeItem extends TooltippedItem {
   public FamilyTreeItem(class_1793 properties) {
      super(properties);
   }

   public final class_1271<class_1799> method_7836(class_1937 world, class_1657 player, class_1268 hand) {
      class_1799 stack = player.method_5998(hand);
      if (player instanceof class_3222 serverPlayer) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.FAMILY_TREE), serverPlayer);
      }

      return class_1271.method_22427(stack);
   }
}
