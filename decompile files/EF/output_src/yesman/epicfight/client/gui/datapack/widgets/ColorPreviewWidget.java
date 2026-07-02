package yesman.epicfight.client.gui.datapack.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ColorPreviewWidget extends AbstractWidget implements ResizableComponent {
   private int packedColor;
   private int x1;
   private int x2;
   private int y1;
   private int y2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public ColorPreviewWidget(
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontalSizingOption,
      ResizableComponent.VerticalSizing verticalSizingOption,
      Component title
   ) {
      super(x1, y1, x2, y2, title);
      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.horizontalSizingOption = horizontalSizingOption;
      this.verticalSizingOption = verticalSizingOption;
   }

   public void setColor(int r, int g, int b) {
      this.packedColor = 0xFF000000 | r << 16 | g << 8 | b;
   }

   public void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      guiGraphics.m_280509_(this._getX(), this._getY(), this._getX() + this._getWidth(), this._getY() + this._getHeight(), -1);
      guiGraphics.m_280509_(this._getX() + 1, this._getY() + 1, this._getX() + this._getWidth() - 1, this._getY() + this._getHeight() - 1, -16777216);
      guiGraphics.m_280509_(this._getX() + 2, this._getY() + 2, this._getX() + this._getWidth() - 2, this._getY() + this._getHeight() - 2, this.packedColor);
   }

   protected void m_168797_(NarrationElementOutput p_259858_) {
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
      this.m_87963_(guiGraphics, mouseX, mouseY, partialTicks);
   }
}
