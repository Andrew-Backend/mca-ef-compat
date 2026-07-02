package forge.net.mca.client.book.pages;

import forge.net.mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

public class CenteredTextPage extends TextPage {
   public CenteredTextPage(String name, int page) {
      super(name, page);
   }

   public CenteredTextPage(String content) {
      super(content);
   }

   @Override
   public void render(ExtendedBookScreen screen, GuiGraphics context, int mouseX, int mouseY, float delta) {
      if (this.content != null) {
         Font textRenderer = screen.getTextRenderer();
         int l = Math.min(14, this.getCachedPage(screen).size());
         int i = (screen.f_96543_ - 192) / 2;

         for (int m = 0; m < l; m++) {
            FormattedCharSequence orderedText = this.getCachedPage(screen).get(m);
            int x = i + 36;
            context.m_280649_(
               textRenderer, orderedText, x + 57 - textRenderer.m_92724_(orderedText) / 2, 32 + (m + 7 - l / 2) * 9, 0, screen.getBook().hasTextShadow()
            );
         }
      }
   }
}
