package forge.net.mca.item;

import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.network.s2c.OpenGuiRequest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class VillagerEditorItem extends TooltippedItem {
   public VillagerEditorItem(Properties settings) {
      super(settings);
   }

   public InteractionResult m_6880_(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
      if (entity instanceof VillagerLike<?> villager && !entity.m_9236_().f_46443_ && player instanceof ServerPlayer serverPlayer) {
         if (player.m_6144_()) {
            villager.getInteractions().handle(serverPlayer, "inventory");
         } else {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_EDITOR, entity), serverPlayer);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.CONSUME;
      }
   }
}
