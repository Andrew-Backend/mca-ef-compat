package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;

public class OffhandValidatorScreen extends Screen {
   private final Screen parentScreen;
   private final List<CompoundTag> conditionList = Lists.newLinkedList();
   private final CompoundTag rootTag;
   private Grid conditionGrid;
   private Grid parameterGrid;

   public OffhandValidatorScreen(Screen parentScreen, CompoundTag rootTag) {
      super(Component.m_237115_("datapack_edit.weapon_type.offhand_visibility"));
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.rootTag = rootTag;
      this.parentScreen = parentScreen;
      this.conditionGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(15, 45)
         .xy2(100, 45)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> {
            this.parameterGrid.reset();
            Supplier<Condition<?>> conditionProvider = (Supplier<Condition<?>>)values.get("condition");
            if (conditionProvider != null) {
               Condition<?> condition = conditionProvider.get();
               CompoundTag comp = this.conditionList.get(rowposition);
               Grid.PackImporter parameters = new Grid.PackImporter();

               for (Condition.ParameterEditor editor : condition.getAcceptingParameters(this)) {
                  parameters.newRow();
                  parameters.newValue("parameter_key", editor);
                  parameters.newValue("parameter_value", editor.fromTag.apply(comp.m_128423_(editor.editWidget.m_6035_().getString())));
               }

               this.parameterGrid._setValue(parameters);
            }
         })
         .addColumn(
            Grid.<Supplier>registryPopup("condition", EpicFightConditions.REGISTRY.get())
               .filter(condition -> condition.get() instanceof Condition.EntityPatchCondition)
               .editable(true)
               .toDisplayText(condition -> ParseUtil.getRegistryName(condition, EpicFightConditions.REGISTRY.get()))
               .valueChanged(event -> {
                  CompoundTag comp = this.conditionList.get(event.rowposition);
                  comp.m_128359_("predicate", ParseUtil.getRegistryName(event.postValue, EpicFightConditions.REGISTRY.get()));
                  this.parameterGrid.reset();
                  if (event.postValue != null) {
                     Condition<?> condition = (Condition<?>)((Supplier)event.postValue).get();
                     Grid.PackImporter parameters = new Grid.PackImporter();

                     for (Condition.ParameterEditor editor : condition.getAcceptingParameters(this)) {
                        parameters.newRow();
                        parameters.newValue("parameter_key", editor);
                        parameters.newValue("parameter_value", editor.fromTag.apply(comp.m_128423_(editor.editWidget.m_6035_().getString())));
                     }

                     this.parameterGrid._setValue(parameters);
                  }
               })
               .width(180)
         )
         .pressAdd((grid, button) -> {
            grid.setValueChangeEnabled(false);
            int rowposition = grid.addRow();
            this.conditionList.add(rowposition, new CompoundTag());
            grid.setGridFocus(rowposition, "weapon_category");
            grid.setValueChangeEnabled(true);
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> this.conditionList.remove(removedRow));
            if (grid.m_6702_().size() == 0) {
               this.parameterGrid.reset();
            }
         })
         .build();
      this.parameterGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(125, 45)
         .xy2(12, 45)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.NONE)
         .transparentBackground(false)
         .addColumn(Grid.wildcard("parameter_key").editable(false).toDisplayText(widget -> widget.editWidget.m_6035_().getString()).width(100))
         .addColumn(Grid.wildcard("parameter_value").editWidgetProvider(row -> {
            Condition.ParameterEditor editor = row.getValue("parameter_key");
            return editor.editWidget;
         }).toDisplayText(ParseUtil::snakeToSpacedCamel).editable(true).valueChanged(event -> {
            CompoundTag predicate = this.conditionList.get(this.conditionGrid.getRowposition());
            Condition.ParameterEditor editor = event.grid.getValue(event.rowposition, "parameter_key");
            if (StringUtil.isNullOrEmpty(ParseUtil.nullParam(event.postValue))) {
               predicate.m_128473_(editor.editWidget.m_6035_().getString());
            } else {
               predicate.m_128365_(editor.editWidget.m_6035_().getString(), editor.toTag.apply(event.postValue));
            }
         }).width(150))
         .build();
      if (rootTag.m_128425_("offhand_item_compatible_predicate", 9)) {
         ListTag conditionList = rootTag.m_128437_("offhand_item_compatible_predicate", 10);
         Grid.PackImporter packImporter = new Grid.PackImporter();

         for (Tag tag : conditionList) {
            CompoundTag compTag = (CompoundTag)tag.m_6426_();
            this.conditionList.add(compTag);
            packImporter.newRow();
            packImporter.newValue("condition", EpicFightConditions.getConditionOrNull(ResourceLocation.parse(compTag.m_128461_("predicate"))));
         }

         this.conditionGrid._setValue(packImporter);
      }
   }

   protected void m_7856_() {
      ScreenRectangle screenRectangle = this.m_264198_();
      this.conditionGrid.resize(screenRectangle);
      this.parameterGrid.resize(screenRectangle);
      this.m_142416_(this.conditionGrid);
      this.m_142416_(this.parameterGrid);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_286989_,
               button -> {
                  ListTag newListTag = new ListTag();
                  int idx = 0;

                  for (CompoundTag tag : this.conditionList) {
                     try {
                        this.validateTagSave(tag);
                        newListTag.add(tag);
                        idx++;
                     } catch (Exception e) {
                        this.f_96541_
                           .m_91152_(
                              new MessageScreen(
                                    "Save Failed", "Failed to save row " + idx + ": " + e.getMessage(), this, button2 -> this.f_96541_.m_91152_(this), 180, 90
                                 )
                                 .autoCalculateHeight()
                           );
                        return;
                     }
                  }

                  this.rootTag.m_128365_("offhand_item_compatible_predicate", newListTag);
                  this.m_7379_();
               }
            )
            .m_252794_(this.f_96543_ / 2 - 162, this.f_96544_ - 32)
            .m_253046_(160, 21)
            .m_253136_()
      );
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_130656_,
               button -> this.f_96541_
                  .m_91152_(
                     new MessageScreen(
                        "", "Do you want to quit without saving changes?", this, button2 -> this.m_7379_(), button2 -> this.f_96541_.m_91152_(this), 180, 70
                     )
                  )
            )
            .m_252794_(this.f_96543_ / 2 + 2, this.f_96544_ - 32)
            .m_253046_(160, 21)
            .m_253136_()
      );
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      int yBegin = 32;
      int yEnd = this.f_96544_ - 45;
      guiGraphics.m_280430_(this.f_96547_, this.f_96539_, 20, 16, 16777215);
      guiGraphics.m_280246_(0.125F, 0.125F, 0.125F, 1.0F);
      guiGraphics.m_280163_(Screen.f_279548_, 0, yBegin, this.f_96543_, (float)yEnd - yBegin, this.f_96543_, yEnd, 32, 32);
      guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.m_280246_(0.25F, 0.25F, 0.25F, 1.0F);
      guiGraphics.m_280163_(Screen.f_279548_, 0, 0, 0.0F, 0.0F, this.f_96543_, yBegin, 32, 32);
      guiGraphics.m_280163_(Screen.f_279548_, 0, yEnd, 0.0F, (float)yEnd - yBegin, this.f_96543_, yEnd, 32, 32);
      guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.m_285978_(RenderType.m_286086_(), 0, yBegin, this.f_96543_, yBegin + 4, -16777216, 0, 0);
      guiGraphics.m_285978_(RenderType.m_286086_(), 0, yEnd, this.f_96543_, yEnd + 1, 0, -16777216, 0);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
   }

   private void validateTagSave(CompoundTag tag) throws IllegalStateException {
      try {
         Supplier<Condition<?>> condition = EpicFightConditions.getConditionOrThrow(ResourceLocation.parse(tag.m_128461_("predicate")));
         condition.get().read(tag);
      } catch (Exception e) {
         throw new IllegalStateException(e.getMessage());
      }
   }
}
