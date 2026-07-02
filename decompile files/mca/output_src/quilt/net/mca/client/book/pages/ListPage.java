package quilt.net.mca.client.book.pages;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.class_2561;

public abstract class ListPage extends Page {
   final List<class_2561> text;
   int page;

   public ListPage() {
      this.text = new LinkedList<>();
   }

   public ListPage(List<class_2561> text) {
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
