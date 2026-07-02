package yesman.epicfight.client.gui.datapack.widgets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.IForgeRegistry;
import org.lwjgl.opengl.GL11;
import yesman.epicfight.api.utils.ParseUtil;

public class Grid extends ObjectSelectionList<Grid.Row> implements DataBindingComponent<Object, Object> {
   private final Screen owner;
   private final Map<String, Grid.Column<?, ?>> columns = Maps.newLinkedHashMap();
   private final List<ResizableButton> rowEditButtons = Lists.newArrayList();
   private final BiConsumer<Integer, Map<String, Object>> onRowpositionChanged;
   private final boolean transparentBackground;
   private final int columnSizeSum;
   private ResizableComponent editingWidget;
   private Grid.Column<?, ?> editingColumn;
   private boolean active = true;
   private boolean rowpositionChangeEnabled = true;
   private boolean valueChangeEnabled = true;
   private int rowposition = -1;
   private int xParam1;
   private int yParam1;
   private int xParam2;
   private int yParam2;
   private final ResizableComponent.HorizontalSizing horizontalSizingOption;
   private final ResizableComponent.VerticalSizing verticalSizingOption;

   public Grid(Grid.GridBuilder gb) {
      super(gb.minecraft, gb.x2, gb.y2, gb.y1, gb.y1 + gb.y2, gb.rowHeight);
      this.owner = gb.owner;
      this.onRowpositionChanged = gb.onRowpositionChanged;
      this.transparentBackground = gb.transparentBackground;
      this.horizontalSizingOption = gb.horizontalSizing;
      this.verticalSizingOption = gb.verticalSizing;
      this.xParam1 = gb.x1;
      this.yParam1 = gb.y1;
      this.xParam2 = gb.x2;
      this.yParam2 = gb.y2;
      this.columnSizeSum = gb.columnSizeTotal;
      gb.columns.forEach(this.columns::put);
      this.resize(gb.minecraft.f_91080_.m_264198_());
      this.m_93507_(gb.x1);
      this.m_93496_(false);
      if (gb.rowEditButtons.add) {
         this.rowEditButtons
            .add(ResizableButton.builder(Component.m_237113_("+"), button -> gb.onAddPress.accept(this, button)).pos(0, 0).size(12, 12).build());
      }

      if (gb.rowEditButtons.remove) {
         this.rowEditButtons
            .add(ResizableButton.builder(Component.m_237113_("-"), button -> gb.onRemovePress.accept(this, button)).pos(0, 0).size(12, 12).build());
      }

      this.relocateButtons();
   }

   public int addRow() {
      return this.addRow(this.m_6702_().size());
   }

   public int addRow(IntConsumer onAdd) {
      return this.addRow(this.m_6702_().size(), onAdd);
   }

   public int addRow(int rowposition) {
      return this.addRow(rowposition, null);
   }

   public int addRowWithDefaultValues(Object... defaultValues) {
      return this.addRow(this.m_6702_().size(), null, defaultValues);
   }

   public int addRow(int rowposition, IntConsumer onAdd, Object... defaultValues) {
      this.editingColumn = null;
      this.editingWidget = null;
      Grid.Row row = new Grid.Row();
      this.m_6702_().add(rowposition, row);
      if (onAdd != null) {
         onAdd.accept(rowposition);
      }

      for (Entry<String, Grid.Column<?, ?>> entry : this.columns.entrySet()) {
         row.setValue(entry.getKey(), entry.getValue().defaultVal);
      }

      for (int i = 0; i < defaultValues.length; i += 2) {
         row.setValue((String)defaultValues[i], defaultValues[i + 1]);
      }

      this.resizeColumnWidth();
      return rowposition;
   }

   public int removeRow() {
      return this.removeRow(this.rowposition);
   }

   public int removeRow(int row) {
      return this.removeRow(row, null);
   }

   public int removeRow(IntConsumer callback) {
      return this.removeRow(this.rowposition, callback);
   }

   public int removeRow(int row, IntConsumer callback) {
      if (row < 0) {
         return -1;
      }

      if (this.m_6702_().size() == 0) {
         return -1;
      }

      if (this.rowposition == row) {
         this.editingColumn = null;
         this.editingWidget = null;
      }

      this.m_6702_().remove(row);
      double scrollAmount = this.m_93517_();
      this.m_93410_(Math.min(scrollAmount, this.m_93518_()));
      this.resizeColumnWidth();
      if (callback != null) {
         callback.accept(row);
      }

      int newRow = Math.min(row, this.m_6702_().size() - 1);
      if (newRow >= 0) {
         int oldRowpos = this.rowposition;
         this.setSelected(newRow);
         if (newRow == oldRowpos && this.onRowpositionChanged != null && this.rowposition > -1 && this.rowpositionChangeEnabled) {
            this.onRowpositionChanged.accept(this.rowposition, ((Grid.Row)this.m_6702_().get(this.rowposition)).values);
         }
      } else {
         this.setSelected(null);
      }

      return row;
   }

   public Grid setValueChangeEnabled(boolean enabled) {
      this.valueChangeEnabled = enabled;
      return this;
   }

   public Grid setRowpositionChangeEnabled(boolean enabled) {
      this.rowpositionChangeEnabled = enabled;
      return this;
   }

   public void setSelected(int rowposition) {
      this.setSelected((Grid.Row)this.m_6702_().get(rowposition));
   }

