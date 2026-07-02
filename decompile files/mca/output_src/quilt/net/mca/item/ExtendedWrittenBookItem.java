package quilt.net.mca.item;

import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1271;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_1843;
import net.minecraft.class_1937;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.client.book.Book;
import quilt.net.mca.client.book.pages.TextPage;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.network.s2c.OpenGuiRequest;

public class ExtendedWrittenBookItem extends class_1843 {
   private final Book book;

   public ExtendedWrittenBookItem(class_1793 settings, Book book) {
      super(settings);
      this.book = book;
   }

   public void method_7851(class_1799 stack, @Nullable class_1937 world, List<class_2561> tooltip, class_1836 context) {
      if (this.book.getBookAuthor() != null) {
         tooltip.add(this.book.getBookAuthor());
      }
   }

   public class_1271<class_1799> method_7836(class_1937 world, class_1657 player, class_1268 hand) {
      class_1799 itemStack = player.method_5998(hand);
      if (player instanceof class_3222) {
         NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.BOOK), (class_3222)player);
      }

      return class_1271.method_22427(itemStack);
   }

   public boolean method_7886(class_1799 stack) {
      return false;
   }

   public Book getBook(class_1799 item) {
      class_2487 tag = item.method_7969();
      if (tag != null && tag.method_10545("pages")) {
         Book book = this.book.copy();
         class_2499 pages = tag.method_10554("pages", 8);

         for (int i = 0; i < pages.size(); i++) {
            book.addPage(new TextPage(pages.method_10608(i)));
         }

         return book;
      } else {
         return this.book;
      }
   }
}
