package forge.net.mca.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class NamedTextFieldWidget extends EditBox {
   private final Font textRenderer;

   public NamedTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component text) {
      super(textRenderer, x + width / 2, y, width / 2, height, text);
      this.textRenderer = textRenderer;
   }

   public void m_87963_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      super.m_87963_(context, mouseX, mouseY, delta);
      FormattedCharSequence orderedText = this.m_6035_().m_7532_();
      context.m_280648_(
         this.textRenderer, orderedText, this.m_252754_() - this.textRenderer.m_92724_(orderedText) - 4, this.m_252907_() + (this.f_93619_ - 8) / 2, 16777215
      );
   }
}
