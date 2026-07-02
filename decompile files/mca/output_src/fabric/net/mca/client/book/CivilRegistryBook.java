package fabric.net.mca.client.book;

import fabric.net.mca.client.book.pages.Page;
import fabric.net.mca.client.book.pages.SimpleListPage;
import fabric.net.mca.client.book.pages.TitlePage;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.network.c2s.CivilRegistryPageRequest;
import fabric.net.mca.util.localization.FlowingText;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2561;

public class CivilRegistryBook extends Book {
   Set<Integer> requestedPages = new HashSet<>();
   HashMap<Integer, Page> loadedPages = new HashMap<>();
   int lastIndex = 0;
   Page EMPTY = new TitlePage("...", "");

   public CivilRegistryBook(String bookName, class_2561 bookAuthor) {
      super(bookName, bookAuthor);
   }

   @Override
   public void open() {
      super.open();
      this.requestedPages.clear();
      this.loadedPages.clear();
      this.lastIndex = 0;
   }

   @Override
   public int getPageCount() {
      return 9999;
   }

   @Override
   public boolean showPageCount() {
      return false;
   }

   @Override
   public Page getPage(int index) {
      if (!this.requestedPages.contains(index) && (this.requestedPages.contains(index - 1) || index == 0)) {
         this.requestedPages.add(index);
         NetworkHandler.sendToServer(new CivilRegistryPageRequest(index, this.lastIndex, this.lastIndex + 14));
      }

      return this.loadedPages.containsKey(index) ? this.loadedPages.get(index) : this.EMPTY;
   }

   public void receive(int index, List<class_2561> lines) {
      List<class_2561> text = new LinkedList<>();

      for (class_2561 line : lines) {
         List<class_2561> wrap = FlowingText.wrap(line, 110);
         if (text.size() + wrap.size() > 14) {
            break;
         }

         int i = 0;

         for (class_2561 l : wrap) {
            if (i == 0) {
               text.add(l);
            } else {
               text.add(class_2561.method_43470(" ").method_10852(l));
            }

            i++;
         }

         this.lastIndex++;
      }

      this.loadedPages.put(index, new SimpleListPage(text));
   }
}
