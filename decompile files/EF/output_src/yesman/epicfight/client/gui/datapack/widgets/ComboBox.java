package yesman.epicfight.client.gui.datapack.widgets;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ComboBox<T> extends AbstractWidget implements DataBindingComponent<T, T> {
   private final ComboBox<T>.ComboItemList comboItemList;
   private final Font font;
   private final int maxRows;
   private Consumer<T> responder;
   private boolean useResponder = true;
   private boolean listOpened;
   private int x1;
   private int x2;
   private int y1;
   private int y2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public ComboBox(
      Screen parent,
      Font font,
      int x1,
      int x2,
      int y1,
      int y2,
      ResizableComponent.HorizontalSizing horizontal,
      ResizableComponent.VerticalSizing vertical,
      int maxRows,
      Component title,
      Collection<T> items,
      Function<T, String> displayStringMapper,
      Consumer<T> responder
   ) {
      super(x1, y1, x2, y2, title);
      this.font = font;
      this.maxRows = Math.min(maxRows, items.size());
      this.responder = responder;
      this.comboItemList = new ComboBox.ComboItemList(parent.getMinecraft(), this.maxRows, 15);

      for (T item : items) {
         this.comboItemList.addEntry(item, displayStringMapper.apply(item));
      }

      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.horizontalSizingOption = horizontal;
      this.verticalSizingOption = vertical;
      this.relocateComboList();
   }

   public boolean m_6375_(double x, double y, int button) {
      if (!this.f_93623_ || !this.f_93624_) {
         return false;
      }

      if (this.listOpened && this.comboItemList.m_6375_(x, y, button)) {
         if (x < this.comboItemList.m_5756_() || x > this.comboItemList.m_5756_() + 6) {
            this.listOpened = false;
         }

         this.m_7435_(Minecraft.m_91087_().m_91106_());
         return true;
      } else {
         if (this.m_7972_(button)) {
            boolean flag = this.m_93680_(x, y);
            if (flag) {
               this.m_5716_(x, y);
               return true;
            }
         }

         return false;
      }
   }

   public boolean m_6050_(double x, double y, double amount) {
      return this.listOpened ? this.comboItemList.m_6050_(x, y, amount) : false;
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.listOpened ? this.comboItemList.m_7979_(mouseX, mouseY, button, deltaX, deltaY) : false;
   }

   protected boolean m_93680_(double x, double y) {
      return this.f_93623_ && this.f_93624_ && x >= this._getX() && y >= this._getY() && x < this._getX() + this.f_93618_ && y < this._getY() + this.f_93619_;
   }

   public boolean m_5953_(double x, double y) {
      return this.listOpened && this.comboItemList.m_5953_(x, y)
         ? true
         : this.f_93623_
            && this.f_93624_
            && x >= this._getX()
            && y >= this._getY()
            && x < this._getX() + this.f_93618_
            && y < this._getY() + this.f_93619_ * (this.maxRows + 1);
   }

   @Override
   public void _setX(int x) {
      super.m_252865_(x);
      this.relocateComboList();
   }

   @Override
   public void _setY(int y) {
      super.m_253211_(y);
      this.relocateComboList();
   }

   public void m_93674_(int width) {
      this.f_93618_ = width;
      int left = this.comboItemList.getLeft();
      this.comboItemList.m_93437_(width, this.comboItemList.getHeight(), this.comboItemList.getTop(), this.comboItemList.getBottom());
      this.comboItemList.m_93507_(left);
   }

   private void relocateComboList() {
      int entryHeight = 15;
      int possibleTopPosition = this._getY() - (this.f_93619_ * this.maxRows + 1);
      int possibleBottomPosition = this._getY() + this.f_93619_ + this.f_93619_ * this.maxRows + 1;
      int bottomSpace = Minecraft.m_91087_().m_91268_().m_85446_() - possibleBottomPosition;
      int topSpace = possibleTopPosition;
      if (bottomSpace < topSpace) {
         this.comboItemList.m_93437_(this.f_93618_, entryHeight * this.maxRows, this._getY() - (entryHeight * this.maxRows + 1), this._getY() - 1);
      } else {
         this.comboItemList
            .m_93437_(
               this.f_93618_, entryHeight * this.maxRows, this._getY() + this.f_93619_ + 1, this._getY() + this.f_93619_ + entryHeight * this.maxRows + 1
            );
      }

      this.comboItemList.m_93507_(this._getX());
   }

   public void m_5716_(double x, double y) {
      if (this.arrowClicked(x, y)) {
         this.m_7435_(Minecraft.m_91087_().m_91106_());
         this.listOpened = !this.listOpened;
      } else if (this.listOpened) {
         this.listOpened = false;
         this.m_7435_(Minecraft.m_91087_().m_91106_());
      }
   }

   private boolean arrowClicked(double x, double y) {
      int openPressed = this._getX() + this.f_93618_ - 14;
      return this.f_93623_ && this.f_93624_ && x >= openPressed && y >= this._getY() && x < this._getX() + this.f_93618_ && y < this._getY() + this.f_93619_;
   }

   public void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      int outlineColor = this.m_93696_() ? -1 : (this.m_142518_() ? -6250336 : -12566463);
      guiGraphics.m_280509_(this._getX() - 1, this._getY() - 1, this._getX() + this.f_93618_ + 1, this._getY() + this.f_93619_ + 1, outlineColor);
      guiGraphics.m_280509_(this._getX(), this._getY(), this._getX() + this.f_93618_, this._getY() + this.f_93619_, -16777216);
      String correctedString = this.font
         .m_92834_(
            this.comboItemList.m_93511_() == null ? "" : ((ComboBox.ComboItemList.ComboItemEntry)this.comboItemList.m_93511_()).displayName, this.f_93618_ - 10
         );
      int fontColor = this.m_142518_() ? 16777215 : 4210752;
      guiGraphics.m_280614_(this.font, Component.m_237113_(correctedString), this._getX() + 4, this._getY() + this.f_93619_ / 2 - 9 / 2 + 1, fontColor, false);
      guiGraphics.m_280614_(
         this.font, Component.m_237113_("▼"), this._getX() + this.f_93618_ - 8, this._getY() + this.f_93619_ / 2 - 9 / 2 + 1, fontColor, false
      );
      if (this.listOpened) {
         guiGraphics.m_280168_().m_85836_();
         guiGraphics.m_280168_().m_252880_(0.0F, 0.0F, 10.0F);
         this.comboItemList.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
         guiGraphics.m_280168_().m_85849_();
      }
   }

   protected MutableComponent m_5646_() {
      Component component = this._getMessage();
      return Component.m_237110_("gui.epicfight.narrate.comboBox", new Object[]{component});
   }

   protected void m_168797_(NarrationElementOutput narrationElementInput) {
      narrationElementInput.m_169146_(NarratedElementType.TITLE, this.m_5646_());
   }

   public void m_93692_(boolean focused) {
      super.m_93692_(focused);
      if (!focused) {
         this.listOpened = false;
      }
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
   public void _setResponder(Consumer<T> responder) {
      this.responder = responder;
   }

   @Override
   public Consumer<T> _getResponder() {
      return this.responder;
   }

   @Override
   public void _setValue(T value) {
      this.comboItemList.setSelected(value);
      if (this.responder != null && this.useResponder) {
         this.responder.accept(value);
      }
   }

   @Override
   public T _getValue() {
      return (T)(this.comboItemList.m_93511_() == null ? null : ((ComboBox.ComboItemList.ComboItemEntry)this.comboItemList.m_93511_()).item);
   }

   @Override
   public void reset() {
      this.comboItemList.setSelected(null);
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

   class ComboItemList extends ObjectSelectionList<ComboBox<T>.ComboItemList.ComboItemEntry> {
      private final Map<T, ComboBox<T>.ComboItemList.ComboItemEntry> entryMap = Maps.newHashMap();

      public ComboItemList(Minecraft minecraft, int maxRows, int itemHeight) {
         super(minecraft, ComboBox.this.f_93618_, ComboBox.this.f_93619_, 0, itemHeight * maxRows, itemHeight);
         this.m_93496_(false);
         this.m_93473_(false, 0);
         this.m_93488_(false);
      }

      public void addEntry(T item, String displayName) {
         ComboBox<T>.ComboItemList.ComboItemEntry entry = new ComboBox.ComboItemList.ComboItemEntry(item, displayName);
         this.entryMap.put(item, entry);
         this.m_7085_(entry);
      }

      public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
         guiGraphics.m_280509_(this.f_93393_ - 1, this.f_93390_ - 1, this.f_93392_ + 1, this.f_93391_ + 1, -1);
         guiGraphics.m_280509_(this.f_93393_, this.f_93390_, this.f_93392_, this.f_93391_, -16777216);
         super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
      }

      public void setSelected(T item) {
         this.m_6987_((ComboBox.ComboItemList.ComboItemEntry)this.entryMap.get(item));
      }

      public int m_5759_() {
         return this.f_93388_;
      }

      protected int m_5756_() {
         return this.f_93392_ - 6;
      }

      public int m_93518_() {
         return Math.max(0, this.m_5775_() - (this.f_93391_ - this.f_93390_));
      }

      protected int m_7610_(int row) {
         return this.f_93390_ + 2 - (int)this.m_93517_() + row * this.f_93387_;
      }

      class ComboItemEntry extends Entry<ComboBox<T>.ComboItemList.ComboItemEntry> {
         private final Object item;
         private final String displayName;

         protected ComboItemEntry(T item, String displayName) {
            this.item = item;
            this.displayName = displayName;
         }

         public boolean m_6375_(double mouseX, double mouseY, int button) {
            if (button == 0) {
               ComboItemList.this.m_6987_(this);
               if (ComboBox.this.responder != null) {
                  ComboBox.this.responder.accept((T)this.item);
               }

               return true;
            } else {
               return false;
            }
         }

         public Component m_142172_() {
            return Component.m_237119_();
         }

         public void m_6311_(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks
         ) {
            guiGraphics.m_280056_(ComboBox.this.font, this.displayName, left + 2, top + 1, 16777215, false);
         }

         public T getItem() {
            return (T)this.item;
         }
      }
   }
}
