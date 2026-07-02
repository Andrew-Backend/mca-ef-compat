package yesman.epicfight.client.gui.widgets;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import org.joml.Matrix4f;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;

public class ColorSlider extends AbstractSliderButton implements ResizableComponent {
   public static final int[] RGB_COMBINATIONS = new int[]{-65536, -256, -16711936, -16711681, -16776961, -65281, -65536};
   private final BiConsumer<Double, Integer> valueChangeCallback;
   private final int[] colors;
   private final Font font;
   private final ColorSlider.Style style;
   private int x1;
   private int x2;
   private int y1;
   private int y2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public ColorSlider(
      Font font,
      int x,
      int y,
      int width,
      int height,
      Component message,
      ColorSlider.Style style,
      double initColor,
      BiConsumer<Double, Integer> valueChangeCallback
   ) {
      this(font, x, y, width, height, message, style, initColor, valueChangeCallback, RGB_COMBINATIONS);
   }

   public ColorSlider(
      Font font,
      int x,
      int y,
      int width,
      int height,
      Component message,
      ColorSlider.Style style,
      double initColor,
      BiConsumer<Double, Integer> valueChangeCallback,
      int... colors
   ) {
      super(x, y, width, height, message, initColor);
      this.valueChangeCallback = valueChangeCallback;
      this.font = font;
      this.colors = colors;
      this.style = style;
      this.horizontalSizingOption = null;
      this.verticalSizingOption = null;
   }

   public ColorSlider(
      Font font,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontalSizing,
      ResizableComponent.VerticalSizing verticalSizing,
      Component message,
      ColorSlider.Style style,
      double initColor,
      BiConsumer<Double, Integer> valueChangeCallback
   ) {
      this(font, x1, x2, y1, y2, horizontalSizing, verticalSizing, message, style, initColor, valueChangeCallback, RGB_COMBINATIONS);
   }

   public ColorSlider(
      Font font,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontalSizing,
      ResizableComponent.VerticalSizing verticalSizing,
      Component message,
      ColorSlider.Style style,
      double initColor,
      BiConsumer<Double, Integer> valueChangeCallback,
      int... colors
   ) {
      super(x1, y1, x2, y2, message, initColor);
      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.valueChangeCallback = valueChangeCallback;
      this.font = font;
      this.colors = colors;
      this.style = style;
      this.horizontalSizingOption = horizontalSizing;
      this.verticalSizingOption = verticalSizing;
   }

