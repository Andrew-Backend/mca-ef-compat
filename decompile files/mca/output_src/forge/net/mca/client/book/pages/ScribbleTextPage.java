package forge.net.mca.client.book.pages;

import com.mojang.blaze3d.systems.RenderSystem;
import forge.net.mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ScribbleTextPage extends TextPage {
   final ResourceLocation scribble;

   public ScribbleTextPage(ResourceLocation scribble, String name, int page) {
      super(name, page);
      this.scribble = scribble;
   }

   public ScribbleTextPage(ResourceLocation scribble, String text) {
      super(text);
      this.scribble = scribble;
   }

   @Override
   public void render(ExtendedBookScreen screen, GuiGraphics context, int mouseX, int mouseY, float delta) {
      int i = (screen.f_96543_ - 192) / 2;
      RenderSystem.enableBlend();
      context.m_280163_(this.scribble, i + 28, 32, 0.0F, 0.0F, 128, 128, 128, 128);
      RenderSystem.disableBlend();
      super.render(screen, context, mouseX, mouseY, delta);
   }
}
