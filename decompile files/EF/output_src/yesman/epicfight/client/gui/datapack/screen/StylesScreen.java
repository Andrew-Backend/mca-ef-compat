package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;

public class StylesScreen extends Screen {
   private final Screen parentScreen;
   private final Grid stylesGrid;
   private Grid conditionGrid;
   private Grid parameterGrid;
   private final ComboBox<Style> defaultStyle;
   private final List<CompoundTag> cases = Lists.newArrayList();
   private final CompoundTag rootTag;

   public StylesScreen(Screen parentScreen, CompoundTag rootTag) {
      super(Component.m_237115_("datapack_edit.weapon_type.styles"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.rootTag = rootTag;
      this.stylesGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(12, 60)
         .xy2(160, 76)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(26)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> {
            CompoundTag caseCompound = this.cases.get(rowposition);
            Grid.PackImporter parameters = new Grid.PackImporter();

            for (Tag tag : caseCompound.m_128437_("conditions", 10)) {
               CompoundTag conditionCompound = (CompoundTag)tag;
               parameters.newRow();
               parameters.newValue("condition", EpicFightConditions.getConditionOrNull(ResourceLocation.parse(conditionCompound.m_128461_("predicate"))));
            }

            this.conditionGrid._setValue(parameters);
            if (this.conditionGrid.m_6702_().size() > 0) {
               this.conditionGrid.setGridFocus(0, "condition");
            }

            this.conditionGrid._setActive(true);
            this.parameterGrid._setActive(true);
         })
         .addColumn(
            Grid.combo("style", ParseUtil.remove(Style.ENUM_MANAGER.universalValues(), CapabilityItem.Styles.COMMON))
               .valueChanged(
                  event -> this.cases
                     .get(event.rowposition)
                     .m_128365_("style", StringTag.m_129297_(ParseUtil.nullParam(event.postValue).toLowerCase(Locale.ROOT)))
               )
               .defaultVal(CapabilityItem.Styles.ONE_HAND)
         )
         .pressAdd((grid, button) -> {
            int rowposition = grid.addRow(addedRow -> this.cases.add(addedRow, new CompoundTag()));
            grid.setGridFocus(rowposition, "style");
            this.conditionGrid._setActive(true);
            this.parameterGrid._setActive(true);
         })
         .pressRemove((grid, button) -> grid.removeRow(removedRow -> {
            this.cases.remove(removedRow);
            if (this.cases.size() == 0) {
               this.conditionGrid._setActive(false);
               this.parameterGrid._setActive(false);
            }
         }))
         .build();
      this.defaultStyle = new ComboBox<>(
         parentScreen,
         this.f_96547_,
         55,
         116,
         15,
         53,
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         ResizableComponent.VerticalSizing.HEIGHT_BOTTOM,
         8,
         Component.m_237115_("datapack_edit.weapon_type.styles.default"),
         new ArrayList<>(ParseUtil.remove(Style.ENUM_MANAGER.universalValues(), CapabilityItem.Styles.COMMON)),
         ParseUtil::snakeToSpacedCamel,
         null
      );
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.conditionGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(187, 60)
         .xy2(15, 50)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_HEIGHT)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> {
            this.parameterGrid.reset();
            Supplier<Condition<?>> conditionProvider = (Supplier<Condition<?>>)values.get("condition");
            if (conditionProvider != null) {
               Condition<?> condition = conditionProvider.get();
               ListTag conditionList = ParseUtil.getOrDefaultTag(this.cases.get(this.stylesGrid.getRowposition()), "conditions", new ListTag());
               CompoundTag conditionCompound = conditionList.m_128728_(rowposition);
               Grid.PackImporter parameters = new Grid.PackImporter();

               for (Condition.ParameterEditor editor : condition.getAcceptingParameters(this)) {
                  parameters.newRow();
                  parameters.newValue("parameter_key", editor);
                  parameters.newValue("parameter_value", editor.fromTag.apply(conditionCompound.m_128423_(editor.editWidget.m_6035_().getString())));
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
                  ListTag conditionList = ParseUtil.getOrDefaultTag(this.cases.get(this.stylesGrid.getRowposition()), "conditions", new ListTag());
                  CompoundTag conditionCompound = conditionList.m_128728_(event.rowposition);
                  conditionCompound.m_128359_("predicate", ParseUtil.getRegistryName(event.postValue, EpicFightConditions.REGISTRY.get()));
                  this.parameterGrid.reset();
                  if (event.postValue != null) {
                     Condition<?> condition = (Condition<?>)((Supplier)event.postValue).get();
                     Grid.PackImporter parameters = new Grid.PackImporter();

                     for (Condition.ParameterEditor editor : condition.getAcceptingParameters(this)) {
                        parameters.newRow();
                        parameters.newValue("parameter_key", editor);
                        parameters.newValue("parameter_value", editor.fromTag.apply(conditionCompound.m_128423_(editor.editWidget.m_6035_().getString())));
                     }

                     this.parameterGrid._setValue(parameters);
                  }
               })
               .width(180)
         )
         .pressAdd((grid, button) -> {
            grid.setValueChangeEnabled(false);
            int rowposition = grid.addRow();
            ListTag conditionList = ParseUtil.getOrDefaultTag(this.cases.get(this.stylesGrid.getRowposition()), "conditions", new ListTag());
            conditionList.add(rowposition, new CompoundTag());
            grid.setGridFocus(rowposition, "weapon_category");
            grid.setValueChangeEnabled(true);
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> {
               ListTag conditionList = ParseUtil.getOrDefaultTag(this.cases.get(this.stylesGrid.getRowposition()), "conditions", new ListTag());
               conditionList.remove(removedRow);
            });
            if (grid.m_6702_().size() == 0) {
               this.parameterGrid.reset();
            }
         })
         .build();
      this.parameterGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(187, 135)
         .xy2(15, 52)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.NONE)
         .transparentBackground(false)
         .addColumn(Grid.wildcard("parameter_key").editable(false).toDisplayText(editor -> editor.editWidget.m_6035_().getString()).width(100))
         .addColumn(Grid.wildcard("parameter_value").editWidgetProvider(row -> {
            Condition.ParameterEditor editor = row.getValue("parameter_key");
            return editor.editWidget;
         }).toDisplayText(ParseUtil::snakeToSpacedCamel).editable(true).valueChanged(event -> {
            ListTag conditionList = ParseUtil.getOrDefaultTag(this.cases.get(this.stylesGrid.getRowposition()), "conditions", new ListTag());
            CompoundTag conditionCompound = conditionList.m_128728_(this.conditionGrid.getRowposition());
            Condition.ParameterEditor editor = event.grid.getValue(event.rowposition, "parameter_key");
            conditionCompound.m_128365_(editor.editWidget.m_6035_().getString(), editor.toTag.apply(event.postValue));
         }).width(150))
         .build();
      this.conditionGrid._setActive(false);
      this.parameterGrid._setActive(false);
      if (rootTag.m_128441_("styles")) {
         CompoundTag stylesCompound = rootTag.m_128469_("styles");
         Grid.PackImporter packImporter = new Grid.PackImporter();

         for (Tag caseTag : stylesCompound.m_128437_("cases", 10)) {
            CompoundTag caseComp = (CompoundTag)caseTag;
            CompoundTag caseComp$2 = new CompoundTag();
            if (caseComp.m_128441_("style")) {
               caseComp$2.m_128365_("style", caseComp.m_128423_("style"));
            }

            if (caseComp.m_128441_("conditions")) {
               caseComp$2.m_128365_("conditions", caseComp.m_128423_("conditions"));
            }

            packImporter.newRow();
            packImporter.newValue("style", Style.ENUM_MANAGER.get(caseComp$2.m_128461_("style")));
            this.cases.add(caseComp$2);
         }

         this.stylesGrid._setValue(packImporter);
         this.defaultStyle._setValue(Style.ENUM_MANAGER.get(stylesCompound.m_128461_("default")));
      }
   }

   protected void m_7856_() {
      ScreenRectangle screenRectangle = this.m_264198_();
      this.stylesGrid.resize(screenRectangle);
      this.defaultStyle.resize(screenRectangle);
      this.conditionGrid.resize(screenRectangle);
      this.parameterGrid.resize(screenRectangle);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_130655_,
               button -> {
                  ListTag caseListTag = new ListTag();
                  String currentStyle = "";
                  if (this.defaultStyle._getValue() == null) {
                     this.f_96541_
                        .m_91152_(
                           new MessageScreen("Save Failed", "Define a default style", this, button2 -> this.f_96541_.m_91152_(this), 180, 90)
                              .autoCalculateHeight()
                        );
                  } else {
                     Set<String> styleNames = Sets.newHashSet();

                     try {
                        for (CompoundTag tag : this.cases) {
                           currentStyle = tag.m_128461_("style");
                           this.validateTagSave(tag);
                           if (styleNames.contains(tag.m_128461_("style"))) {
                              throw new IllegalStateException("Duplicated style " + tag.m_128461_("style"));
                           }

                           caseListTag.add(tag);
                           styleNames.add(tag.m_128461_("style"));
                        }

                        if (this.defaultStyle._getValue() == null) {
                           throw new IllegalStateException("Define a default style");
                        }
                     } catch (Exception e) {
                        this.f_96541_
                           .m_91152_(
                              new MessageScreen(
                                    "Save Failed",
                                    "Failed to save style " + currentStyle + ": " + e.getMessage(),
                                    this,
                                    button2 -> this.f_96541_.m_91152_(this),
                                    180,
                                    90
                                 )
                                 .autoCalculateHeight()
                           );
                        return;
                     }

                     CompoundTag stylesCompound = new CompoundTag();
                     stylesCompound.m_128365_("cases", caseListTag);
                     stylesCompound.m_128359_("default", ParseUtil.nullParam(this.defaultStyle._getValue()).toLowerCase(Locale.ROOT));
                     this.rootTag.m_128473_("styles");
                     this.rootTag.m_128365_("styles", stylesCompound);
                     this.m_7379_();
                  }
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
      Static defaultStyleTitle = new Static(
         this,
         12,
         100,
         15,
         53,
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         ResizableComponent.VerticalSizing.HEIGHT_BOTTOM,
         "datapack_edit.weapon_type.styles.default"
      );
      defaultStyleTitle.resize(screenRectangle);
      Static conditionTitle = new Static(
         this,
         187,
         100,
         40,
         15,
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         ResizableComponent.VerticalSizing.TOP_HEIGHT,
         "datapack_edit.weapon_type.styles.condition"
      );
      conditionTitle.resize(screenRectangle);
      Static parameterTitle = new Static(
         this,
         187,
         100,
         115,
         15,
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         ResizableComponent.VerticalSizing.TOP_HEIGHT,
         "datapack_edit.weapon_type.styles.parameters"
      );
      parameterTitle.resize(screenRectangle);
      this.m_142416_(
         new Static(
            this,
            12,
            60,
            40,
            15,
            ResizableComponent.HorizontalSizing.LEFT_WIDTH,
            null,
            Component.m_237115_("datapack_edit.styles"),
            Component.m_237115_("datapack_edit.styles.tooltip.optional")
         )
      );
      this.m_142416_(this.stylesGrid);
      this.m_142416_(defaultStyleTitle);
      this.m_142416_(this.defaultStyle);
      this.m_142416_(conditionTitle);
      this.m_142416_(this.conditionGrid);
      this.m_142416_(parameterTitle);
      this.m_142416_(this.parameterGrid);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
   }

   public boolean m_6375_(double mouseX, double mouseY, int action) {
      if (this.m_7222_() != null && this.m_7222_().m_6375_(mouseX, mouseY, action)) {
         if (action == 0) {
            this.m_7897_(true);
         }

         return true;
      } else {
         for (GuiEventListener guieventlistener : this.m_6702_()) {
            if (guieventlistener != this.m_7222_() && guieventlistener.m_6375_(mouseX, mouseY, action)) {
               this.m_7522_(guieventlistener);
               if (action == 0) {
                  this.m_7897_(true);
               }

               return true;
            }
         }

         return false;
      }
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
         if (!tag.m_128441_("conditions")) {
            throw new IllegalStateException("Define conditions");
         }

         for (Tag conditionTag : tag.m_128437_("conditions", 10)) {
            CompoundTag conditionCompound = (CompoundTag)conditionTag;
            Supplier<Condition<?>> condition = EpicFightConditions.getConditionOrThrow(ResourceLocation.parse(conditionCompound.m_128461_("predicate")));
            condition.get().read(conditionCompound);
         }

         if (!tag.m_128441_("style") || StringUtil.isNullOrEmpty(tag.m_128461_("style"))) {
            throw new IllegalStateException("Define a style");
         }
      } catch (Exception e) {
         throw new IllegalStateException(e.getMessage());
      }
   }
}