   public void setSelected(@Nullable Grid.Row row) {
      super.m_6987_(row);
      this.setRowposition(this.m_6702_().indexOf(row));
   }

   private void setRowposition(int position) {
      if (this.rowposition != position) {
         this.rowposition = position;
         if (this.onRowpositionChanged != null && this.rowposition > -1 && this.rowpositionChangeEnabled) {
            this.onRowpositionChanged.accept(this.rowposition, ((Grid.Row)this.m_6702_().get(this.rowposition)).values);
         }
      }
   }

   public int getRowposition() {
      return this.rowposition;
   }

   public <T> T getValue(int rowposition, String columnName) {
      return ((Grid.Row)this.m_6702_().get(rowposition)).getValue(columnName);
   }

   public <T> void setValue(int rowposition, String columnName, T value) {
      Grid.Row row = (Grid.Row)this.m_6702_().get(rowposition);
      row.setValue(columnName, value);
   }

   public void setGridFocus(int rowposition, String columnName) {
      if (this.owner.m_7222_() != this) {
         this.owner.m_7522_(this);
      }

      this.setSelected(rowposition);
      int startX = 0;
      this.editingColumn = null;

      for (Entry<String, Grid.Column<?, ?>> entry : this.columns.entrySet()) {
         if (entry.getKey() == columnName) {
            this.editingColumn = entry.getValue();
            break;
         }

         startX += entry.getValue().width;
      }

      if (this.editingColumn == null) {
         this.editingWidget = null;
      } else if (this.editingColumn.editable) {
         this.editingWidget = ((Grid.Column<Object, ?>)this.editingColumn)
            .createEditWidget(
               this.owner,
               this.owner.getMinecraft().f_91062_,
               this.f_93393_ + startX + 2,
               this.m_7610_(rowposition) + 2,
               this.f_93387_ - 3,
               rowposition,
               (Grid.Row)this.m_93511_(),
               columnName,
               ((Grid.Row)this.m_93511_()).getValue(columnName)
            );
         if (this.editingWidget != null) {
            this.editingWidget.m_93692_(true);
         }
      }
   }

   public void visitRows(Consumer<Map<String, Object>> task) {
      this.m_6702_().forEach(row -> task.accept(row.values));
   }

   public List<ResizableButton> getRowEditButtons() {
      return this.rowEditButtons;
   }

   private void relocateButtons() {
      int x = this.f_93392_ - 12;
      int y = this.f_93390_ - 12;

      for (Button rowEditButton : Lists.reverse(this.rowEditButtons)) {
         rowEditButton.m_264152_(x, y);
         x -= 12;
      }
   }

   @Override
   public void resize(ScreenRectangle screenRectangle) {
      if (this.getHorizontalSizingOption() != null) {
         this.getHorizontalSizingOption().resizeFunction.resize(this, screenRectangle, this.getX1(), this.getX2());
      }

      if (this.getVerticalSizingOption() != null) {
         this.getVerticalSizingOption().resizeFunction.resize(this, screenRectangle, this.getY1(), this.getY2());
      }

      this.relocateButtons();
      this.resizeColumnWidth();
   }

   protected void resizeColumnWidth() {
      int remainWidth = this.f_93388_ - (this.m_93518_() > 0 ? 7 : 1);
      int idx = 0;
      int size = this.columns.size();

      for (Grid.Column<?, ?> col : this.columns.values()) {
         col.width = (int)(col.initialWidth * ((float)this.f_93388_ / this.columnSizeSum));
         remainWidth -= col.width;
         if (++idx == size && remainWidth != 0) {
            col.width += remainWidth;
         }
      }

      if (this.editingWidget != null) {
         int width = 0;

         for (Grid.Column<?, ?> column : this.columns.values()) {
            if (column == this.editingColumn) {
               break;
            }

            width += column.width + 1;
         }

         this.editingWidget._setX(this._getX() + width + 1);
         this.editingWidget._setWidth(this.editingColumn.width - 3);
      }
   }

   public void m_93437_(int width, int height, int y0, int y1) {
      this.f_93388_ = width;
      this.f_93389_ = height;
      this.f_93390_ = y0;
      this.f_93391_ = y1;
      this.f_93393_ = 0;
      this.f_93392_ = width;
      this.relocateButtons();
   }

   public void m_93507_(int x) {
      this.f_93393_ = x;
      this.f_93392_ = x + this.f_93388_;
      this.relocateButtons();
   }

   public int m_5747_() {
      return this.f_93393_ + this.f_93388_ / 2 - this.m_5759_() / 2 + 2;
   }

   public int m_93520_() {
      return this.m_5747_() + this.m_5759_();
   }

   protected int m_7610_(int p_93512_) {
      return this.f_93390_ - (int)this.m_93517_() + p_93512_ * this.f_93387_ + this.f_93395_;
   }

   protected int m_93485_(int p_93486_) {
      return this.m_7610_(p_93486_) + this.f_93387_;
   }

   public void m_93692_(boolean focused) {
      if (!focused) {
         this.editingColumn = null;
         this.editingWidget = null;
      }
   }

   public boolean m_93696_() {
      return this.owner.m_7222_() == this;
   }

