package quilt.net.mca.client.book.pages;

import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5481;
import quilt.net.mca.client.gui.ExtendedBookScreen;

public class TitlePage extends Page {
   final class_2561 title;
   final class_2561 subtitle;

   public TitlePage(String book) {
      this(book, class_124.field_1074);
   }

   public TitlePage(String book, class_124 color) {
      this("item.mca.book_" + book, "mca.books." + book + ".author", color);
   }

   public TitlePage(String title, String subtitle) {
      this(title, subtitle, class_124.field_1074);
   }

   public TitlePage(String title, String subtitle, class_124 color) {
      this(
         class_2561.method_43471(title).method_27692(color).method_27692(class_124.field_1067),
         class_2561.method_43471(subtitle).method_27692(color).method_27692(class_124.field_1056)
      );
   }

   public TitlePage(class_2561 title, class_2561 subtitle) {
      this.title = title;
      this.subtitle = subtitle;
   }

   private static void drawCenteredText(ExtendedBookScreen screen, class_332 context, class_327 textRenderer, class_2561 text, int centerX, int y, int color) {
      class_5481 orderedText = text.method_30937();
      drawCenteredText(screen, context, textRenderer, orderedText, centerX, y, color);
   }

   private static void drawCenteredText(ExtendedBookScreen screen, class_332 context, class_327 textRenderer, class_5481 text, int centerX, int y, int color) {
      context.method_51430(textRenderer, text, centerX - textRenderer.method_30880(text) / 2, y, color, screen.getBook().hasTextShadow());
   }

   @Override
   public void render(ExtendedBookScreen screen, class_332 context, int mouseX, int mouseY, float delta) {
      List<class_5481> texts = screen.getTextRenderer().method_1728(this.title, 114);
      int y = 80 - 5 * texts.size();

      for (class_5481 t : texts) {
         drawCenteredText(screen, context, screen.getTextRenderer(), t, screen.field_22789 / 2 - 2, y, 16777215);
         y += 10;
      }

      y = 82 + 5 * texts.size();
      drawCenteredText(screen, context, screen.getTextRenderer(), this.subtitle, screen.field_22789 / 2 - 2, y, 16777215);
   }
}
