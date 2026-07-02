package fabric.net.mca.client.book;

import fabric.net.mca.client.book.pages.EmptyPage;
import fabric.net.mca.client.book.pages.Page;
import fabric.net.mca.client.book.pages.TextPage;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public class Book {
   private final String bookName;
   private final class_2561 bookAuthor;
   private final List<Page> pages = new LinkedList<>();
   private class_2960 background = new class_2960("textures/gui/book.png");
   private class_124 textFormatting = class_124.field_1074;
   private boolean pageTurnSound = true;
   private boolean textShadow;

   public Book(String bookName) {
      this(bookName, class_2561.method_43471("mca.books." + bookName + ".author").method_27692(class_124.field_1080));
   }

   public Book(String bookName, class_2561 bookAuthor) {
      this.bookName = bookName;
      this.bookAuthor = bookAuthor;
   }

   public Book setBackground(class_2960 background) {
      this.background = background;
      return this;
   }

   public Book setTextFormatting(class_124 textFormatting) {
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
   public class_2561 getBookAuthor() {
      return this.bookAuthor;
   }

   public List<Page> getPages() {
      return this.pages;
   }

   public class_2960 getBackground() {
      return this.background;
   }

   public class_124 getTextFormatting() {
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
