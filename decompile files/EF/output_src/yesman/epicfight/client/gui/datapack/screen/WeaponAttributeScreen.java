package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;

public class WeaponAttributeScreen extends Screen {
   private final Map<String, Condition.ParameterEditor> weaponAttributeEditors = Maps.newLinkedHashMap();
   private final Map<String, Condition.ParameterEditor> armorAttributeEditors = Maps.newLinkedHashMap();
   private final Screen parentScreen;
   private final DatapackEditScreen.ItemCapabilityTab.ItemType itemType;
   private Grid stylesGrid;
   private Grid attributesGrid;
   private final List<PackEntry<String, CompoundTag>> styles = Lists.newArrayList();
   private final CompoundTag rootTag;

   public WeaponAttributeScreen(Screen parentScreen, CompoundTag rootTag, DatapackEditScreen.ItemCapabilityTab.ItemType itemType) {
      super(Component.m_237115_("datapack_edit.item_capability.attributes"));
      this.itemType = itemType;
      this.parentScreen = parentScreen;
      this.rootTag = rootTag;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      ResizableEditBox impactEditBox = new ResizableEditBox(this.f_96547_, 0, 0, 0, 0, Component.m_237113_("impact"), null, null);
      ResizableEditBox armorNegationEditBox = new ResizableEditBox(this.f_96547_, 0, 0, 0, 0, Component.m_237113_("armor_negation"), null, null);
      ResizableEditBox maxStrikesEditBox = new ResizableEditBox(this.f_96547_, 0, 0, 0, 0, Component.m_237113_("max_strikes"), null, null);
      ResizableEditBox damageBonusEditBox = new ResizableEditBox(this.f_96547_, 0, 0, 0, 0, Component.m_237113_("damage_bonus"), null, null);
      ResizableEditBox speedBonusEditBox = new ResizableEditBox(this.f_96547_, 0, 0, 0, 0, Component.m_237113_("speed_bonus"), null, null);
      ResizableEditBox stunArmorEditBox = new ResizableEditBox(this.f_96547_, 0, 0, 0, 0, Component.m_237113_("stun_armor"), null, null);
      ResizableEditBox weightEditBox = new ResizableEditBox(this.f_96547_, 0, 0, 0, 0, Component.m_237113_("weight"), null, null);
      impactEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
      armorNegationEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
      maxStrikesEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Integer::parseInt));
      damageBonusEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
      speedBonusEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
      stunArmorEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
      weightEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
      this.weaponAttributeEditors
         .put(
            "armor_negation",
            Condition.ParameterEditor.of(
               value -> DoubleTag.m_128500_(ParseUtil.parseOrGet(value.toString(), Double::parseDouble, 0.0)),
               tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()),
               armorNegationEditBox
            )
         );
      this.weaponAttributeEditors
         .put(
            "impact",
            Condition.ParameterEditor.of(
               value -> DoubleTag.m_128500_(ParseUtil.parseOrGet(value.toString(), Double::parseDouble, 0.0)),
               tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()),
               impactEditBox
            )
         );
      this.weaponAttributeEditors
         .put(
            "max_strikes",
            Condition.ParameterEditor.of(
               value -> IntTag.m_128679_(ParseUtil.parseOrGet(value.toString(), Integer::parseInt, 0)),
               tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()),
               maxStrikesEditBox
            )
         );
      this.weaponAttributeEditors
         .put(
            "damage_bonus",
            Condition.ParameterEditor.of(
               value -> DoubleTag.m_128500_(ParseUtil.parseOrGet(value.toString(), Double::parseDouble, 0.0)),
               tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()),
               damageBonusEditBox
            )
         );
      this.weaponAttributeEditors
         .put(
            "speed_bonus",
            Condition.ParameterEditor.of(
               value -> DoubleTag.m_128500_(ParseUtil.parseOrGet(value.toString(), Double::parseDouble, 0.0)),
               tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()),
               speedBonusEditBox
            )
         );
      this.armorAttributeEditors
         .put(
            "stun_armor",
            Condition.ParameterEditor.of(
               value -> DoubleTag.m_128500_(ParseUtil.parseOrGet(value.toString(), Double::parseDouble, 0.0)),
               tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()),
               stunArmorEditBox
            )
         );
      this.armorAttributeEditors
         .put(
            "weight",
            Condition.ParameterEditor.of(
               value -> DoubleTag.m_128500_(ParseUtil.parseOrGet(value.toString(), Double::parseDouble, 0.0)),
               tag -> ParseUtil.valueOfOmittingType(tag.m_7916_()),
               weightEditBox
            )
         );
      if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON) {
         this.stylesGrid = Grid.builder(this, parentScreen.getMinecraft())
            .xy1(20, 60)
            .xy2(90, 50)
            .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
            .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
            .rowHeight(21)
            .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
            .transparentBackground(false)
            .rowpositionChanged(
               (rowposition, values) -> {
                  Grid.PackImporter packImporterx = new Grid.PackImporter();

                  for (Entry<String, Tag> entryx : this.styles.get(rowposition).getValue().f_128329_.entrySet()) {
                     Condition.ParameterEditor paramEditorx = this.weaponAttributeEditors.get(entryx.getKey());
                     packImporterx.newRow()
                        .newValue("attribute", this.weaponAttributeEditors.get(entryx.getKey()))
                        .newValue("amount", paramEditorx == null ? "" : paramEditorx.fromTag.apply(entryx.getValue()));
                  }

                  this.attributesGrid._setActive(true);
                  this.attributesGrid._setValue(packImporterx);
               }
            )
            .addColumn(
               Grid.combo("style", Style.ENUM_MANAGER.universalValues())
                  .valueChanged(event -> this.styles.get(event.rowposition).setPackKey(ParseUtil.nullParam(event.postValue).toLowerCase(Locale.ROOT)))
                  .defaultVal(CapabilityItem.Styles.ONE_HAND)
            )
            .pressAdd((grid, button) -> {
               this.styles.add(PackEntry.of("", CompoundTag::new));
               int rowposition = grid.addRow();
               grid.setGridFocus(rowposition, "style");
            })
            .pressRemove((grid, button) -> {
               grid.removeRow(removedRow -> this.styles.remove(removedRow));
               if (grid.m_6702_().size() == 0) {
                  this.attributesGrid._setActive(false);
               }
            })
            .build();
         this.attributesGrid = Grid.builder(this, parentScreen.getMinecraft())
            .xy1(120, 60)
            .xy2(20, 50)
            .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
            .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
            .rowHeight(21)
            .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
            .transparentBackground(false)
            .addColumn(
               Grid.combo("attribute", List.copyOf(this.weaponAttributeEditors.values()))
                  .toDisplayText(editor -> ParseUtil.nullOrToString(editor, editor$1 -> ParseUtil.snakeToSpacedCamel(editor.editWidget.m_6035_().getString())))
                  .valueChanged(event -> {
                     CompoundTag attributesCompound = this.styles.get(this.stylesGrid.getRowposition()).getValue();
                     if (event.prevValue != null) {
                        attributesCompound.m_128473_(event.prevValue.editWidget.m_6035_().getString());
                     } else {
                        attributesCompound.m_128473_("");
                     }

                     attributesCompound.m_128359_(ParseUtil.nullParam(event.postValue.editWidget.m_6035_().getString()), "");
                  })
                  .width(100)
            )
            .addColumn(Grid.wildcard("amount").editWidgetProvider(row -> {
               Condition.ParameterEditor editor = row.getValue("attribute");
               return editor == null ? null : editor.editWidget;
            }).valueChanged(event -> {
               CompoundTag attributesTag = this.styles.get(this.stylesGrid.getRowposition()).getValue();
               Condition.ParameterEditor editor = event.grid.getValue(event.rowposition, "attribute");
               if (!StringUtil.isNullOrEmpty(ParseUtil.nullParam(event.postValue))) {
                  attributesTag.m_128365_(editor.editWidget.m_6035_().getString(), editor.toTag.apply(event.postValue));
               } else {
                  attributesTag.m_128473_(editor.editWidget.m_6035_().getString());
               }
            }).width(150))
            .pressAdd((grid, button) -> {
               this.styles.get(this.stylesGrid.getRowposition()).getValue().m_128365_("", StringTag.m_129297_(""));
               int rowposition = grid.addRow();
               grid.setGridFocus(rowposition, "attribute");
            })
            .pressRemove(
               (grid, button) -> {
                  Object attributeGridRow = grid.getValue(grid.getRowposition(), "attribute");
                  this.styles
                     .get(this.stylesGrid.getRowposition())
                     .getValue()
                     .m_128473_(attributeGridRow == null ? "" : ((Condition.ParameterEditor)attributeGridRow).editWidget.m_6035_().getString());
                  grid.removeRow(removedRow -> {});
               }
            )
            .build();
         Grid.PackImporter packImporter = new Grid.PackImporter();

         for (Entry<String, Tag> entry : rootTag.f_128329_.entrySet()) {
            this.styles.add(PackEntry.of(entry.getKey(), () -> (CompoundTag)entry.getValue()));
            packImporter.newRow();
            packImporter.newValue("style", Style.ENUM_MANAGER.get(entry.getKey()));
         }

         this.stylesGrid._setValue(packImporter);
         this.attributesGrid._setActive(false);
      } else if (itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.ARMOR) {
         this.styles.add(PackEntry.of("armor", CompoundTag::new));
         this.attributesGrid = Grid.builder(this, parentScreen.getMinecraft())
            .xy1(20, 60)
            .xy2(20, 50)
            .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
            .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
            .rowHeight(21)
            .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
            .transparentBackground(false)
            .addColumn(
               Grid.combo("attribute", List.copyOf(this.armorAttributeEditors.values()))
                  .toDisplayText(editor -> ParseUtil.nullOrToString(editor, editor$1 -> ParseUtil.snakeToSpacedCamel(editor.editWidget.m_6035_().getString())))
                  .valueChanged(event -> {
                     CompoundTag attributesCompound = this.styles.get(0).getValue();
                     if (event.prevValue != null) {
                        attributesCompound.m_128473_(event.prevValue.editWidget.m_6035_().getString());
                     } else {
                        attributesCompound.m_128473_("");
                     }

                     attributesCompound.m_128359_(ParseUtil.nullParam(event.postValue.editWidget.m_6035_().getString()), "");
                  })
                  .width(100)
            )
            .addColumn(Grid.wildcard("amount").editWidgetProvider(row -> {
               Condition.ParameterEditor editor = row.getValue("attribute");
               return editor == null ? null : editor.editWidget;
            }).valueChanged(event -> {
               CompoundTag attributesTag = this.styles.get(0).getValue();
               Condition.ParameterEditor editor = event.grid.getValue(event.rowposition, "attribute");
               if (!StringUtil.isNullOrEmpty(ParseUtil.nullParam(event.postValue))) {
                  attributesTag.m_128365_(editor.editWidget.m_6035_().getString(), editor.toTag.apply(event.postValue));
               } else {
                  attributesTag.m_128473_(editor.editWidget.m_6035_().getString());
               }
            }).width(150))
            .pressAdd((grid, button) -> {
               this.styles.get(0).getValue().m_128365_("", StringTag.m_129297_(""));
               int rowposition = grid.addRow();
               grid.setGridFocus(rowposition, "attribute");
            })
            .pressRemove((grid, button) -> {
               this.styles.get(0).getValue().m_128473_(grid.getValue(grid.getRowposition(), "attribute"));
               grid.removeRow(removedRow -> {});
            })
            .build();
         this.styles.add(PackEntry.of("attributes", CompoundTag::new));
         Grid.PackImporter packImporter = new Grid.PackImporter();

         for (Entry<String, Tag> entry : rootTag.f_128329_.entrySet()) {
            Condition.ParameterEditor paramEditor = this.armorAttributeEditors.get(entry.getKey());
            packImporter.newRow();
            packImporter.newValue("attribute", this.armorAttributeEditors.get(entry.getKey()));
            packImporter.newValue("amount", paramEditor.fromTag.apply(entry.getValue()));
         }

         this.attributesGrid._setValue(packImporter);
      }
   }

   protected void m_7856_() {
      if (this.itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON) {
         this.stylesGrid.resize(this.m_264198_());
         this.m_142416_(new Static(this, 20, 60, 40, 15, ResizableComponent.HorizontalSizing.LEFT_WIDTH, null, Component.m_237115_("datapack_edit.styles")));
         this.m_142416_(this.stylesGrid);
      }

      this.attributesGrid.resize(this.m_264198_());
      this.m_142416_(
         new Static(
            this,
            this.itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON ? 120 : 20,
            60,
            40,
            15,
            ResizableComponent.HorizontalSizing.LEFT_WIDTH,
            null,
            Component.m_237115_("datapack_edit.item_capability.attributes")
         )
      );
      this.m_142416_(this.attributesGrid);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_130655_,
               button -> {
                  if (this.itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.WEAPON) {
                     Set<String> styles = Sets.newHashSet();

                     for (PackEntry<String, CompoundTag> entry : this.styles) {
                        if (styles.contains(entry.getKey())) {
                           this.f_96541_
                              .m_91152_(
                                 new MessageScreen(
                                    "Save Failed",
                                    "Unable to save because of duplicated style: " + entry.getKey(),
                                    this,
                                    button2 -> this.f_96541_.m_91152_(this),
                                    180,
                                    90
                                 )
                              );
                           return;
                        }

                        styles.add(entry.getKey());
                     }

                     this.rootTag.f_128329_.clear();

                     for (PackEntry<String, CompoundTag> entry : this.styles) {
                        this.rootTag.m_128365_(entry.getKey(), (Tag)entry.getValue());
                     }
                  } else if (this.itemType == DatapackEditScreen.ItemCapabilityTab.ItemType.ARMOR) {
                     CompoundTag attributesTag = this.styles.get(0).getValue();

                     for (Entry<String, Tag> tag : attributesTag.f_128329_.entrySet()) {
                        this.rootTag.m_128365_(tag.getKey(), tag.getValue());
                     }
                  }

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
}
