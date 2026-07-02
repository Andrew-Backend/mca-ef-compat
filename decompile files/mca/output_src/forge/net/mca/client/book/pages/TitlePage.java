package forge.net.mca.client.book.pages;

import forge.net.mca.client.gui.ExtendedBookScreen;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class TitlePage extends Page {
   final Component title;
   final Component subtitle;

   public TitlePage(String book) {
      this(book, ChatFormatting.BLACK);
   }

   public TitlePage(String book, ChatFormatting color) {
      this("item.mca.book_" + book, "mca.books." + book + ".author", color);
   }

   public TitlePage(String title, String subtitle) {
      this(title, subtitle, ChatFormatting.BLACK);
   }

   public TitlePage(String title, String subtitle, ChatFormatting color) {
      this(
         Component.m_237115_(title).m_130940_(color).m_130940_(ChatFormatting.BOLD),
         Component.m_237115_(subtitle).m_130940_(color).m_130940_(ChatFormatting.ITALIC)
      );
   }

   public TitlePage(Component title, Component subtitle) {
      this.title = title;
      this.subtitle = subtitle;
   }

   private static void drawCenteredText(ExtendedBookScreen screen, GuiGraphics context, Font textRenderer, Component text, int centerX, int y, int color) {
      FormattedCharSequence orderedText = text.m_7532_();
      drawCenteredText(screen, context, textRenderer, orderedText, centerX, y, color);
   }

   private static void drawCenteredText(
      ExtendedBookScreen screen, GuiGraphics context, Font textRenderer, FormattedCharSequence text, int centerX, int y, int color
   ) {
      context.m_280649_(textRenderer, text, centerX - textRenderer.m_92724_(text) / 2, y, color, screen.getBook().hasTextShadow());
   }

   @Override
   public void render(ExtendedBookScreen screen, GuiGraphics context, int mouseX, int mouseY, float delta) {
      List<FormattedCharSequence> texts = screen.getTextRenderer().m_92923_(this.title, 114);
      int y = 80 - 5 * texts.size();

      for (FormattedCharSequence t : texts) {
         drawCenteredText(screen, context, screen.getTextRenderer(), t, screen.f_96543_ / 2 - 2, y, 16777215);
         y += 10;
      }

      y = 82 + 5 * texts.size();
      drawCenteredText(screen, context, screen.getTextRenderer(), this.subtitle, screen.f_96543_ / 2 - 2, y, 16777215);
   }
}
