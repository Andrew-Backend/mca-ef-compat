package forge.net.mca.client.book;

import forge.net.mca.client.book.pages.EmptyPage;
import forge.net.mca.client.book.pages.Page;
import forge.net.mca.client.book.pages.TextPage;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Book {
   private final String bookName;
   private final Component bookAuthor;
   private final List<Page> pages = new LinkedList<>();
   private ResourceLocation background = new ResourceLocation("textures/gui/book.png");
   private ChatFormatting textFormatting = ChatFormatting.BLACK;
   private boolean pageTurnSound = true;
   private boolean textShadow;

   public Book(String bookName) {
      this(bookName, Component.m_237115_("mca.books." + bookName + ".author").m_130940_(ChatFormatting.GRAY));
   }

   public Book(String bookName, Component bookAuthor) {
      this.bookName = bookName;
      this.bookAuthor = bookAuthor;
   }

   public Book setBackground(ResourceLocation background) {
      this.background = background;
      return this;
   }

   public Book setTextFormatting(ChatFormatting textFormatting) {
      this.textFormatting = textFormatting;
      return this;
   }

   public Book setTextShadow(boolean textShadow) {
      this.textShadow = textShadow;
      return this;
   }

   public Book setPageTurnSound(boolean pageTurnSound) {
      this.pageTurnSound = pageTurnSound;
      return this;
   }

   public Book addPage(Page page) {
      this.pages.add(page);
      return this;
   }

   private Book addPages(List<Page> pages) {
      for (Page p : pages) {
         this.addPage(p);
      }

      return this;
   }

   public Book addSimplePages(int n) {
      return this.addSimplePages(n, 0);
   }

   public Book addSimplePages(int n, int start) {
      for (int i = 0; i < n; i++) {
         this.addPage(new TextPage(this.getBookName(), start + i));
      }

      return this;
   }

   public int getPageCount() {
      return this.pages.size();
   }

   public boolean showPageCount() {
      return this.getPageCount() > 1;
   }

   public String getBookName() {
      return this.bookName;
   }

   @Nullable
   public Component getBookAuthor() {
      return this.bookAuthor;
   }

   public List<Page> getPages() {
      return this.pages;
   }

   public ResourceLocation getBackground() {
      return this.background;
   }

   public ChatFormatting getTextFormatting() {
      return this.textFormatting;
   }

   public boolean hasTextShadow() {
      return this.textShadow;
   }

   public boolean hasPageTurnSound() {
      return this.pageTurnSound;
   }

   public Page getPage(int index) {
      return index < this.pages.size() ? this.pages.get(index) : new EmptyPage();
   }

   public void open() {
   }

   public void setPage(int i, boolean back) {
      this.getPage(i).open(back);
   }

   public Book copy() {
      return new Book(this.getBookName())
         .setBackground(this.getBackground())
         .setTextFormatting(this.getTextFormatting())
         .setPageTurnSound(this.hasPageTurnSound())
         .addPages(this.pages);
   }
}
