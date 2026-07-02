package forge.net.mca.item;

import forge.net.mca.Config;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.OpenGuiRequest;
import forge.net.mca.server.world.data.VillagerTrackerManager;
import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.localization.FlowingText;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class VillagerTrackerItem extends Item implements Vanishable {
   public VillagerTrackerItem(Properties settings) {
      super(settings);
   }

   public final InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (player instanceof ServerPlayer serverPlayer) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_TRACKER), serverPlayer);
      }

      return InteractionResultHolder.m_19090_(stack);
   }

   public static GlobalPos getTargetPos(ItemStack stack) {
      CompoundTag position = stack.m_41737_("position");
      return position != null ? NbtHelper.decodeGlobalPos(position) : null;
   }

   public boolean m_5812_(ItemStack stack) {
      return CompassItem.m_40736_(stack) || super.m_5812_(stack);
   }

   public void m_6883_(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
      if (world instanceof ServerLevel serverWorld
         && world.m_46467_() % Config.getInstance().trackVillagerPositionEveryNTicks == 0L
         && stack.m_41784_().m_128441_("targetUUID")) {
         UUID uuid = stack.m_41784_().m_128342_("targetUUID");
         GlobalPos pos = VillagerTrackerManager.get(serverWorld).get(uuid);
         if (pos != null) {
            stack.m_41784_().m_128365_("position", NbtHelper.encodeGlobalPosition(pos));
         }
      }
   }

   public void m_7373_(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
      if (stack.m_41784_().m_128441_("targetName")) {
         tooltip.add(
            Component.m_237110_(this.m_5671_(stack) + ".active", new Object[]{stack.m_41784_().m_128423_("targetName").m_7916_()})
               .m_130940_(ChatFormatting.GREEN)
         );
         GlobalPos pos = getTargetPos(stack);
         if (pos != null && world != null && pos.m_122640_() == world.m_46472_()) {
            LocalPlayer player = Minecraft.m_91087_().f_91074_;
            if (player != null) {
               int precision = 5;
               int distance = (int)Math.sqrt(pos.m_122646_().m_203193_(player.m_20182_())) / precision * precision;
               tooltip.add(Component.m_237110_(this.m_5671_(stack) + ".distance", new Object[]{distance}).m_130940_(ChatFormatting.ITALIC));
            }
         }
      }

      tooltip.addAll(FlowingText.wrap(Component.m_237115_(this.m_5671_(stack) + ".tooltip").m_130940_(ChatFormatting.GRAY), 160));
   }
}
