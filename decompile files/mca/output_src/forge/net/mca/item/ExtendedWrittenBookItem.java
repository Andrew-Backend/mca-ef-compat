package forge.net.mca.item;

import forge.net.mca.client.book.Book;
import forge.net.mca.client.book.pages.TextPage;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.s2c.OpenGuiRequest;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ExtendedWrittenBookItem extends WrittenBookItem {
   private final Book book;

   public ExtendedWrittenBookItem(Properties settings, Book book) {
      super(settings);
      this.book = book;
   }

   public void m_7373_(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
      if (this.book.getBookAuthor() != null) {
         tooltip.add(this.book.getBookAuthor());
      }
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      ItemStack itemStack = player.m_21120_(hand);
      if (player instanceof ServerPlayer) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.BOOK), (ServerPlayer)player);
      }

      return InteractionResultHolder.m_19090_(itemStack);
   }

   public boolean m_5812_(ItemStack stack) {
      return false;
   }

   public Book getBook(ItemStack item) {
      CompoundTag tag = item.m_41783_();
      if (tag != null && tag.m_128441_("pages")) {
         Book book = this.book.copy();
         ListTag pages = tag.m_128437_("pages", 8);

         for (int i = 0; i < pages.size(); i++) {
            book.addPage(new TextPage(pages.m_128778_(i)));
         }

         return book;
      } else {
         return this.book;
      }
   }
}
