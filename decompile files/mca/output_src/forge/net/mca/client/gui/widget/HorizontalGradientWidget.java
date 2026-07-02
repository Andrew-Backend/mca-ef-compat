package forge.net.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public class HorizontalGradientWidget extends HorizontalColorPickerWidget {
   private final Supplier<float[]> startColorSupplier;
   private final Supplier<float[]> endColorSupplier;

   public HorizontalGradientWidget(
      int x,
      int y,
      int width,
      int height,
      double valueX,
      Supplier<float[]> startColorSupplier,
      Supplier<float[]> endColorSupplier,
      ColorPickerWidget.DualConsumer<Double, Double> consumer
   ) {
      super(x, y, width, height, valueX, null, consumer);
      this.startColorSupplier = startColorSupplier;
      this.endColorSupplier = endColorSupplier;
   }

   public void m_88315_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(GameRenderer::m_172811_);
      Tesselator tessellator = Tesselator.m_85913_();
      BufferBuilder builder = tessellator.m_85915_();
      builder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
      float[] startColor = this.startColorSupplier.get();
      float[] endColor = this.endColorSupplier.get();
      float z = 0.0F;
      PoseStack matrices = context.m_280168_();
      Matrix4f matrix = matrices.m_85850_().m_252922_();
      builder.m_252986_(matrix, (float)this.m_252754_() + this.f_93618_, this.m_252907_(), z)
         .m_85950_(endColor[0], endColor[1], endColor[2], endColor[3])
         .m_5752_();
      builder.m_252986_(matrix, this.m_252754_(), this.m_252907_(), z).m_85950_(startColor[0], startColor[1], startColor[2], startColor[3]).m_5752_();
      builder.m_252986_(matrix, this.m_252754_(), (float)this.m_252907_() + this.f_93619_, z)
         .m_85950_(startColor[0], startColor[1], startColor[2], startColor[3])
         .m_5752_();
      builder.m_252986_(matrix, (float)this.m_252754_() + this.f_93618_, (float)this.m_252907_() + this.f_93619_, z)
         .m_85950_(endColor[0], endColor[1], endColor[2], endColor[3])
         .m_5752_();
      tessellator.m_85914_();
      RenderSystem.disableBlend();
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
   }
}