   public void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.style.backgroundRenderer.render(this, guiGraphics);
      this.style.selectorRenderer.render(this, guiGraphics);
      this.style.titleRenderer.render(this, guiGraphics, this.font);
   }

   public void changeColor(int color, int index) {
      this.colors[index] = color;
   }

   protected void m_5697_() {
      this.valueChangeCallback.accept(this.f_93577_, this.getColor());
   }

   protected void m_5695_() {
   }

   public double getPosition() {
      return this.f_93577_;
   }

   public int getColor() {
      return sliderPositionToColor(this.f_93577_, this.colors);
   }

   public static int sliderPositionToColor(double value, int[] colors) {
      int packedColor = 0;
      int colorBlocks = colors.length - 1;

      for (int i = 0; i < colorBlocks; i++) {
         double min = 1.0 / colorBlocks * i;
         double max = 1.0 / colorBlocks * (i + 1);
         if (value >= min && value <= max) {
            double lerpFactor = (value - min) / (max - min);
            int startColor = colors[i];
            int endColor = colors[i + 1];
            int f = startColor >> 24 & 0xFF;
            int f1 = startColor >> 16 & 0xFF;
            int f2 = startColor >> 8 & 0xFF;
            int f3 = startColor & 0xFF;
            int f4 = endColor >> 24 & 0xFF;
            int f5 = endColor >> 16 & 0xFF;
            int f6 = endColor >> 8 & 0xFF;
            int f7 = endColor & 0xFF;
            int r = (int)Mth.m_14139_(lerpFactor, f, f4);
            int g = (int)Mth.m_14139_(lerpFactor, f1, f5);
            int b = (int)Mth.m_14139_(lerpFactor, f2, f6);
            int a = (int)Mth.m_14139_(lerpFactor, f3, f7);
            packedColor = r << 24 | g << 16 | b << 8 | a;
         }
      }

      return packedColor;
   }

   public static int rgbColor(double position) {
      return sliderPositionToColor(position, RGB_COMBINATIONS);
   }

   private static void fillGradient(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int startColor, int endColor) {
      float f = ARGB32.m_13655_(startColor) / 255.0F;
      float f1 = ARGB32.m_13665_(startColor) / 255.0F;
      float f2 = ARGB32.m_13667_(startColor) / 255.0F;
      float f3 = ARGB32.m_13669_(startColor) / 255.0F;
      float f4 = ARGB32.m_13655_(endColor) / 255.0F;
      float f5 = ARGB32.m_13665_(endColor) / 255.0F;
      float f6 = ARGB32.m_13667_(endColor) / 255.0F;
      float f7 = ARGB32.m_13669_(endColor) / 255.0F;
      Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();
      VertexConsumer consumer = guiGraphics.m_280091_().m_6299_(RenderType.m_285907_());
      consumer.m_252986_(matrix4f, x1, y2, 0.0F).m_85950_(f1, f2, f3, f).m_5752_();
      consumer.m_252986_(matrix4f, x2, y2, 0.0F).m_85950_(f5, f6, f7, f4).m_5752_();
      consumer.m_252986_(matrix4f, x2, y1, 0.0F).m_85950_(f5, f6, f7, f4).m_5752_();
      consumer.m_252986_(matrix4f, x1, y1, 0.0F).m_85950_(f1, f2, f3, f).m_5752_();
   }

   private static void fillRhombus(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY, int pColor) {
      Matrix4f matrix4f = guiGraphics.m_280168_().m_85850_().m_252922_();
      float a = ARGB32.m_13655_(pColor) / 255.0F;
      float r = ARGB32.m_13665_(pColor) / 255.0F;
      float g = ARGB32.m_13667_(pColor) / 255.0F;
      float b = ARGB32.m_13669_(pColor) / 255.0F;
      int centerX = minX + (maxX - minX) / 2;
      int centerY = minY + (maxY - minY) / 2;
      VertexConsumer vertexconsumer = guiGraphics.m_280091_().m_6299_(EpicFightRenderTypes.guiTriangle());
      vertexconsumer.m_252986_(matrix4f, centerX, minY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      vertexconsumer.m_252986_(matrix4f, minX, centerY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      vertexconsumer.m_252986_(matrix4f, centerX, maxY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      vertexconsumer.m_252986_(matrix4f, centerX, maxY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      vertexconsumer.m_252986_(matrix4f, maxX, centerY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      vertexconsumer.m_252986_(matrix4f, centerX, minY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      guiGraphics.m_280262_();
   }

   @Override
   public void setX1(int x1) {
      this.x1 = x1;
   }

   @Override
   public void setX2(int x2) {
      this.x2 = x2;
   }

   @Override
   public void setY1(int y1) {
      this.y1 = y1;
   }

   @Override
   public void setY2(int y2) {
      this.y2 = y2;
   }

   @Override
   public int getX1() {
      return this.x1;
   }

   @Override
   public int getX2() {
      return this.x2;
   }

   @Override
   public int getY1() {
      return this.y1;
   }

   @Override
   public int getY2() {
      return this.y2;
   }

   @Override
   public ResizableComponent.HorizontalSizing getHorizontalSizingOption() {
      return this.horizontalSizingOption;
   }

   @Override
   public ResizableComponent.VerticalSizing getVerticalSizingOption() {
      return this.verticalSizingOption;
   }

   @Override
   public void _setActive(boolean active) {
      this.f_93623_ = active;
   }

   @Override
   public void _tick() {
   }

   @Override
   public int _getX() {
      return this.m_252754_();
   }

   @Override
   public int _getY() {
      return this.m_252907_();
   }

   @Override
   public int _getWidth() {
      return this.m_5711_();
   }

   @Override
   public int _getHeight() {
      return this.m_93694_();
   }

   @Override
   public void _setX(int x) {
      this.m_252865_(x);
   }

   @Override
   public void _setY(int y) {
      this.m_253211_(y);
   }

   @Override
   public void _setWidth(int width) {
      this.m_93674_(width);
   }

   @Override
   public void _setHeight(int height) {
      this.setHeight(height);
   }

   @Override
   public Component _getMessage() {
      return this.m_6035_();
   }

   @Override
   public void _renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   @FunctionalInterface
   interface Background {
      void render(ColorSlider var1, GuiGraphics var2);
   }

   @FunctionalInterface
   interface Selector {
      void render(ColorSlider var1, GuiGraphics var2);
   }

   public enum Style {
      CLASSIC(
         (widget, guiGraphics) -> {
            int minX = widget.m_252754_() + (int)(widget.f_93577_ * (widget.f_93618_ - 8));
            guiGraphics.m_280509_(minX, widget.m_252907_(), minX + 8, widget.m_252907_() + 20, -1);
            guiGraphics.m_280509_(
               minX + 1, widget.m_252907_() + 1, minX + 7, widget.m_252907_() + 19, ColorSlider.sliderPositionToColor(widget.f_93577_, widget.colors)
            );
         },
         (widget, guiGraphics) -> {
            int y1 = widget.m_252907_();
            int y2 = widget.m_252907_() + widget.f_93619_;
            int blocks = widget.colors.length - 1;
            int prevEnd = widget.m_252754_();

            for (int i = 0; i < widget.colors.length - 1; i++) {
               int nextX = widget.m_252754_() + widget.f_93618_ * (i + 1) / blocks;
               ColorSlider.fillGradient(guiGraphics, prevEnd, y1, nextX, y2, widget.colors[i], widget.colors[i + 1]);
               prevEnd = nextX;
            }
         },
         (widget, guiGraphics, font) -> {
            int j = widget.getFGColor();
            guiGraphics.m_280653_(
               widget.font,
               widget.m_6035_(),
               widget.m_252754_() + widget.f_93618_ / 2,
               widget.m_252907_() + (widget.f_93619_ - 8) / 2,
               j | Mth.m_14167_(widget.f_93625_ * 255.0F) << 24
            );
         }
      ),
      SIMPLE((widget, guiGraphics) -> {
         int outlineColor = widget.m_198029_() ? -1 : -16777216;
         int minX = widget.m_252754_() + (int)(widget.f_93577_ * (widget.f_93618_ - 8));
         int centerY = widget.m_252907_() + widget.m_93694_() / 2;
         ColorSlider.fillRhombus(guiGraphics, minX, centerY - 5, minX + 10, centerY + 5, outlineColor);
         ColorSlider.fillRhombus(guiGraphics, minX + 1, centerY - 4, minX + 9, centerY + 4, ColorSlider.sliderPositionToColor(widget.f_93577_, widget.colors));
      }, (widget, guiGraphics) -> {
         int y1 = widget.m_252907_() + widget.f_93619_ / 2 - 1;
         int y2 = widget.m_252907_() + widget.f_93619_ / 2 + 1;
         int blocks = widget.colors.length - 1;
         int prevEnd = widget.m_252754_();
         guiGraphics.m_280509_(widget.m_252754_() - 1, y1 - 1, widget.m_252754_() + widget.f_93618_ + 1, y2 + 1, -16777216);

         for (int i = 0; i < widget.colors.length - 1; i++) {
            int nextX = widget.m_252754_() + widget.f_93618_ * (i + 1) / blocks;
            ColorSlider.fillGradient(guiGraphics, prevEnd, y1, nextX, y2, widget.colors[i], widget.colors[i + 1]);
            prevEnd = nextX;
         }
      }, (widget, guiGraphics, font) -> {});

      ColorSlider.Selector selectorRenderer;
      ColorSlider.Background backgroundRenderer;
      ColorSlider.Title titleRenderer;

      Style(ColorSlider.Selector selectorRenderer, ColorSlider.Background backgroundRenderer, ColorSlider.Title titleRenderer) {
         this.selectorRenderer = selectorRenderer;
         this.backgroundRenderer = backgroundRenderer;
         this.titleRenderer = titleRenderer;
      }
   }

   @FunctionalInterface
   interface Title {
      void render(ColorSlider var1, GuiGraphics var2, Font var3);
   }
}
