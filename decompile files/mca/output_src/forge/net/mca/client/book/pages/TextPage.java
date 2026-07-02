package forge.net.mca.client.book.pages;

import forge.net.mca.client.gui.ExtendedBookScreen;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.util.FormattedCharSequence;

public class TextPage extends Page {
   protected final String content;
   private Style style = Style.f_131099_;
   private List<FormattedCharSequence> cachedPage;

   public TextPage(String name, int page) {
      this.content = "{ \"translate\": \"mca.books." + name + "." + page + "\" }";
   }

   public TextPage(String content) {
      this.content = content;
   }

   protected List<FormattedCharSequence> getCachedPage(ExtendedBookScreen screen) {
      if (this.cachedPage == null) {
         FormattedText stringVisitable = FormattedText.m_130762_(this.content, this.style);

         try {
            MutableComponent text = Serializer.m_130701_(this.content);
            if (text != null) {
               text.m_130948_(this.style);
            }

            stringVisitable = text;
         } catch (Exception var4) {
         }

         if (stringVisitable == null) {
            this.cachedPage = new LinkedList<>();
         } else {
            this.cachedPage = screen.getTextRenderer().m_92923_(stringVisitable, 114);
         }
      }

      return this.cachedPage;
   }

   @Override
   public void render(ExtendedBookScreen screen, GuiGraphics context, int mouseX, int mouseY, float delta) {
      if (this.content != null) {
         int l = Math.min(14, this.getCachedPage(screen).size());
         int i = (screen.f_96543_ - 192) / 2;

         for (int m = 0; m < l; m++) {
            FormattedCharSequence orderedText = this.getCachedPage(screen).get(m);
            int x = i + 36;
            context.m_280649_(screen.getTextRenderer(), orderedText, x, 32 + m * 9, 0, screen.getBook().hasTextShadow());
         }
      }
   }

   public TextPage setStyle(Style style) {
      this.style = style;
      return this;
   }
}
