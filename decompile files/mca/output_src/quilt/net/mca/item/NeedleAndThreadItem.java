package quilt.net.mca.item;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1271;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.network.s2c.OpenGuiRequest;

public class NeedleAndThreadItem extends TooltippedItem {
   public NeedleAndThreadItem(class_1793 properties) {
      super(properties);
   }

   public final class_1271<class_1799> method_7836(class_1937 world, class_1657 player, class_1268 hand) {
      if (player instanceof class_3222 serverPlayer) {
         class_1799 stack = player.method_5998(hand);
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.NEEDLE_AND_THREAD), serverPlayer);
         return class_1271.method_22427(stack);
      } else {
         return super.method_7836(world, player, hand);
      }
   }

   public class_1269 method_7847(class_1799 stack, class_1657 player, class_1309 entity, class_1268 hand) {
      if (entity instanceof VillagerLike && !entity.method_37908().field_9236 && player instanceof class_3222) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.NEEDLE_AND_THREAD, entity), (class_3222)player);
         return class_1269.field_5812;
      } else {
         return class_1269.field_21466;
      }
   }
}
