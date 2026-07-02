package forge.net.mca.item;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.network.s2c.OpenGuiRequest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class NeedleAndThreadItem extends TooltippedItem {
   public NeedleAndThreadItem(Properties properties) {
      super(properties);
   }

   public final InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      if (player instanceof ServerPlayer serverPlayer) {
         ItemStack stack = player.m_21120_(hand);
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.NEEDLE_AND_THREAD), serverPlayer);
         return InteractionResultHolder.m_19090_(stack);
      } else {
         return super.m_7203_(world, player, hand);
      }
   }

   public InteractionResult m_6880_(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
      if (entity instanceof VillagerLike && !entity.m_9236_().f_46443_ && player instanceof ServerPlayer) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.NEEDLE_AND_THREAD, entity), (ServerPlayer)player);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.CONSUME;
      }
   }
}
