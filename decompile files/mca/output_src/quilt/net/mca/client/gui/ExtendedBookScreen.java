package quilt.net.mca.client.gui;

import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_333;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_474;
import net.minecraft.class_5244;
import net.minecraft.class_2558.class_2559;
import quilt.net.mca.client.book.Book;
import quilt.net.mca.client.book.pages.Page;
import quilt.net.mca.client.gui.widget.ExtendedPageTurnWidget;
import quilt.net.mca.util.compat.ButtonWidget;

public class ExtendedBookScreen extends class_437 {
   private int pageIndex;
   private class_474 nextPageButton;
   private class_474 previousPageButton;
   private final Book book;

   public ExtendedBookScreen(Book book) {
      super(class_333.field_18967);
      this.book = book;
      book.open();
      book.setPage(0, false);
   }

   public boolean setPage(int index) {
      int i = class_3532.method_15340(index, 0, this.book.getPageCount() - 1);
      if (i != this.pageIndex) {
         this.book.setPage(i, false);
         this.pageIndex = i;
         this.updatePageButtons();
         return true;
      } else {
         return false;
      }
   }

   protected boolean jumpToPage(int page) {
      return this.setPage(page);
   }

   protected void method_25426() {
      this.addCloseButton();
      this.addPageButtons();
   }

   protected void addCloseButton() {
      this.method_37063(new ButtonWidget(this.field_22789 / 2 - 100, 196, 200, 20, class_5244.field_24334, buttonWidget -> this.field_22787.method_1507(null)));
   }

   protected void addPageButtons() {
      int i = (this.field_22789 - 192) / 2;
      this.nextPageButton = (class_474)this.method_37063(
         new ExtendedPageTurnWidget(i + 116, 159, true, buttonWidget -> this.goToNextPage(), this.book.hasPageTurnSound(), this.book.getBackground())
      );
      this.previousPageButton = (class_474)this.method_37063(
         new ExtendedPageTurnWidget(i + 43, 159, false, buttonWidget -> this.goToPreviousPage(), this.book.hasPageTurnSound(), this.book.getBackground())
      );
      this.updatePageButtons();
   }

   protected void goToPreviousPage() {
      if (this.book.getPage(this.pageIndex).previousPage()) {
         if (this.pageIndex > 0) {
            this.pageIndex--;
            this.book.setPage(this.pageIndex, true);
         }

         this.updatePageButtons();
      }
   }

   protected void goToNextPage() {
      if (this.book.getPage(this.pageIndex).nextPage()) {
         if (this.pageIndex < this.book.getPageCount() - 1) {
            this.pageIndex++;
            this.book.setPage(this.pageIndex, false);
         }

         this.updatePageButtons();
      }
   }

   private void updatePageButtons() {
      this.nextPageButton.field_22764 = this.pageIndex < this.book.getPageCount() - 1;
      this.previousPageButton.field_22764 = this.pageIndex > 0;
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (super.method_25404(keyCode, scanCode, modifiers)) {
         return true;
      }

      switch (keyCode) {
         case 266:
            this.previousPageButton.method_25306();
            return true;
         case 267:
            this.nextPageButton.method_25306();
            return true;
         default:
            return false;
      }
   }

   public class_327 getTextRenderer() {
      return this.field_22793;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      int i = (this.field_22789 - 192) / 2;
      context.method_25302(this.book.getBackground(), i, 2, 0, 0, 192, 192);
      if (this.book.showPageCount()) {
         class_2561 pageIndexText = class_2561.method_43469("book.pageIndicator", new Object[]{this.pageIndex + 1, Math.max(this.book.getPageCount(), 1)})
            .method_27692(this.book.getTextFormatting());
         int k = this.field_22793.method_27525(pageIndexText);
         context.method_51439(this.field_22793, pageIndexText, i - k + 192 - 44, 18, 0, this.getBook().hasTextShadow());
      }

      Page page = this.book.getPage(this.pageIndex);
      if (page != null) {
         page.render(this, context, mouseX, mouseY, delta);
      }

      super.method_25394(context, mouseX, mouseY, delta);
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      return super.method_25402(mouseX, mouseY, button);
   }

   public boolean method_25430(class_2583 style) {
      class_2558 clickEvent = style.method_10970();
      if (clickEvent == null) {
         return false;
      }

      if (clickEvent.method_10845() == class_2559.field_11748) {
         try {
            return this.jumpToPage(Integer.parseInt(clickEvent.method_10844()) - 1);
         } catch (Exception var5) {
            return false;
         }
      } else {
         boolean handled = super.method_25430(style);
         if (handled && clickEvent.method_10845() == class_2559.field_11750) {
            this.field_22787.method_1507(null);
         }

         return handled;
      }
   }

   public Book getBook() {
      return this.book;
   }
}
