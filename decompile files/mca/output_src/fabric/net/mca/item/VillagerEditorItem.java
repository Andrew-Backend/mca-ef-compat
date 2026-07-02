package fabric.net.mca.item;

import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.network.s2c.OpenGuiRequest;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;

public class VillagerEditorItem extends TooltippedItem {
   public VillagerEditorItem(class_1793 settings) {
      super(settings);
   }

   public class_1269 method_7847(class_1799 stack, class_1657 player, class_1309 entity, class_1268 hand) {
      if (entity instanceof VillagerLike<?> villager && !entity.method_37908().field_9236 && player instanceof class_3222 serverPlayer) {
         if (player.method_5715()) {
            villager.getInteractions().handle(serverPlayer, "inventory");
         } else {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_EDITOR, entity), serverPlayer);
         }

         return class_1269.field_5812;
      } else {
         return class_1269.field_21466;
      }
   }
}
