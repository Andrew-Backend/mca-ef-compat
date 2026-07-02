package forge.net.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import forge.net.mca.client.gui.InteractScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ToggleableTooltipIconButtonWidget extends ToggleableTooltipButtonWidget {
   private final int u;
   private final int v;

   public ToggleableTooltipIconButtonWidget(int x, int y, int u, int v, boolean toggle, MutableComponent tooltip, OnPress onPress) {
      super(x, y, 16, 16, toggle, Component.m_237113_(""), tooltip, onPress);
      this.u = u;
      this.v = v;
   }

   public void m_87963_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.drawIcon(context);
   }

   private void drawIcon(GuiGraphics context) {
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.f_93625_);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      int offset = this.toggle ? 0 : 16;
      context.m_280218_(InteractScreen.ICON_TEXTURES, this.m_252754_(), this.m_252907_(), this.u, this.v + offset, this.f_93618_, this.f_93619_);
   }
}
