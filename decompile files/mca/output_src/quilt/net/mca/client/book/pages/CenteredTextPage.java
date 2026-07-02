package quilt.net.mca.client.book.pages;

import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5481;
import quilt.net.mca.client.gui.ExtendedBookScreen;

public class CenteredTextPage extends TextPage {
   public CenteredTextPage(String name, int page) {
      super(name, page);
   }

   public CenteredTextPage(String content) {
      super(content);
   }

   @Override
   public void render(ExtendedBookScreen screen, class_332 context, int mouseX, int mouseY, float delta) {
      if (this.content != null) {
         class_327 textRenderer = screen.getTextRenderer();
         int l = Math.min(14, this.getCachedPage(screen).size());
         int i = (screen.field_22789 - 192) / 2;

         for (int m = 0; m < l; m++) {
            class_5481 orderedText = this.getCachedPage(screen).get(m);
            int x = i + 36;
            context.method_51430(
               textRenderer, orderedText, x + 57 - textRenderer.method_30880(orderedText) / 2, 32 + (m + 7 - l / 2) * 9, 0, screen.getBook().hasTextShadow()
            );
         }
      }
   }
}
