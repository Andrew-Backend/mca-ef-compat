package fabric.net.mca.client.book.pages;

import fabric.net.mca.client.gui.ExtendedBookScreen;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.class_2583;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.minecraft.class_2561.class_2562;

public class TextPage extends Page {
   protected final String content;
   private class_2583 style = class_2583.field_24360;
   private List<class_5481> cachedPage;

   public TextPage(String name, int page) {
      this.content = "{ \"translate\": \"mca.books." + name + "." + page + "\" }";
   }

   public TextPage(String content) {
      this.content = content;
   }

   protected List<class_5481> getCachedPage(ExtendedBookScreen screen) {
      if (this.cachedPage == null) {
         class_5348 stringVisitable = class_5348.method_29431(this.content, this.style);

         try {
            class_5250 text = class_2562.method_10877(this.content);
            if (text != null) {
               text.method_27696(this.style);
            }

            stringVisitable = text;
         } catch (Exception var4) {
         }

         if (stringVisitable == null) {
            this.cachedPage = new LinkedList<>();
         } else {
            this.cachedPage = screen.getTextRenderer().method_1728(stringVisitable, 114);
         }
      }

      return this.cachedPage;
   }

   @Override
   public void render(ExtendedBookScreen screen, class_332 context, int mouseX, int mouseY, float delta) {
      if (this.content != null) {
         int l = Math.min(14, this.getCachedPage(screen).size());
         int i = (screen.field_22789 - 192) / 2;

         for (int m = 0; m < l; m++) {
            class_5481 orderedText = this.getCachedPage(screen).get(m);
            int x = i + 36;
            context.method_51430(screen.getTextRenderer(), orderedText, x, 32 + m * 9, 0, screen.getBook().hasTextShadow());
         }
      }
   }

   public TextPage setStyle(class_2583 style) {
      this.style = style;
      return this;
   }
}
