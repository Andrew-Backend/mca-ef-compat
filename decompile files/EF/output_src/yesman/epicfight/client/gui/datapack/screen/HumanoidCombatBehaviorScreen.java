package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.InputComponentList;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.client.gui.datapack.widgets.SubScreenOpenButton;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class HumanoidCombatBehaviorScreen extends Screen {
   private final Screen parentScreen;
   private final InputComponentList<CompoundTag> inputComponentsList;
   private final CompoundTag rootTag;
   private final List<CompoundTag> weaponList = Lists.newLinkedList();
   private Grid weaponGrid;
   private Grid weaponCategoriesGrid;
   private ComboBox<Style> styleCombo;

   public HumanoidCombatBehaviorScreen(
      Screen parentScreen, CompoundTag rootTag, AssetAccessor<? extends Armature> armature, AssetAccessor<? extends SkinnedMesh> mesh
   ) {
      super(Component.m_237115_("datapack_edit.mob_patch.humanoid_combat_behavior"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.rootTag = rootTag;
      this.inputComponentsList = new InputComponentList<CompoundTag>(this, 0, 0, 0, 0, 26) {
         public void importTag(CompoundTag tag) {
            this.setComponentsActive(true);
            ListTag weaponCategoriesList = tag.m_128437_("weapon_categories", 8);
            Grid.PackImporter packImporter = new Grid.PackImporter();

            for (Tag weaponCategory : weaponCategoriesList) {
               packImporter.newRow();
               packImporter.newValue("weapon_category", weaponCategory.m_7916_());
            }

            this.setDataBindingComponenets(new Object[]{packImporter, Style.ENUM_MANAGER.get(tag.m_128461_("style"))});
         }
      };
      this.inputComponentsList.m_93507_(150);
      this.weaponGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(12, 50)
         .xy2(130, 50)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(26)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> this.inputComponentsList.importTag(this.weaponList.get(rowposition)))
         .addColumn(Grid.editbox("weapon").editable(false).width(180))
         .pressAdd((grid, button) -> {
            grid.setValueChangeEnabled(false);
            int rowposition = grid.addRowWithDefaultValues("weapon", String.format("weapon%d", grid.m_6702_().size() + 1));
            this.weaponList.add(rowposition, new CompoundTag());
            grid.setGridFocus(rowposition, "weapon");
            grid.setValueChangeEnabled(true);
            this.m_7522_(grid);
            if (grid.m_6702_().size() > 0) {
               this.inputComponentsList.setComponentsActive(true);
            }
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> this.weaponList.remove(removedRow));
            if (this.weaponList.size() == 0) {
               this.inputComponentsList.setComponentsActive(false);
            }
         })
         .build();
      this.weaponCategoriesGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(4, 40)
         .xy2(12, 120)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .addColumn(
            Grid.combo("weapon_category", List.copyOf(CapabilityItem.WeaponCategories.ENUM_MANAGER.universalValues())).editable(true).valueChanged(event -> {
               CompoundTag weaponBehaviorTag = this.weaponList.get(this.weaponGrid.getRowposition());
               ListTag weaponCategoriesList = weaponBehaviorTag.m_128437_("weapon_categories", 8);
               weaponCategoriesList.set(event.rowposition, StringTag.m_129297_(ParseUtil.nullParam(event.postValue).toLowerCase(Locale.ROOT)));
            }).width(150)
         )
         .pressAdd((grid, button) -> {
            grid.setValueChangeEnabled(false);
            int rowposition = grid.addRow();
            CompoundTag weaponBehaviorTag = this.weaponList.get(this.weaponGrid.getRowposition());
            ListTag weaponCategoriesList = ParseUtil.getOrDefaultTag(weaponBehaviorTag, "weapon_categories", new ListTag());
            weaponCategoriesList.add(rowposition, StringTag.m_129297_(""));
            grid.setGridFocus(rowposition, "weapon_category");
            grid.setValueChangeEnabled(true);
         })
         .pressRemove((grid, button) -> grid.removeRow(removedRow -> {
            CompoundTag weaponBehaviorTag = this.weaponList.get(this.weaponGrid.getRowposition());
            ListTag weaponCategoriesList = weaponBehaviorTag.m_128437_("weapon_categories", 8);
            weaponCategoriesList.remove(removedRow);
         }))
         .build();
      this.styleCombo = new ComboBox<>(
         this,
         this.f_96547_,
         55,
         116,
         0,
         16,
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         null,
         8,
         Component.m_237115_("datapack_edit.style"),
         new ArrayList<>(Style.ENUM_MANAGER.universalValues()),
         ParseUtil::snakeToSpacedCamel,
         style -> {
            CompoundTag weaponBehaviorTag = this.weaponList.get(this.weaponGrid.getRowposition());
            weaponBehaviorTag.m_128359_("style", ParseUtil.nullParam(style).toLowerCase(Locale.ROOT));
         }
      );
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               100,
               100,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               Component.m_237115_("datapack_edit.mob_patch.humanoid_weapon_motions.weapon_categories")
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.addComponentCurrentRow(this.weaponCategoriesGrid);
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               100,
               100,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               Component.m_237115_("datapack_edit.style")
            )
         );
      this.inputComponentsList.addComponentCurrentRow(this.styleCombo.relocateX(this.m_264198_(), this.inputComponentsList.nextStart(5)));
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               100,
               100,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               Component.m_237115_("datapack_edit.mob_patch.combat_behavior")
            )
         );
      this.inputComponentsList
         .addComponentCurrentRow(
            SubScreenOpenButton.builder()
               .subScreen(() -> new CombatBehaviorScreen(this, this.weaponList.get(this.weaponGrid.getRowposition()), armature, mesh, true))
               .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
               .build()
         );
      if (this.rootTag.m_128441_("combat_behavior_humanoid")) {
         Grid.PackImporter packImporter = new Grid.PackImporter();
         ListTag list = this.rootTag.m_128437_("combat_behavior_humanoid", 10);

         for (int i = 0; i < list.size(); i++) {
            this.weaponList.add(list.m_128728_(i));
            packImporter.newRow();
            packImporter.newValue("weapon", "weapon" + (i + 1));
         }

         this.weaponGrid._setValue(packImporter);
      }

      this.inputComponentsList.setComponentsActive(false);
   }

   protected void m_7856_() {
      ScreenRectangle screenRectangle = this.m_264198_();
      this.inputComponentsList
         .m_93437_(screenRectangle.f_263770_() - 150, screenRectangle.f_263800_(), screenRectangle.m_274449_() + 45, screenRectangle.f_263800_() - 45);
      this.inputComponentsList.m_93507_(150);
      this.weaponGrid.resize(screenRectangle);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_286989_,
               button -> {
                  ListTag newListTag = new ListTag();
                  int idx = 0;

                  for (CompoundTag tag : this.weaponList) {
                     try {
                        this.validateTagSave(tag);
                        newListTag.add(tag);
                        idx++;
                     } catch (Exception e) {
                        this.f_96541_
                           .m_91152_(
                              new MessageScreen(
                                    "Save Failed",
                                    "Failed to save " + this.weaponGrid.getValue(idx, "weapon") + ": " + e.getMessage(),
                                    this,
                                    button2 -> this.f_96541_.m_91152_(this),
                                    180,
                                    90
                                 )
                                 .autoCalculateHeight()
                           );
                        return;
                     }
                  }

                  this.rootTag.m_128473_("combat_behavior_humanoid");
                  this.rootTag.m_128365_("combat_behavior_humanoid", newListTag);
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
      this.m_142416_(this.weaponGrid);
      this.m_142416_(this.inputComponentsList);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      return this.inputComponentsList.m_7979_(mouseX, mouseY, button, dx, dy) ? true : super.m_7979_(mouseX, mouseY, button, dx, dy);
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
      if (tag.m_128441_("weapon_categories") && tag.m_128437_("weapon_categories", 8).size() != 0) {
         for (Tag weaponCategoryTag : tag.m_128437_("weapon_categories", 8)) {
            if (StringUtil.isNullOrEmpty(weaponCategoryTag.m_7916_())) {
               throw new IllegalStateException("Invalid weapon category");
            }

            try {
               WeaponCategory.ENUM_MANAGER.getOrThrow(weaponCategoryTag.m_7916_());
            } catch (IllegalArgumentException e) {
               throw new IllegalStateException("Invalid weapon category " + weaponCategoryTag.m_7916_());
            }
         }

         if (!tag.m_128441_("style") || StringUtil.isNullOrEmpty(tag.m_128461_("style"))) {
            throw new IllegalStateException("Define a style");
         }

         if (!tag.m_128441_("behavior_series")) {
            throw new IllegalStateException("Define combat behavior");
         }
      } else {
         throw new IllegalStateException("Define at least one weapon category");
      }
   }
}
