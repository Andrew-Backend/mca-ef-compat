package forge.net.mca.client.gui;

import forge.net.mca.client.book.Book;
import forge.net.mca.client.book.pages.Page;
import forge.net.mca.client.gui.widget.ExtendedPageTurnWidget;
import forge.net.mca.util.compat.ButtonWidget;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.util.Mth;

public class ExtendedBookScreen extends Screen {
   private int pageIndex;
   private PageButton nextPageButton;
   private PageButton previousPageButton;
   private final Book book;

   public ExtendedBookScreen(Book book) {
      super(GameNarrator.f_93310_);
      this.book = book;
      book.open();
      book.setPage(0, false);
   }

   public boolean setPage(int index) {
      int i = Mth.m_14045_(index, 0, this.book.getPageCount() - 1);
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

   protected void m_7856_() {
      this.addCloseButton();
      this.addPageButtons();
   }

   protected void addCloseButton() {
      this.m_142416_(new ButtonWidget(this.f_96543_ / 2 - 100, 196, 200, 20, CommonComponents.f_130655_, buttonWidget -> this.f_96541_.m_91152_(null)));
   }

   protected void addPageButtons() {
      int i = (this.f_96543_ - 192) / 2;
      this.nextPageButton = (PageButton)this.m_142416_(
         new ExtendedPageTurnWidget(i + 116, 159, true, buttonWidget -> this.goToNextPage(), this.book.hasPageTurnSound(), this.book.getBackground())
      );
      this.previousPageButton = (PageButton)this.m_142416_(
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
      this.nextPageButton.f_93624_ = this.pageIndex < this.book.getPageCount() - 1;
      this.previousPageButton.f_93624_ = this.pageIndex > 0;
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (super.m_7933_(keyCode, scanCode, modifiers)) {
         return true;
      }

      switch (keyCode) {
         case 266:
            this.previousPageButton.m_5691_();
            return true;
         case 267:
            this.nextPageButton.m_5691_();
            return true;
         default:
            return false;
      }
   }

   public Font getTextRenderer() {
      return this.f_96547_;
   }

   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.m_280273_(context);
      int i = (this.f_96543_ - 192) / 2;
      context.m_280218_(this.book.getBackground(), i, 2, 0, 0, 192, 192);
      if (this.book.showPageCount()) {
         Component pageIndexText = Component.m_237110_("book.pageIndicator", new Object[]{this.pageIndex + 1, Math.max(this.book.getPageCount(), 1)})
            .m_130940_(this.book.getTextFormatting());
         int k = this.f_96547_.m_92852_(pageIndexText);
         context.m_280614_(this.f_96547_, pageIndexText, i - k + 192 - 44, 18, 0, this.getBook().hasTextShadow());
      }

      Page page = this.book.getPage(this.pageIndex);
      if (page != null) {
         page.render(this, context, mouseX, mouseY, delta);
      }

      super.m_88315_(context, mouseX, mouseY, delta);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      return super.m_6375_(mouseX, mouseY, button);
   }

   public boolean m_5561_(Style style) {
      ClickEvent clickEvent = style.m_131182_();
      if (clickEvent == null) {
         return false;
      }

      if (clickEvent.m_130622_() == Action.CHANGE_PAGE) {
         try {
            return this.jumpToPage(Integer.parseInt(clickEvent.m_130623_()) - 1);
         } catch (Exception var5) {
            return false;
         }
      } else {
         boolean handled = super.m_5561_(style);
         if (handled && clickEvent.m_130622_() == Action.RUN_COMMAND) {
            this.f_96541_.m_91152_(null);
         }

         return handled;
      }
   }

   public Book getBook() {
      return this.book;
   }
}
