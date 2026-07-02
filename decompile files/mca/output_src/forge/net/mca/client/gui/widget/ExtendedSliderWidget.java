package forge.net.mca.client.gui.widget;

import forge.net.mca.util.localization.FlowingText;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public abstract class ExtendedSliderWidget<T> extends AbstractSliderButton {
   private T oldValue;
   final Consumer<T> onApplyValue;
   protected final Supplier<Component> tooltipSupplier;

   public ExtendedSliderWidget(int x, int y, int width, int height, Component text, double value, Consumer<T> onApplyValue, Supplier<Component> tooltipSupplier) {
      super(x, y, width, height, text, value);
      this.onApplyValue = onApplyValue;
      this.tooltipSupplier = tooltipSupplier;
   }

   protected double getOpticalValue() {
      return this.f_93577_;
   }

   abstract T getValue();

   public void m_87963_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      int i = (this.m_274382_() ? 2 : 1) * 20;
      context.m_280218_(f_93617_, this.m_252754_() + (int)(this.getOpticalValue() * (this.f_93618_ - 8)), this.m_252907_(), 0, 46 + i, 4, 20);
      context.m_280218_(f_93617_, this.m_252754_() + (int)(this.getOpticalValue() * (this.f_93618_ - 8)) + 4, this.m_252907_(), 196, 46 + i, 4, 20);
      super.m_87963_(context, mouseX, mouseY, delta);
      if (this.m_274382_()) {
         this.renderTooltip(context, mouseX, mouseY);
      }
   }

   protected void m_5697_() {
      T v = this.getValue();
      if (v != this.oldValue) {
         this.oldValue = v;
         this.onApplyValue.accept(v);
      }
   }

   public void renderTooltip(GuiGraphics context, int mouseX, int mouseY) {
      assert Minecraft.m_91087_() != null;
      context.m_280666_(Minecraft.m_91087_().f_91062_, FlowingText.wrap(this.tooltipSupplier.get(), 160), mouseX, mouseY);
   }
}
