package forge.net.mca.client.book.pages;

import forge.net.mca.client.gui.ExtendedBookScreen;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class CenteredListPage extends ListPage {
   final Component title;
   public static final int ENTRIES_PER_PAGE = 11;

   public CenteredListPage(Component title, List<Component> text) {
      super(text);
      this.title = title;
   }

   public CenteredListPage(String title, List<Component> text) {
      this(Component.m_237115_(title).m_130940_(ChatFormatting.BLACK).m_130940_(ChatFormatting.BOLD), text);
   }

   @Override
   int getEntriesPerPage() {
      return 11;
   }

   private static void drawCenteredText(ExtendedBookScreen screen, GuiGraphics context, Font textRenderer, Component text, int centerX, int y, int color) {
      FormattedCharSequence orderedText = text.m_7532_();
      context.m_280649_(textRenderer, orderedText, centerX - textRenderer.m_92724_(orderedText) / 2, y, color, screen.getBook().hasTextShadow());
   }

   @Override
   public void render(ExtendedBookScreen screen, GuiGraphics context, int mouseX, int mouseY, float delta) {
      drawCenteredText(screen, context, screen.getTextRenderer(), this.title, screen.f_96543_ / 2, 35, -1);
      int y = 48;

      for (int i = this.page * 11; i < Math.min(this.text.size(), (this.page + 1) * 11); i++) {
         drawCenteredText(screen, context, screen.getTextRenderer(), this.text.get(i), screen.f_96543_ / 2 - 4, y, -1);
         y += 10;
      }
   }
}
