package yesman.epicfight.client.gui.datapack.widgets;

import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Static extends AbstractWidget implements ResizableComponent {
   private final Screen owner;
   private final Font font;
   private final Component tooltip;
   private int fontColor = -1;
   private int x1;
   private int x2;
   private int y1;
   private int y2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public Static(
      Screen owner,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontal,
      ResizableComponent.VerticalSizing vertical,
      String translateKey
   ) {
      this(owner, x1, x2, y1, y2, horizontal, vertical, Component.m_237115_(translateKey), Component.m_237115_(translateKey + ".tooltip"));
   }

   public Static(
      Screen owner,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontal,
      ResizableComponent.VerticalSizing vertical,
      Component message
   ) {
      this(owner, x1, x2, y1, y2, horizontal, vertical, message, null);
   }

   public Static(
      Screen owner,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontal,
      ResizableComponent.VerticalSizing vertical,
      Component message,
      @Nullable Component tooltip
   ) {
      super(x1, y1, x2, y2, message);
      this.owner = owner;
      this.font = this.owner.getMinecraft().f_91062_;
      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.horizontalSizingOption = horizontal;
      this.verticalSizingOption = vertical;
      this.tooltip = tooltip;
      this.m_257544_(this.tooltip == null ? null : Tooltip.m_257550_(this.tooltip));
   }

   public void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      String correctedString = this._getMessage() == null ? "" : this.font.m_92834_(this._getMessage().getString(), this._getWidth());
      guiGraphics.m_280056_(this.font, correctedString, this._getX(), this._getY() + this.f_93619_ / 2 - 9 / 2, this.fontColor, false);
   }

   protected void m_168797_(NarrationElementOutput p_259858_) {
   }

   public boolean m_5953_(double mouseX, double mouseY) {
      return mouseX >= this.m_252754_()
         && mouseY >= this.m_252907_()
         && mouseX < this.m_252754_() + this.font.m_92852_(this._getMessage())
         && mouseY < this.m_252907_() + this.f_93619_;
   }

   public boolean m_6375_(double mouseX, double mouseY, int action) {
      return false;
   }

   public boolean m_6348_(double mouseX, double mouseY, int action) {
      return false;
   }

   public boolean m_7979_(double mouseX, double mouseY, int action, double p_93648_, double p_93649_) {
      return false;
   }

   public void setColor(int r, int g, int b) {
      this.fontColor = 0xFF000000 | r << 24 | g << 16 | b << 8;
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
}
