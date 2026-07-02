package forge.net.mca.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.resources.ResourceLocation;

public class ExtendedPageTurnWidget extends PageButton {
   private final ResourceLocation texture;
   private final boolean isNextPageButton;

   public ExtendedPageTurnWidget(int x, int y, boolean isNextPageButton, OnPress action, boolean playPageTurnSound, ResourceLocation texture) {
      super(x, y, isNextPageButton, action, playPageTurnSound);
      this.isNextPageButton = isNextPageButton;
      this.texture = texture;
   }

   public void m_87963_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      int i = 0;
      int j = 192;
      if (this.m_274382_()) {
         i += 23;
      }

      if (!this.isNextPageButton) {
         j += 13;
      }

      context.m_280218_(this.texture, this.m_252754_(), this.m_252907_(), i, j, 23, 13);
   }
}
