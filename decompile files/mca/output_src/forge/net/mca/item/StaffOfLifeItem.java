package forge.net.mca.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class StaffOfLifeItem extends TooltippedItem {
   public StaffOfLifeItem(Properties properties) {
      super(properties);
   }

   public InteractionResult m_6225_(UseOnContext context) {
      InteractionResult result = ScytheItem.use(context, true);
      if (result == InteractionResult.SUCCESS) {
         context.m_43722_().m_41622_(1, context.m_43723_(), x -> {});
         return result;
      } else {
         return result;
      }
   }

   @Override
   public void m_7373_(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
      tooltip.add(Component.m_237110_(this.m_5671_(stack) + ".uses", new Object[]{stack.m_41776_() - stack.m_41773_()}));
      tooltip.add(Component.m_237113_(""));
      super.m_7373_(stack, world, tooltip, context);
   }

   public boolean m_5812_(ItemStack stack) {
      return true;
   }

   public Rarity m_41460_(ItemStack stack) {
      return Rarity.RARE;
   }

   public boolean m_8120_(ItemStack stack) {
      return false;
   }
}