   public int m_93518_() {
      return Math.max(0, this.m_5775_() - (this.f_93391_ - this.f_93390_ - 1));
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.rowEditButtons.forEach(button -> button.m_88315_(guiGraphics, mouseX, mouseY, partialTicks));
      int color = this.m_93696_() ? -1 : (this.m_142518_() ? -6250336 : -12566463);
      GL11.glEnable(2960);
      RenderSystem.stencilOp(7680, 7680, 7681);
      RenderSystem.stencilFunc(519, 1, 255);
      RenderSystem.stencilMask(255);
      RenderSystem.clear(1024, true);
      guiGraphics.m_280509_(this.f_93393_, this.f_93390_, this.f_93392_, this.f_93391_, color);
      RenderSystem.stencilFunc(514, 1, 255);
      RenderSystem.stencilMask(0);
      this.m_239227_(guiGraphics, mouseX, mouseY, partialTicks);
      GL11.glDisable(2960);
      if (this.editingWidget != null) {
         int rowTop = this.m_7610_(this.rowposition);
         int rowBottom = this.m_7610_(this.rowposition) + this.f_93387_;
         if (rowBottom >= this.f_93390_ && rowTop <= this.f_93391_) {
            guiGraphics.m_280168_().m_85836_();
            guiGraphics.m_280168_().m_252880_(0.0F, 0.0F, 1.0F);
            this.editingWidget._setY(this.m_7610_(this.rowposition) + 2);
            this.editingWidget.asWidget().m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
            guiGraphics.m_280168_().m_85849_();
         }
      }
   }

   protected void m_239227_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      int rowLeft = this.m_5747_() - 1;
      int rowWidth = this.m_5759_();
      int itemHeight = this.f_93387_;
      int itemCount = this.m_5773_();
      int rowBottom = this.f_93390_;

      for (int rowIndex = 0; rowIndex < itemCount; rowIndex++) {
         int rowTop = this.m_7610_(rowIndex);
         rowBottom = this.m_7610_(rowIndex) + this.f_93387_;
         if (rowBottom >= this.f_93390_ && rowTop <= this.f_93391_) {
            this.m_238964_(guiGraphics, mouseX, mouseY, partialTicks, rowIndex, rowLeft, rowTop, rowWidth, itemHeight);
         }
      }

      if (rowBottom + 1 < this.f_93391_ - 1) {
         if (this.transparentBackground) {
            guiGraphics.m_280246_(0.12F, 0.12F, 0.12F, 1.0F);
            guiGraphics.m_280163_(
               Screen.f_279548_, rowLeft, rowBottom + 1, rowLeft + rowWidth - 2, this.f_93391_ - 1, rowWidth - 2, this.f_93391_ - rowBottom - 2, 32, 32
            );
            guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
         } else {
            guiGraphics.m_280509_(rowLeft, rowBottom + 1, rowLeft + rowWidth - 2, this.f_93391_ - 1, -16777216);
         }
      }

      guiGraphics.m_280168_().m_85836_();
      guiGraphics.m_280168_().m_252880_(0.0F, 0.0F, 1.0F);
      int i = this.m_5756_();
      int j = i + 6;
      int i2 = this.m_93518_();
      if (i2 > 0) {
         int j2 = (int)((float)((this.f_93391_ - this.f_93390_) * (this.f_93391_ - this.f_93390_)) / this.m_5775_());
         j2 = Mth.m_14045_(j2, 32, this.f_93391_ - this.f_93390_ - 8);
         int k1 = (int)this.m_93517_() * (this.f_93391_ - this.f_93390_ - j2) / i2 + this.f_93390_;
         if (k1 < this.f_93390_) {
            k1 = this.f_93390_;
         }

         guiGraphics.m_280509_(i, this.f_93390_, j, this.f_93391_, -16777216);
         guiGraphics.m_280509_(i, k1, j, k1 + j2, -8355712);
         guiGraphics.m_280509_(i, k1, j - 1, k1 + j2 - 1, -4144960);
      }

