package yesman.epicfight.client.gui.datapack.widgets;

import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class CheckBox extends AbstractWidget implements DataBindingComponent<Boolean, Boolean> {
   private final Font font;
   private final boolean defaultVal;
   private Consumer<Boolean> responder;
   private Boolean value;
   private int x1;
   private int x2;
   private int y1;
   private int y2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public CheckBox(
      Font font,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontal,
      ResizableComponent.VerticalSizing vertical,
      Boolean defaultVal,
      Component title,
      Consumer<Boolean> responder
   ) {
      super(x1, y1, x2, y2, title);
      this.font = font;
      this.defaultVal = defaultVal == null ? false : defaultVal;
      this.responder = responder;
      if (defaultVal != null) {
         this._setValue(defaultVal);
      } else {
         this.value = defaultVal;
      }

      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.horizontalSizingOption = horizontal;
      this.verticalSizingOption = vertical;
   }

   public boolean m_6375_(double x, double y, int button) {
      if (this.f_93623_ && this.f_93624_ && this.m_7972_(button)) {
         boolean flag = this.m_93680_(x, y);
         if (flag) {
            this.m_5716_(x, y);
            return true;
         }
      }

      return false;
   }

   protected boolean m_93680_(double x, double y) {
      return this.f_93623_ && this.f_93624_ && x >= this._getX() && y >= this._getY() && x < this._getX() + this.f_93618_ && y < this._getY() + this.f_93619_;
   }

   public void m_5716_(double x, double y) {
      this._setValue(this.value == null ? !this.defaultVal : !this.value);
   }

   public boolean m_5953_(double x, double y) {
      int rectangleLength = Math.min(this._getWidth(), this._getHeight());
      return this.f_93623_
         && this.f_93624_
         && x >= this._getX()
         && y >= this._getY()
         && x < this._getX() + rectangleLength
         && y < this._getY() + rectangleLength;
   }

   public void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      int rectangleLength = Math.min(this._getWidth(), this._getHeight());
      int outlineColor = this.m_93696_() ? -1 : (this.m_142518_() ? -6250336 : -12566463);
      guiGraphics.m_280509_(this._getX(), this._getY(), this._getX() + rectangleLength, this._getY() + rectangleLength, outlineColor);
      guiGraphics.m_280509_(this._getX() + 1, this._getY() + 1, this._getX() + rectangleLength - 1, this._getY() + rectangleLength - 1, -16777216);
      if (this.value == null ? this.defaultVal : this.value) {
         guiGraphics.m_280509_(this._getX() + 2, this._getY() + 2, this._getX() + rectangleLength - 2, this._getY() + rectangleLength - 2, -1);
      }

      int fontColor = this.m_142518_() ? 16777215 : 4210752;
      guiGraphics.m_280614_(this.font, this._getMessage(), this._getX() + rectangleLength + 4, this._getY() + this.f_93619_ / 2 - 9 / 2 + 1, fontColor, false);
   }

   protected void m_168797_(NarrationElementOutput narrationElementInput) {
      narrationElementInput.m_169146_(NarratedElementType.TITLE, this.m_5646_());
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
   public void _setResponder(Consumer<Boolean> responder) {
      this.responder = responder;
   }

   @Override
   public Consumer<Boolean> _getResponder() {
      return this.responder;
   }

   public void _setValue(Boolean value) {
      this.value = value;
      if (this.responder != null) {
         this.responder.accept(value == null ? this.defaultVal : value);
      }
   }

   public Boolean _getValue() {
      return this.value;
   }

   @Override
   public void reset() {
      this.value = this.defaultVal;
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
