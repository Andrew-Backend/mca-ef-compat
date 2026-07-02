package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SetTargetMessage implements Message {
   private static final long serialVersionUID = 7257172480717481644L;
   private final String itemIdentifier;
   private final String targetName;
   private final String targetUUID;

   public SetTargetMessage(ResourceLocation identifier, String targetName, UUID targetUUID) {
      this.itemIdentifier = identifier.toString();
      this.targetName = targetName;
      this.targetUUID = targetUUID.toString();
   }

   @Override
   public void receive(ServerPlayer player) {
      Arrays.stream(InteractionHand.values()).forEach(hand -> {
         ItemStack stack = player.m_21120_(hand);
         if (BuiltInRegistries.f_257033_.m_7981_(stack.m_41720_()).toString().equals(this.itemIdentifier)) {
            stack.m_41784_().m_128359_("targetName", this.targetName);
            stack.m_41784_().m_128362_("targetUUID", UUID.fromString(this.targetUUID));
         }
      });
   }
}
