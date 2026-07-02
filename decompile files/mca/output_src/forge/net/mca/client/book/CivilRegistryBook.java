package forge.net.mca.client.book;

import forge.net.mca.client.book.pages.Page;
import forge.net.mca.client.book.pages.SimpleListPage;
import forge.net.mca.client.book.pages.TitlePage;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.network.c2s.CivilRegistryPageRequest;
import forge.net.mca.util.localization.FlowingText;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.Component;

public class CivilRegistryBook extends Book {
   Set<Integer> requestedPages = new HashSet<>();
   HashMap<Integer, Page> loadedPages = new HashMap<>();
   int lastIndex = 0;
   Page EMPTY = new TitlePage("...", "");

   public CivilRegistryBook(String bookName, Component bookAuthor) {
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

   public void receive(int index, List<Component> lines) {
      List<Component> text = new LinkedList<>();

      for (Component line : lines) {
         List<Component> wrap = FlowingText.wrap(line, 110);
         if (text.size() + wrap.size() > 14) {
            break;
         }

         int i = 0;

         for (Component l : wrap) {
            if (i == 0) {
               text.add(l);
            } else {
               text.add(Component.m_237113_(" ").m_7220_(l));
            }

            i++;
         }

         this.lastIndex++;
      }

      this.loadedPages.put(index, new SimpleListPage(text));
   }
}
