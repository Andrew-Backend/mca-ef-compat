package fabric.net.mca.item;

import fabric.net.mca.Config;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.s2c.OpenGuiRequest;
import fabric.net.mca.server.world.data.VillagerTrackerManager;
import fabric.net.mca.util.NbtHelper;
import fabric.net.mca.util.localization.FlowingText;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_1268;
import net.minecraft.class_1271;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1759;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_1937;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_4208;
import net.minecraft.class_5150;
import net.minecraft.class_746;
import net.minecraft.class_1792.class_1793;
import org.jetbrains.annotations.Nullable;

public class VillagerTrackerItem extends class_1792 implements class_5150 {
   public VillagerTrackerItem(class_1793 settings) {
      super(settings);
   }

   public final class_1271<class_1799> method_7836(class_1937 world, class_1657 player, class_1268 hand) {
      class_1799 stack = player.method_5998(hand);
      if (player instanceof class_3222 serverPlayer) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_TRACKER), serverPlayer);
      }

      return class_1271.method_22427(stack);
   }

   public static class_4208 getTargetPos(class_1799 stack) {
      class_2487 position = stack.method_7941("position");
      return position != null ? NbtHelper.decodeGlobalPos(position) : null;
   }

   public boolean method_7886(class_1799 stack) {
      return class_1759.method_26365(stack) || super.method_7886(stack);
   }

   public void method_7888(class_1799 stack, class_1937 world, class_1297 entity, int slot, boolean selected) {
      if (world instanceof class_3218 serverWorld
         && world.method_8510() % Config.getInstance().trackVillagerPositionEveryNTicks == 0L
         && stack.method_7948().method_10545("targetUUID")) {
         UUID uuid = stack.method_7948().method_25926("targetUUID");
         class_4208 pos = VillagerTrackerManager.get(serverWorld).get(uuid);
         if (pos != null) {
            stack.method_7948().method_10566("position", NbtHelper.encodeGlobalPosition(pos));
         }
      }
   }

   public void method_7851(class_1799 stack, @Nullable class_1937 world, List<class_2561> tooltip, class_1836 context) {
      if (stack.method_7948().method_10545("targetName")) {
         tooltip.add(
            class_2561.method_43469(this.method_7866(stack) + ".active", new Object[]{stack.method_7948().method_10580("targetName").method_10714()})
               .method_27692(class_124.field_1060)
         );
         class_4208 pos = getTargetPos(stack);
         if (pos != null && world != null && pos.method_19442() == world.method_27983()) {
            class_746 player = class_310.method_1551().field_1724;
            if (player != null) {
               int precision = 5;
               int distance = (int)Math.sqrt(pos.method_19446().method_19770(player.method_19538())) / precision * precision;
               tooltip.add(class_2561.method_43469(this.method_7866(stack) + ".distance", new Object[]{distance}).method_27692(class_124.field_1056));
            }
         }
      }

      tooltip.addAll(FlowingText.wrap(class_2561.method_43471(this.method_7866(stack) + ".tooltip").method_27692(class_124.field_1080), 160));
   }
}
