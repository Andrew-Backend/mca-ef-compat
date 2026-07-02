package forge.net.mca.item;

import forge.net.mca.client.book.Book;
import forge.net.mca.util.localization.FlowingText;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CivilRegistry extends ExtendedWrittenBookItem {
   public CivilRegistry(Properties settings, Book book) {
      super(settings, book);
   }

   @Override
   public void m_7373_(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
      tooltip.addAll(FlowingText.wrap(Component.m_237115_(this.m_5671_(stack) + ".tooltip").m_130940_(ChatFormatting.GRAY), 160));
   }
}
