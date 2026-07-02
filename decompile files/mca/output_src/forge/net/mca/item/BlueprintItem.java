package forge.net.mca.item;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.OpenGuiRequest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class BlueprintItem extends TooltippedItem {
   public BlueprintItem(Properties properties) {
      super(properties);
   }

   public final InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (player instanceof ServerPlayer serverPlayer) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.BLUEPRINT), serverPlayer);
      }

      return InteractionResultHolder.m_19090_(stack);
   }
}
