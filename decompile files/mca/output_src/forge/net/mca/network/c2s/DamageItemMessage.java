package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import java.util.Arrays;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class DamageItemMessage implements Message {
   private static final long serialVersionUID = -8975978126445189429L;
   private final String itemIdentifier;

   public DamageItemMessage(ResourceLocation identifier) {
      this.itemIdentifier = identifier.toString();
   }

   @Override
   public void receive(ServerPlayer player) {
      Arrays.stream(InteractionHand.values()).forEach(hand -> {
         ItemStack stack = player.m_21120_(hand);
         if (BuiltInRegistries.f_257033_.m_7981_(stack.m_41720_()).toString().equals(this.itemIdentifier)) {
            stack.m_41622_(1, player, e -> e.m_21166_(EquipmentSlot.MAINHAND));
         }
      });
   }
}
