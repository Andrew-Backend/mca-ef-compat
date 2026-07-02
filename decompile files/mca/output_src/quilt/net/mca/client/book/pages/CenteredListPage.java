package quilt.net.mca.client.book.pages;

import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5481;
import quilt.net.mca.client.gui.ExtendedBookScreen;

public class CenteredListPage extends ListPage {
   final class_2561 title;
   public static final int ENTRIES_PER_PAGE = 11;

   public CenteredListPage(class_2561 title, List<class_2561> text) {
      super(text);
      this.title = title;
   }

   public CenteredListPage(String title, List<class_2561> text) {
      this(class_2561.method_43471(title).method_27692(class_124.field_1074).method_27692(class_124.field_1067), text);
   }

   @Override
   int getEntriesPerPage() {
      return 11;
   }

   private static void drawCenteredText(ExtendedBookScreen screen, class_332 context, class_327 textRenderer, class_2561 text, int centerX, int y, int color) {
      class_5481 orderedText = text.method_30937();
      context.method_51430(textRenderer, orderedText, centerX - textRenderer.method_30880(orderedText) / 2, y, color, screen.getBook().hasTextShadow());
   }

   @Override
   public void render(ExtendedBookScreen screen, class_332 context, int mouseX, int mouseY, float delta) {
      drawCenteredText(screen, context, screen.getTextRenderer(), this.title, screen.field_22789 / 2, 35, -1);
      int y = 48;

      for (int i = this.page * 11; i < Math.min(this.text.size(), (this.page + 1) * 11); i++) {
         drawCenteredText(screen, context, screen.getTextRenderer(), this.text.get(i), screen.field_22789 / 2 - 4, y, -1);
         y += 10;
      }
   }
}
