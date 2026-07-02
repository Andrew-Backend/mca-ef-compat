package forge.net.mca.client.book.pages;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.network.chat.Component;

public abstract class ListPage extends Page {
   final List<Component> text;
   int page;

   public ListPage() {
      this.text = new LinkedList<>();
   }

   public ListPage(List<Component> text) {
      this.text = text;
   }

   @Override
   public void open(boolean back) {
      this.page = back ? (this.text.size() - 1) / this.getEntriesPerPage() : 0;
   }

   @Override
   public boolean previousPage() {
      if (this.page > 0) {
         this.page--;
         return false;
      } else {
         return true;
      }
   }

   @Override
   public boolean nextPage() {
      if (this.page < (this.text.size() - 1) / this.getEntriesPerPage()) {
         this.page++;
         return false;
      } else {
         return true;
      }
   }

   abstract int getEntriesPerPage();
}
