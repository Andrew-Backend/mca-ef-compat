package forge.net.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import forge.net.mca.MCA;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ColorPickerWidget extends AbstractWidget {
   public static final ResourceLocation MCA_GUI_ICONS_TEXTURE = MCA.locate("textures/gui.png");
   private final ColorPickerWidget.DualConsumer<Double, Double> consumer;
   private final ResourceLocation texture;
   double valueX;
   double valueY;

   public ColorPickerWidget(
      int x, int y, int width, int height, double valueX, double valueY, ResourceLocation texture, ColorPickerWidget.DualConsumer<Double, Double> consumer
   ) {
      super(x, y, width, height, Component.m_237113_(""));
      this.consumer = consumer;
      this.texture = texture;
      this.valueX = valueX;
      this.valueY = valueY;
   }

   public void m_87963_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.f_93625_);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      context.m_280163_(this.texture, this.m_252754_(), this.m_252907_(), 0.0F, 0.0F, this.f_93618_, this.f_93619_, this.f_93618_, this.f_93619_);
      WidgetUtils.drawRectangle(context, this.m_252754_(), this.m_252907_(), this.m_252754_() + this.f_93618_, this.m_252907_() + this.f_93619_, -1426063361);
      context.m_280163_(
         MCA_GUI_ICONS_TEXTURE,
         (int)(this.m_252754_() + this.valueX * this.f_93618_) - 8,
         (int)(this.m_252907_() + this.valueY * this.f_93619_) - 8,
         240.0F,
         0.0F,
         16,
         16,
         256,
         256
      );
      WidgetUtils.drawRectangle(context, this.m_252754_(), this.m_252907_(), this.m_252754_() + this.f_93618_, this.m_252907_() + this.f_93619_, -1426063361);
   }

   protected void m_7212_(double mouseX, double mouseY, double deltaX, double deltaY) {
      this.update(mouseX, mouseY);
      super.m_7212_(mouseX, mouseY, deltaX, deltaY);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (this.isInArea(mouseX, mouseY)) {
         this.update(mouseX, mouseY);
      }

      return super.m_6375_(mouseX, mouseY, button);
   }

   private boolean isInArea(double mouseX, double mouseY) {
      return mouseX >= this.m_252754_()
         && mouseX <= this.m_252754_() + this.f_93618_
         && mouseY >= this.m_252907_()
         && mouseY <= this.m_252907_() + this.f_93619_;
   }

   void update(double mouseX, double mouseY) {
      this.valueX = Mth.m_14008_((mouseX - this.m_252754_()) / this.f_93618_, 0.0, 1.0);
      this.valueY = Mth.m_14008_((mouseY - this.m_252907_()) / this.f_93619_, 0.0, 1.0);
      this.consumer.apply(this.valueX, this.valueY);
   }

   protected void m_168797_(NarrationElementOutput builder) {
      this.m_168802_(builder);
   }

   public double getValueX() {
      return this.valueX;
   }

   public void setValueX(double valueX) {
      this.valueX = valueX;
   }

   public double getValueY() {
      return this.valueY;
   }

   public void setValueY(double valueY) {
      this.valueY = valueY;
   }

   @FunctionalInterface
   public interface DualConsumer<A, B> {
      void apply(A var1, B var2);
   }
}
