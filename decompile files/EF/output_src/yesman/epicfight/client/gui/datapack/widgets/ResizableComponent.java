package yesman.epicfight.client.gui.datapack.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

public interface ResizableComponent extends GuiEventListener, NarratableEntry {
   default void resize(ScreenRectangle screenRectangle) {
      if (this.getHorizontalSizingOption() != null) {
         this.getHorizontalSizingOption().resizeFunction.resize(this, screenRectangle, this.getX1(), this.getX2());
      }

      if (this.getVerticalSizingOption() != null) {
         this.getVerticalSizingOption().resizeFunction.resize(this, screenRectangle, this.getY1(), this.getY2());
      }
   }

   default ResizableComponent relocateX(ScreenRectangle screenrect, int screenX) {
      this._setX(screenX);
      if (this.getHorizontalSizingOption() == ResizableComponent.HorizontalSizing.WIDTH_RIGHT) {
         this.setX2(screenrect.m_274445_() - (screenX + this._getWidth()));
      } else {
         this.setX1(screenX);
      }

      return this;
   }

   default ResizableComponent relocateY(ScreenRectangle screenrect, int screenY) {
      this._setY(screenY);
      if (this.getVerticalSizingOption() == ResizableComponent.VerticalSizing.HEIGHT_BOTTOM) {
         this.setY2(screenrect.m_274349_() - (screenY + this._getHeight()));
      } else {
         this.setY1(screenY);
      }

      return this;
   }

   int getX1();

   int getX2();

   int getY1();

   int getY2();

   void setX1(int var1);

   void setX2(int var1);

   void setY1(int var1);

   void setY2(int var1);

   ResizableComponent.HorizontalSizing getHorizontalSizingOption();

   ResizableComponent.VerticalSizing getVerticalSizingOption();

   default AbstractWidget asWidget() {
      return (AbstractWidget)this;
   }

   void _tick();

   void _setActive(boolean var1);

   void _renderWidget(GuiGraphics var1, int var2, int var3, float var4);

   int _getX();

   int _getY();

   int _getWidth();

   int _getHeight();

   void _setX(int var1);

   void _setY(int var1);

   void _setWidth(int var1);

   void _setHeight(int var1);

   Component _getMessage();

   enum HorizontalSizing {
      LEFT_WIDTH((component, screenRectangle, v1, v2) -> {
         component._setX(screenRectangle.m_274563_() + v1);
         component._setWidth(v2);
      }),
      LEFT_RIGHT((component, screenRectangle, v1, v2) -> {
         int end = screenRectangle.m_274445_() - v2;
         int width = Math.max(end - (screenRectangle.m_274563_() + v1), 0);
         component._setX(screenRectangle.m_274563_() + v1);
         component._setWidth(width);
      }),
      WIDTH_RIGHT((component, screenRectangle, v1, v2) -> {
         int end = screenRectangle.m_274445_() - v2;
         int start = Math.max(end - v1, 0);
         component._setX(start);
         component._setWidth(v1);
      });

      ResizableComponent.ResizeFunction resizeFunction;

      HorizontalSizing(ResizableComponent.ResizeFunction resizeFunction) {
         this.resizeFunction = resizeFunction;
      }
   }

   @FunctionalInterface
   interface ResizeFunction {
      void resize(ResizableComponent var1, ScreenRectangle var2, int var3, int var4);
   }

   enum VerticalSizing {
      TOP_HEIGHT((component, screenRectangle, v1, v2) -> {
         component._setY(v1);
         component._setHeight(v2);
      }),
      TOP_BOTTOM((component, screenRectangle, v1, v2) -> {
         int end = screenRectangle.m_274349_() - v2;
         int height = Math.max(end - v1, 0);
         component._setY(v1);
         component._setHeight(height);
      }),
      HEIGHT_BOTTOM((component, screenRectangle, v1, v2) -> {
         int end = screenRectangle.m_274349_() - v2;
         int start = Math.max(end - v1, 0);
         component._setY(start);
         component._setHeight(v1);
      });

      ResizableComponent.ResizeFunction resizeFunction;

      VerticalSizing(ResizableComponent.ResizeFunction resizeFunction) {
         this.resizeFunction = resizeFunction;
      }
   }
}
