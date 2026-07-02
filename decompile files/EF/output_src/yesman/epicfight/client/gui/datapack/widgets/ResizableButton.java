package yesman.epicfight.client.gui.datapack.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

public class ResizableButton extends Button implements ResizableComponent {
   private int x1;
   private int x2;
   private int y1;
   private int y2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public ResizableButton(ResizableButton.Builder builder) {
      super(builder);
      this.x1 = builder.x1;
      this.x2 = builder.x2;
      this.y1 = builder.y1;
      this.y2 = builder.y2;
      this.horizontalSizingOption = builder.horizontalSizing;
      this.verticalSizingOption = builder.verticalSizing;
   }

   public static ResizableButton.Builder builder(Component title, OnPress onPress) {
      return new ResizableButton.Builder(title, onPress);
   }

   @Override
   public void _renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      super.m_87963_(guiGraphics, mouseX, mouseY, partialTicks);
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
   public void _tick() {
   }

   public static class Builder extends net.minecraft.client.gui.components.Button.Builder {
      private int x1;
      private int x2;
      private int y1;
      private int y2;
      private ResizableComponent.HorizontalSizing horizontalSizing = ResizableComponent.HorizontalSizing.LEFT_WIDTH;
      private ResizableComponent.VerticalSizing verticalSizing = null;

      public Builder(Component title, OnPress onPress) {
         super(title, onPress);
      }

      public ResizableButton.Builder x1(int x1) {
         this.x1 = x1;
         return this;
      }

      public ResizableButton.Builder x2(int x2) {
         this.x2 = x2;
         return this;
      }

      public ResizableButton.Builder y1(int y1) {
         this.y1 = y1;
         return this;
      }

      public ResizableButton.Builder y2(int y2) {
         this.y2 = y2;
         return this;
      }

      public ResizableButton.Builder horizontalSizing(ResizableComponent.HorizontalSizing horizontalSizing) {
         this.horizontalSizing = horizontalSizing;
         return this;
      }

      public ResizableButton.Builder verticalSizing(ResizableComponent.VerticalSizing verticalSizing) {
         this.verticalSizing = verticalSizing;
         return this;
      }

      public ResizableButton.Builder pos(int x, int y) {
         super.m_252794_(x, y);
         this.x1 = x;
         this.y1 = y;
         return this;
      }

      public ResizableButton.Builder width(int width) {
         super.m_252780_(width);
         this.x2 = width;
         return this;
      }

      public ResizableButton.Builder size(int width, int height) {
         super.m_253046_(width, height);
         this.x2 = width;
         this.y2 = height;
         return this;
      }

      public ResizableButton.Builder bounds(int x, int y, int width, int height) {
         return this.pos(x, y).size(width, height);
      }

      public ResizableButton build() {
         return new ResizableButton(this);
      }
   }
}