      guiGraphics.m_280168_().m_85849_();
   }

   protected void m_238964_(
      GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, int rowPosition, int rowLeft, int rowTop, int rowRight, int itemHeight
   ) {
      Grid.Row row = (Grid.Row)this.m_93500_(rowPosition);
      if (this.m_7987_(rowPosition)) {
         guiGraphics.m_280168_().m_85836_();
         guiGraphics.m_280168_().m_252880_(0.0F, 0.0F, 1.0F);
         this.m_240140_(guiGraphics, rowTop, rowRight, itemHeight, 0, 0);
         row.m_6311_(guiGraphics, rowPosition, rowTop, rowLeft, rowRight, itemHeight, mouseX, mouseY, false, partialTicks);
         guiGraphics.m_280168_().m_85849_();
      } else {
         row.m_6311_(guiGraphics, rowPosition, rowTop, rowLeft, rowRight, itemHeight, mouseX, mouseY, false, partialTicks);
      }
   }

   protected void m_240140_(GuiGraphics guiGraphics, int rowTop, int rowRight, int itemHeight, int color, int color2) {
      guiGraphics.m_280509_(this.f_93393_, rowTop, this.f_93392_, rowTop + itemHeight + 1, -1);
   }

   public boolean m_5953_(double x, double y) {
      double y0 = this.rowEditButtons.size() > 0 ? this.f_93390_ - 12 : this.f_93390_;
      return this.editingWidget != null && this.editingWidget.m_5953_(x, y) || y >= y0 && y <= this.f_93391_ && x >= this.f_93393_ && x <= this.f_93392_;
   }

   public boolean m_6375_(double x, double y, int button) {
      if (!this.m_142518_()) {
         return false;
      }

      for (Button editButton : this.rowEditButtons) {
         if (editButton.m_6375_(x, y, button)) {
            return true;
         }
      }

      if (this.editingWidget != null && this.editingWidget.m_6375_(x, y, button)) {
         return true;
      } else {
         return !this.m_5953_(x, y) ? false : super.m_6375_(x, y, button);
      }
   }

   public boolean m_6050_(double x, double y, double amount) {
      if (this.editingWidget != null && this.editingWidget.m_5953_(x, y) && this.editingWidget.m_6050_(x, y, amount)) {
         return true;
      } else if (this.m_93696_() && this.m_93518_() > 0) {
         this.m_93410_(this.m_93517_() - amount * this.f_93387_ / 2.0);
         return true;
      } else {
         return false;
      }
   }

   public boolean m_7933_(int keycode, int p_100876_, int p_100877_) {
      if (!this.m_142518_()) {
         return false;
      } else {
         return this.editingWidget != null ? this.editingWidget.m_7933_(keycode, p_100876_, p_100877_) : super.m_7933_(keycode, p_100876_, p_100877_);
      }
   }

   public boolean m_5534_(char c, int i) {
      if (!this.m_142518_()) {
         return false;
      } else {
         return this.editingWidget != null ? this.editingWidget.m_5534_(c, i) : super.m_5534_(c, i);
      }
   }

   @Override
   public void _tick() {
      if (this.editingWidget instanceof EditBox editBox) {
         editBox.m_94120_();
      }
   }

   public boolean m_142518_() {
      return this.active;
   }

   public int m_5759_() {
      return this.f_93388_;
   }

   protected int m_5756_() {
      return this.f_93392_ - 6;
   }

   public static Grid.GridBuilder builder(Screen owner) {
      return new Grid.GridBuilder(owner);
   }

   public static Grid.GridBuilder builder(Screen owner, Minecraft minecraft) {
      return new Grid.GridBuilder(owner, minecraft);
   }

   public static Grid.EditBoxColumnBuilder editbox(String string) {
      return new Grid.EditBoxColumnBuilder(string);
   }

   public static <T> Grid.ComboBoxColumnBuilder<T> combo(String string, Collection<T> selectionList) {
      return new Grid.ComboBoxColumnBuilder<>(string, selectionList);
   }

   public static <T> Grid.RegistryPopupColumnBuilder<T> registryPopup(String string, IForgeRegistry<T> registry) {
      return new Grid.RegistryPopupColumnBuilder<>(string, registry);
   }

   public static <T, P extends PopupBox<T>> Grid.PopupColumnBuilder<T, P> popup(String string, PopupBox.PopupBoxProvider<T, P> popupBoxProvider) {
      return new Grid.PopupColumnBuilder<>(string, popupBoxProvider);
   }

   public static <T, W extends AbstractWidget & DataBindingComponent> Grid.WildcardColumnBuilder<T, W> wildcard(String string) {
      return new Grid.WildcardColumnBuilder<>(string);
   }

   @Override
   public void setX1(int x1) {
      this.xParam1 = x1;
   }

   @Override
   public void setX2(int x2) {
      this.xParam2 = x2;
   }

   @Override
   public void setY1(int y1) {
      this.yParam1 = y1;
   }

   @Override
   public void setY2(int y2) {
      this.yParam2 = y2;
   }

   public Grid relocateX(ScreenRectangle screenrect, int x) {
      this.f_93393_ = x;
      this.f_93392_ = x + this.f_93388_;
      this.relocateButtons();
      return this;
   }

   public Grid relocateY(ScreenRectangle screenrect, int y) {
      this.f_93390_ = y;
      this.f_93391_ = y + this.f_93389_;
      this.relocateButtons();
      return this;
   }

   @Override
   public void _setX(int x) {
      this.f_93393_ = x;
      this.f_93392_ = this.f_93393_ + this.f_93388_;
      this.relocateButtons();
   }

   @Override
   public void _setY(int y) {
      this.f_93390_ = y;
      this.f_93391_ = this.f_93390_ + this.f_93389_;
      this.relocateButtons();
   }

   @Override
   public void _setWidth(int width) {
      this.f_93392_ = this.f_93393_ + width;
      this.f_93388_ = width;
      this.relocateButtons();
   }

   @Override
   public void _setHeight(int height) {
      this.f_93391_ = this.f_93390_ + height;
      this.f_93389_ = height;
      this.relocateButtons();
   }

   @Override
   public int _getX() {
      return this.f_93393_;
   }

   @Override
   public int _getY() {
      return this.f_93390_;
   }

   @Override
   public int getX1() {
      return this.xParam1;
   }

   @Override
   public int getX2() {
      return this.xParam2;
   }

   @Override
   public int getY1() {
      return this.yParam1;
   }

   @Override
   public int getY2() {
      return this.yParam2;
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
      this.active = active;
      this.rowEditButtons.forEach(button -> button._setActive(active));
      if (!this.active) {
         this.reset();
      }
   }

   @Override
   public void _setResponder(Consumer<Object> responder) {
   }

   @Override
   public Consumer<Object> _getResponder() {
      return null;
   }

   @Override
   public void _setValue(Object value) {
      this.reset();
      if (value instanceof Grid.PackImporter packImporter) {
         this.setValueChangeEnabled(false);

         for (int i = 0; i < packImporter.rows.size(); i++) {
            this.addRow();
            Map<String, Object> map = packImporter.rows.get(i);
            Grid.Row row = (Grid.Row)this.m_6702_().get(i);
            map.forEach(row::setValue);
         }

         this.setValueChangeEnabled(true);
      }
   }

   @Override
   public Object _getValue() {
      return null;
   }

   @Override
   public void _renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   @Override
   public void reset() {
      this.rowposition = -1;
      this.m_6702_().clear();
      this.m_93410_(0.0);
      this.setSelected(null);
      this.editingColumn = null;
      this.editingWidget = null;
   }

   @Override
   public Component _getMessage() {
      return Component.m_237113_(this.toString());
   }

   @Override
   public int _getWidth() {
      return this.getWidth();
   }

   @Override
   public int _getHeight() {
      return this.getHeight();
   }

   private abstract static class Column<T, W extends AbstractWidget> {
      final Function<T, String> toDisplayText;
      final Consumer<Grid.ValueChangeEvent<T>> onValueChanged;
      final Consumer<W> onEditWidgetCreate;
      final T defaultVal;
      final boolean editable;
      final int initialWidth;
      int width;

      private Column(
         Function<T, String> toDisplayText,
         Consumer<Grid.ValueChangeEvent<T>> onValueChanged,
         Consumer<W> onEditWidgetCreate,
         T defaultVal,
         boolean editable,
         int width
      ) {
         this.toDisplayText = toDisplayText;
         this.onValueChanged = onValueChanged;
         this.onEditWidgetCreate = onEditWidgetCreate;
         this.defaultVal = defaultVal;
         this.initialWidth = width;
         this.editable = editable;
      }

      public String toDisplayText(Object object) {
         return this.toDisplayText.apply((T)object);
      }

      public abstract ResizableComponent createEditWidget(Screen var1, Font var2, int var3, int var4, int var5, int var6, Grid.Row var7, String var8, T var9);
   }

   public abstract static class ColumnBuilder<T, C extends Grid.Column<T, W>, W extends AbstractWidget> {
      protected final String name;
      protected Function<T, String> toDisplayText = ParseUtil::nullParam;
      protected Consumer<Grid.ValueChangeEvent<T>> onValueChanged = null;
      protected Consumer<W> onEditWidgetCreated = null;
      protected T defaultValue = (T)null;
      protected boolean editable = true;
      protected int width = 100;

      protected ColumnBuilder(String name) {
         this.name = name;
      }

      public Grid.ColumnBuilder<T, C, W> toDisplayText(Function<T, String> toDisplayText) {
         this.toDisplayText = toDisplayText;
         return this;
      }

      public Grid.ColumnBuilder<T, C, W> editable(boolean editable) {
         this.editable = editable;
         return this;
      }

      public Grid.ColumnBuilder<T, C, W> valueChanged(Consumer<Grid.ValueChangeEvent<T>> onValueChanged) {
         this.onValueChanged = onValueChanged;
         return this;
      }

      public Grid.ColumnBuilder<T, C, W> defaultVal(T value) {
         this.defaultValue = value;
         return this;
      }

      public Grid.ColumnBuilder<T, C, W> editWidgetCreated(Consumer<W> onCreate) {
         this.onEditWidgetCreated = onCreate;
         return this;
      }

      public Grid.ColumnBuilder<T, C, W> width(int width) {
         this.width = width;
         return this;
      }

      protected abstract C create();
   }

   public static class ComboBoxColumnBuilder<T> extends Grid.ColumnBuilder<T, Grid.ComboColumn<T>, ComboBox<T>> {
      private final Collection<T> enums;

      protected ComboBoxColumnBuilder(String name, Collection<T> enums) {
         super(name);
         this.enums = enums;
         this.toDisplayText = ParseUtil::snakeToSpacedCamel;
      }

      protected Grid.ComboColumn<T> create() {
         return new Grid.ComboColumn<>(
            this.toDisplayText, this.onValueChanged, this.onEditWidgetCreated, this.defaultValue, this.enums, this.editable, this.width
         );
      }
   }

   private static class ComboColumn<T> extends Grid.Column<T, ComboBox<T>> {
      final Collection<T> comboItemCollection;

      private ComboColumn(
         Function<T, String> toDisplayText,
         Consumer<Grid.ValueChangeEvent<T>> onValueChanged,
         Consumer<ComboBox<T>> onEditWidgetCreate,
         T defaultVal,
         Collection<T> enums,
         boolean editable,
         int size
      ) {
         super(toDisplayText, onValueChanged, onEditWidgetCreate, defaultVal, editable, size);
         this.comboItemCollection = enums;
      }

      @Override
      public ResizableComponent createEditWidget(Screen owner, Font font, int x, int y, int height, int rowposition, Grid.Row row, String colName, T value) {
         ComboBox<T> comboBox = new ComboBox<>(
            owner,
            font,
            x,
            this.width - 3,
            y,
            height,
            null,
            null,
            Math.min(this.comboItemCollection.size(), 8),
            Component.m_237113_("grid.comboEdit"),
            this.comboItemCollection,
            this.toDisplayText,
            item -> row.setValue(colName, item)
         );
         comboBox._setValue(value);
         if (this.onEditWidgetCreate != null) {
            this.onEditWidgetCreate.accept(comboBox);
         }

         return comboBox;
      }
   }

   private static class EditBoxColumn extends Grid.Column<String, EditBox> {
      private EditBoxColumn(
         Function<String, String> toDisplayText,
         Consumer<Grid.ValueChangeEvent<String>> onValueChanged,
         Consumer<EditBox> onEditWidgetCreate,
         String defaultVal,
         boolean editable,
         int size
      ) {
         super(toDisplayText, onValueChanged, onEditWidgetCreate, defaultVal, editable, size);
      }

      public ResizableComponent createEditWidget(Screen owner, Font font, int x, int y, int height, int rowposition, Grid.Row row, String colName, String value) {
         ResizableEditBox editbox = new ResizableEditBox(font, x, this.width - 3, y, height, Component.m_237113_("grid.editbox"), null, null);
         editbox.m_94199_(100);
         editbox.m_94144_(value);
         editbox.m_94151_(string -> row.setValue(colName, string));
         if (this.onEditWidgetCreate != null) {
            this.onEditWidgetCreate.accept(editbox);
         }

         return editbox;
      }
   }

   public static class EditBoxColumnBuilder extends Grid.ColumnBuilder<String, Grid.EditBoxColumn, EditBox> {
      protected EditBoxColumnBuilder(String name) {
         super(name);
      }

      protected Grid.EditBoxColumn create() {
         return new Grid.EditBoxColumn(this.toDisplayText, this.onValueChanged, this.onEditWidgetCreated, this.defaultValue, this.editable, this.width);
      }
   }

   public static class GridBuilder {
      private final Minecraft minecraft;
      private final Screen owner;
      private final Map<String, Grid.Column<?, ?>> columns = Maps.newLinkedHashMap();
      private int x1;
      private int y1;
      private int x2;
      private int y2;
      private int rowHeight;
      private int columnSizeTotal;
      private boolean transparentBackground;
      private BiConsumer<Grid, Button> onAddPress;
      private BiConsumer<Grid, Button> onRemovePress;
      private BiConsumer<Integer, Map<String, Object>> onRowpositionChanged;
      private ResizableComponent.HorizontalSizing horizontalSizing = null;
      private ResizableComponent.VerticalSizing verticalSizing = null;
      private Grid.GridBuilder.RowEditButton rowEditButtons = Grid.GridBuilder.RowEditButton.NONE;

      private GridBuilder(Screen owner) {
         this(owner, owner.getMinecraft());
      }

      private GridBuilder(Screen owner, Minecraft minecraft) {
         this.owner = owner;
         this.minecraft = minecraft;
      }

      public <T, C extends Grid.Column<T, W>, W extends AbstractWidget> Grid.GridBuilder addColumn(Grid.ColumnBuilder<T, C, W> builder) {
         this.columns.put(builder.name, builder.create());
         this.columnSizeTotal = this.columnSizeTotal + builder.width;
         return this;
      }

      public Grid.GridBuilder xy1(int x1, int y1) {
         this.x1 = x1;
         this.y1 = y1;
         return this;
      }

      public Grid.GridBuilder xy2(int x2, int y2) {
         this.x2 = x2;
         this.y2 = y2;
         return this;
      }

      public Grid.GridBuilder rowHeight(int rowHeight) {
         this.rowHeight = rowHeight;
         return this;
      }

      public Grid.GridBuilder transparentBackground(boolean transparentBackground) {
         this.transparentBackground = transparentBackground;
         return this;
      }

      public Grid.GridBuilder rowEditable(Grid.GridBuilder.RowEditButton rowEditButtons) {
         this.rowEditButtons = rowEditButtons;
         return this;
      }

      public Grid.GridBuilder pressAdd(BiConsumer<Grid, Button> onAddPress) {
         this.onAddPress = onAddPress;
         return this;
      }

      public Grid.GridBuilder pressRemove(BiConsumer<Grid, Button> OnRemovePress) {
         this.onRemovePress = OnRemovePress;
         return this;
      }

      public Grid.GridBuilder rowpositionChanged(BiConsumer<Integer, Map<String, Object>> onRowpositionChanged) {
         this.onRowpositionChanged = onRowpositionChanged;
         return this;
      }

      public Grid.GridBuilder horizontalSizing(ResizableComponent.HorizontalSizing horizontalSizing) {
         this.horizontalSizing = horizontalSizing;
         return this;
      }

      public Grid.GridBuilder verticalSizing(ResizableComponent.VerticalSizing verticalSizing) {
         this.verticalSizing = verticalSizing;
         return this;
      }

      public Grid build() {
         return new Grid(this);
      }

      public enum RowEditButton {
         ADD(true, false),
         REMOVE(false, true),
         ADD_REMOVE(true, true),
         NONE(false, false);

         public boolean add;
         public boolean remove;

         RowEditButton(boolean add, boolean remove) {
            this.add = add;
            this.remove = remove;
         }
      }
   }

   public static class PackImporter {
      List<Map<String, Object>> rows = Lists.newArrayList();

      public Grid.PackImporter newRow() {
         this.rows.add(Maps.newHashMap());
         return this;
      }

      public Grid.PackImporter newValue(String column, Object value) {
         this.rows.get(this.rows.size() - 1).put(column, value);
         return this;
      }
   }

   private static class PopupColumn<T, P extends PopupBox<T>> extends Grid.Column<T, P> {
      final PopupBox.PopupBoxProvider<T, P> popupBoxProvider;
      final Predicate<T> filter;

      private PopupColumn(
         Function<T, String> toDisplayText,
         Consumer<Grid.ValueChangeEvent<T>> onValueChanged,
         Consumer<P> onEditWidgetCreate,
         T defaultVal,
         PopupBox.PopupBoxProvider<T, P> popupBoxProvider,
         Predicate<T> filter,
         Consumer<P> onCreate,
         boolean editable,
         int size
      ) {
         super(toDisplayText, onValueChanged, onEditWidgetCreate, defaultVal, editable, size);
         this.popupBoxProvider = popupBoxProvider;
         this.filter = filter;
      }

      @Override
      public ResizableComponent createEditWidget(Screen owner, Font font, int x, int y, int height, int rowposition, Grid.Row row, String colName, T value) {
         P popup = this.popupBoxProvider
            .create(
               owner, font, x, this.width - 3, y, height, null, null, Component.m_237113_("grid.popupEdit"), pair -> row.setValue(colName, (T)pair.getSecond())
            );
         popup.applyFilter(this.filter);
         popup._setValue(value);
         if (this.onEditWidgetCreate != null) {
            this.onEditWidgetCreate.accept(popup);
         }

         return popup;
      }
   }

   public static class PopupColumnBuilder<T, P extends PopupBox<T>> extends Grid.ColumnBuilder<T, Grid.PopupColumn<T, P>, P> {
      final PopupBox.PopupBoxProvider<T, P> popupProvider;
      Predicate<T> filter = item -> true;

      protected PopupColumnBuilder(String name, PopupBox.PopupBoxProvider<T, P> popupProvider) {
         super(name);
         this.popupProvider = popupProvider;
      }

      public Grid.PopupColumnBuilder<T, P> filter(Predicate<T> filter) {
         this.filter = filter;
         return this;
      }

      protected Grid.PopupColumn<T, P> create() {
         return new Grid.PopupColumn<>(
            this.toDisplayText,
            this.onValueChanged,
            this.onEditWidgetCreated,
            this.defaultValue,
            this.popupProvider,
            this.filter,
            this.onEditWidgetCreated,
            this.editable,
            this.width
         );
      }
   }

   private static class RegistryPopupColumn<T> extends Grid.Column<T, PopupBox.RegistryPopupBox<T>> {
      final IForgeRegistry<T> registry;
      final Predicate<T> filter;

      private RegistryPopupColumn(
         Function<T, String> toDisplayText,
         Consumer<Grid.ValueChangeEvent<T>> onValueChanged,
         Consumer<PopupBox.RegistryPopupBox<T>> onEditWidgetCreate,
         T defaultVal,
         IForgeRegistry<T> registry,
         Predicate<T> filter,
         boolean editable,
         int size
      ) {
         super(toDisplayText, onValueChanged, onEditWidgetCreate, defaultVal, editable, size);
         this.registry = registry;
         this.filter = filter;
      }

      @Override
      public ResizableComponent createEditWidget(Screen owner, Font font, int x, int y, int height, int rowposition, Grid.Row row, String colName, T value) {
         PopupBox.RegistryPopupBox<T> popup = new PopupBox.RegistryPopupBox<>(
            owner,
            font,
            x,
            this.width - 3,
            y,
            height,
            null,
            null,
            Component.m_237113_("grid.popupEdit"),
            this.registry,
            pair -> row.setValue(colName, (T)pair.getSecond())
         );
         popup.applyFilter(this.filter);
         popup._setValue(value);
         if (this.onEditWidgetCreate != null) {
            this.onEditWidgetCreate.accept(popup);
         }

         return popup;
      }
   }

   public static class RegistryPopupColumnBuilder<T> extends Grid.ColumnBuilder<T, Grid.RegistryPopupColumn<T>, PopupBox.RegistryPopupBox<T>> {
      final IForgeRegistry<T> registry;
      Predicate<T> filter = item -> true;

      protected RegistryPopupColumnBuilder(String name, IForgeRegistry<T> registry) {
         super(name);
         this.registry = registry;
      }

      public Grid.RegistryPopupColumnBuilder<T> filter(Predicate<T> filter) {
         this.filter = filter;
         return this;
      }

      protected Grid.RegistryPopupColumn<T> create() {
         return new Grid.RegistryPopupColumn<>(
            this.toDisplayText, this.onValueChanged, this.onEditWidgetCreated, this.defaultValue, this.registry, this.filter, this.editable, this.width
         );
      }
   }

   public class Row extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<Grid.Row> {
      private Map<String, Object> values = Maps.newLinkedHashMap();

      private Row() {
         for (String columnName : Grid.this.columns.keySet()) {
            this.values.put(columnName, null);
         }
      }

      public <T> T getValue(String columnName) {
         return (T)this.values.get(columnName);
      }

      public <T> void setValue(String columnName, T value) {
         if (!Grid.this.columns.containsKey(columnName)) {
            throw new IllegalArgumentException("There's no column named " + columnName + " in Grid");
         }

         T oldVal = (T)this.values.get(columnName);
         this.values.put(columnName, value);
         Grid.Column<T, ?> column = (Grid.Column<T, ?>)Grid.this.columns.get(columnName);
         if (column.onValueChanged != null && Grid.this.valueChangeEnabled && !ParseUtil.compareNullables(oldVal, value)) {
            int idx = Grid.this.m_6702_().indexOf(this);
            if (idx >= 0) {
               column.onValueChanged.accept(new Grid.ValueChangeEvent<>(Grid.this, Grid.this.m_6702_().indexOf(this), oldVal, value));
            }
         }
      }

      public Component m_142172_() {
         return Component.m_237115_("narrator.select");
      }

      private boolean rowHighlight() {
         return Grid.this.m_93696_() && Grid.this.m_93511_() == this;
      }

      public void m_6311_(
         GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks
      ) {
         int startX = Grid.this.f_93393_;
         int start = this.rowHighlight() ? 2 : 1;
         int size = this.values.size();
         int idx = 0;

         for (Entry<String, Object> entry : this.values.entrySet()) {
            Grid.Column<?, ?> column = Grid.this.columns.get(entry.getKey());
            boolean first = idx == 0;
            boolean last = idx == size - 1;
            if (Grid.this.transparentBackground) {
               int end = this.rowHighlight() ? 3 : 1;
               guiGraphics.m_280246_(0.12F, 0.12F, 0.12F, 1.0F);
               guiGraphics.m_280163_(
                  Screen.f_279548_, startX + start, top + start, startX + column.width, top + height, column.width - end, height - end, 32, 32
               );
               guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
               int end = this.rowHighlight() ? 1 : 0;
               guiGraphics.m_280509_(startX + (first ? start : 1), top + start, startX + column.width - (last ? end : 0), top + height - end, -16777216);
            }

            String displayText = column.toDisplayText(entry.getValue());
            String correctedString = Grid.this.f_93386_.f_91062_.m_92834_(displayText, column.width - 1);
            guiGraphics.m_280056_(Grid.this.f_93386_.f_91062_, correctedString, startX + 3, top + Grid.this.f_93387_ / 2 - 4, 16777215, false);
            startX += column.width;
            idx++;
         }
      }

      public boolean m_6375_(double mouseX, double mouseY, int button) {
         if (button != 0) {
            return false;
         }

         int rowposition = Grid.this.m_6702_().indexOf(this);
         if (Grid.this.m_93511_() == this) {
            if (Grid.this.editingColumn != null && Grid.this.editingColumn == this.getColumn(mouseX)) {
               Grid.this.setGridFocus(rowposition, null);
            } else {
               Grid.this.setGridFocus(rowposition, this.getColumnName(mouseX));
            }
         } else {
            Grid.this.editingWidget = null;
            Grid.this.setGridFocus(rowposition, this.getColumnName(mouseX));
         }

         Grid.this.setSelected(this);
         return true;
      }

      public Grid.Column<?, ?> getColumn(double mouseX) {
         double x = Grid.this.f_93393_;

         for (Grid.Column<?, ?> entry : Grid.this.columns.values()) {
            x += entry.width;
            if (mouseX < x) {
               return entry;
            }
         }

         return null;
      }

      public String getColumnName(double mouseX) {
         double x = Grid.this.f_93393_;

         for (Entry<String, Grid.Column<?, ?>> entry : Grid.this.columns.entrySet()) {
            x += entry.getValue().width;
            if (mouseX < x) {
               return entry.getKey();
            }
         }

         return null;
      }
   }

   public static class ValueChangeEvent<T> {
      public final Grid grid;
      public final int rowposition;
      public final T prevValue;
      public final T postValue;

      private ValueChangeEvent(Grid grid, int rowposition, T prevValue, T postValue) {
         this.grid = grid;
         this.rowposition = rowposition;
         this.prevValue = prevValue;
         this.postValue = postValue;
      }
   }

   private static class WildcardColumn<T, W extends AbstractWidget & DataBindingComponent> extends Grid.Column<T, W> {
      Function<Grid.Row, AbstractWidget> editWidgetProvider;

      private WildcardColumn(
         Function<T, String> toDisplayText,
         Consumer<Grid.ValueChangeEvent<T>> onValueChanged,
         Consumer<W> onEditWidgetCreate,
         T defaultVal,
         Function<Grid.Row, AbstractWidget> editWidgetProvider,
         boolean editable,
         int size
      ) {
         super(toDisplayText, onValueChanged, onEditWidgetCreate, defaultVal, editable, size);
         this.editWidgetProvider = editWidgetProvider;
      }

      public W createEditWidget(Screen owner, Font font, int x, int y, int height, int rowposition, Grid.Row row, String colName, T value) {
         W editWidget = (W)this.editWidgetProvider.apply(row);
         if (editWidget == null) {
            return null;
         }

         editWidget.m_252865_(x);
         editWidget.m_253211_(y);
         editWidget.m_93674_(this.width - 3);
         editWidget.setHeight(height);
         editWidget._setValue(value);
         editWidget._setResponder(val -> row.setValue(colName, (T)val));
         if (editWidget instanceof PopupBox<?> popupBox) {
            popupBox._setResponder(pair -> row.setValue(colName, (T)((String)pair.getFirst())));
         }

         if (this.onEditWidgetCreate != null) {
            this.onEditWidgetCreate.accept(editWidget);
         }

         return editWidget;
      }
   }

   public static class WildcardColumnBuilder<T, W extends AbstractWidget & DataBindingComponent> extends Grid.ColumnBuilder<T, Grid.WildcardColumn<T, W>, W> {
      Function<Grid.Row, AbstractWidget> editWidgetProvider;

      protected WildcardColumnBuilder(String name) {
         super(name);
      }

      public Grid.WildcardColumnBuilder<T, W> editWidgetProvider(Function<Grid.Row, AbstractWidget> editWidgetProvider) {
         this.editWidgetProvider = editWidgetProvider;
         return this;
      }

      protected Grid.WildcardColumn<T, W> create() {
         return new Grid.WildcardColumn<>(
            this.toDisplayText, this.onValueChanged, this.onEditWidgetCreated, this.defaultValue, this.editWidgetProvider, this.editable, this.width
         );
      }
   }
}
