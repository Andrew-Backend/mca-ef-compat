package fabric.net.mca.client.book.pages;

import com.mojang.blaze3d.systems.RenderSystem;
import fabric.net.mca.client.gui.ExtendedBookScreen;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public class ScribbleTextPage extends TextPage {
   final class_2960 scribble;

   public ScribbleTextPage(class_2960 scribble, String name, int page) {
      super(name, page);
      this.scribble = scribble;
   }

   public ScribbleTextPage(class_2960 scribble, String text) {
      super(text);
      this.scribble = scribble;
   }

   @Override
   public void render(ExtendedBookScreen screen, class_332 context, int mouseX, int mouseY, float delta) {
      int i = (screen.field_22789 - 192) / 2;
      RenderSystem.enableBlend();
      context.method_25290(this.scribble, i + 28, 32, 0.0F, 0.0F, 128, 128, 128, 128);
      RenderSystem.disableBlend();
      super.render(screen, context, mouseX, mouseY, delta);
   }
}
