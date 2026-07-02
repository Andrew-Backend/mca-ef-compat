package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
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
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.InputComponentList;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class HumanoidWeaponMotionScreen extends Screen {
   private Grid motionSetGrid;
   private InputComponentList<CompoundTag> inputComponentsList;
   private final List<CompoundTag> motionSetList = Lists.newLinkedList();
   private final Screen parentScreen;
   private final CompoundTag rootTag;

   protected HumanoidWeaponMotionScreen(
      Screen parentScreen, CompoundTag rootTag, AssetAccessor<? extends Armature> armature, AssetAccessor<? extends SkinnedMesh> mesh
   ) {
      super(Component.m_237115_("datapack_edit.mob_patch.humanoid_weapon_motions"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.rootTag = rootTag;
      this.motionSetGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(8, 45)
         .xy2(150, 50)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(26)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> this.inputComponentsList.importTag(this.motionSetList.get(rowposition)))
         .addColumn(Grid.editbox("motion_set").editable(false).width(180))
         .pressAdd((grid, button) -> {
            grid.setValueChangeEnabled(false);
            int rowposition = grid.addRowWithDefaultValues("motion_set", String.format("set%d", grid.m_6702_().size() + 1));
            this.motionSetList.add(rowposition, new CompoundTag());
            grid.setGridFocus(rowposition, "motion_set");
            grid.setValueChangeEnabled(true);
            this.m_7522_(grid);
            if (grid.m_6702_().size() > 0) {
               this.inputComponentsList.setComponentsActive(true);
            }
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> this.motionSetList.remove(removedRow));
            if (this.motionSetList.size() == 0) {
               this.inputComponentsList.setComponentsActive(false);
            }
         })
         .build();
      this.inputComponentsList = new InputComponentList<CompoundTag>(this, 0, 0, 0, 0, 28) {
         public void importTag(CompoundTag tag) {
            Grid.PackImporter categories = new Grid.PackImporter();

            for (Tag weaponCategory : tag.m_128437_("weapon_categories", 8)) {
               categories.newRow();
               categories.newValue("weapon_category", WeaponCategory.ENUM_MANAGER.get(weaponCategory.m_7916_()));
            }

            Grid.PackImporter livingMotions = new Grid.PackImporter();

            for (Entry<String, Tag> entry : tag.m_128469_("livingmotions").f_128329_.entrySet()) {
               livingMotions.newRow();
               livingMotions.newValue("living_motion", LivingMotion.ENUM_MANAGER.get(entry.getKey()));
               livingMotions.newValue("animation", DatapackEditScreen.animationByKey(entry.getValue().m_7916_()));
            }

            Style style = Style.ENUM_MANAGER.get(tag.m_128461_("style"));
            this.setDataBindingComponenets(new Object[]{categories, style, livingMotions});
            this.setComponentsActive(true);
         }
      };
      this.inputComponentsList.m_93507_(164);
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(5),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.humanoid_weapon_motions.weapon_categories"
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            Grid.builder(this, parentScreen.getMinecraft())
               .xy1(this.inputComponentsList.nextStart(5), 0)
               .xy2(10, 80)
               .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
               .rowHeight(21)
               .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
               .transparentBackground(false)
               .addColumn(Grid.combo("weapon_category", List.of(CapabilityItem.WeaponCategories.values())).editable(true).valueChanged(event -> {
                  ListTag listTag = ParseUtil.getOrDefaultTag(this.motionSetList.get(this.motionSetGrid.getRowposition()), "weapon_categories", new ListTag());
                  listTag.remove(event.rowposition);
                  listTag.add(event.rowposition, StringTag.m_129297_(ParseUtil.nullParam(event.postValue).toLowerCase(Locale.ROOT)));
               }).width(180))
               .pressAdd((grid, button) -> {
                  grid.setValueChangeEnabled(false);
                  int rowposition = grid.addRow();
                  ListTag listTag = ParseUtil.getOrDefaultTag(this.motionSetList.get(this.motionSetGrid.getRowposition()), "weapon_categories", new ListTag());
                  listTag.add(rowposition, StringTag.m_129297_(""));
                  grid.setGridFocus(rowposition, "weapon_category");
                  grid.setValueChangeEnabled(true);
               })
               .pressRemove((grid, button) -> grid.removeRow(removedRow -> {
                  ListTag listTag = ParseUtil.getOrDefaultTag(this.motionSetList.get(this.motionSetGrid.getRowposition()), "weapon_categories", new ListTag());
                  listTag.remove(removedRow);
               }))
               .build()
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(5),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.humanoid_weapon_motions.style"
            )
         );
      this.inputComponentsList
         .addComponentCurrentRow(
            new ComboBox<>(
               this,
               this.f_96547_,
               this.inputComponentsList.nextStart(5),
               116,
               15,
               53,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               ResizableComponent.VerticalSizing.HEIGHT_BOTTOM,
               8,
               Component.m_237115_("datapack_edit.weapon_type.styles.default"),
               new ArrayList<>(Style.ENUM_MANAGER.universalValues()),
               ParseUtil::snakeToSpacedCamel,
               style -> {
                  CompoundTag tag = this.motionSetList.get(this.motionSetGrid.getRowposition());
                  tag.m_128359_("style", ParseUtil.nullParam(style).toLowerCase(Locale.ROOT));
               }
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(5),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.humanoid_weapon_motions.living_animations"
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            Grid.builder(this, parentScreen.getMinecraft())
               .xy1(this.inputComponentsList.nextStart(5), 0)
               .xy2(10, 80)
               .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
               .rowHeight(21)
               .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
               .transparentBackground(false)
               .addColumn(
                  Grid.combo(
                        "living_motion",
                        List.of(
                           LivingMotions.IDLE,
                           LivingMotions.WALK,
                           LivingMotions.CHASE,
                           LivingMotions.MOUNT,
                           LivingMotions.FALL,
                           LivingMotions.FLOAT,
                           LivingMotions.DEATH,
                           LivingMotions.RELOAD,
                           LivingMotions.AIM
                        )
                     )
                     .valueChanged(
                        event -> {
                           CompoundTag livingMotionTag = ParseUtil.getOrSupply(
                              this.motionSetList.get(this.motionSetGrid.getRowposition()), "livingmotions", CompoundTag::new
                           );
                           livingMotionTag.m_128473_(ParseUtil.nullParam(event.prevValue));
                           livingMotionTag.m_128359_(
                              ParseUtil.nullOrToString(event.postValue, livingmotion -> livingmotion.name().toLowerCase(Locale.ROOT)), ""
                           );
                        }
                     )
                     .editable(true)
                     .width(100)
               )
               .addColumn(
                  Grid.popup("animation", PopupBox.AnimationPopupBox::new)
                     .filter(accessor -> !accessor.checkType(MainFrameAnimation.class))
                     .editWidgetCreated(popupBox -> popupBox.setModel(armature, mesh))
                     .valueChanged(
                        event -> {
                           CompoundTag livingMotionTag = ParseUtil.getOrSupply(
                              this.motionSetList.get(this.motionSetGrid.getRowposition()), "livingmotions", CompoundTag::new
                           );
                           livingMotionTag.m_128359_(
                              ParseUtil.nullOrToString(
                                 (LivingMotions)event.grid.getValue(event.rowposition, "living_motion"),
                                 livingmotion -> ((LivingMotions)livingmotion).name().toLowerCase(Locale.ROOT)
                              ),
                              ParseUtil.nullOrToString(event.postValue, animation -> animation.registryName().toString())
                           );
                        }
                     )
                     .toDisplayText(item -> item == null ? "" : item.registryName().toString())
                     .width(150)
               )
               .pressAdd(
                  (grid, button) -> {
                     CompoundTag attributeTag = ParseUtil.getOrDefaultTag(
                        this.motionSetList.get(this.motionSetGrid.getRowposition()), "livingmotions", new CompoundTag()
                     );
                     attributeTag.m_128359_("", "");
                     int rowposition = grid.addRow();
                     grid.setGridFocus(rowposition, "living_motion");
                  }
               )
               .pressRemove(
                  (grid, button) -> {
                     int rowposition = grid.getRowposition();
                     if (rowposition > -1) {
                        CompoundTag livingMotionTag = ParseUtil.getOrDefaultTag(
                           this.motionSetList.get(this.motionSetGrid.getRowposition()), "livingmotions", new CompoundTag()
                        );
                        livingMotionTag.m_128473_(ParseUtil.nullParam(grid.getValue(rowposition, "living_motion")).toLowerCase(Locale.ROOT));
                        grid.removeRow(rowposition);
                     }
                  }
               )
               .build()
         );
      this.inputComponentsList.newRow();
      if (this.rootTag.m_128441_("humanoid_weapon_motions")) {
         Grid.PackImporter packImporter = new Grid.PackImporter();
         ListTag list = this.rootTag.m_128437_("humanoid_weapon_motions", 10);

         for (int i = 0; i < list.size(); i++) {
            this.motionSetList.add(list.m_128728_(i));
            packImporter.newRow();
            packImporter.newValue("motion_set", "set" + (i + 1));
         }

         this.motionSetGrid._setValue(packImporter);
      }

      this.inputComponentsList.setComponentsActive(false);
   }

   protected void m_7856_() {
      ScreenRectangle screenRectangle = this.m_264198_();
      this.motionSetGrid.resize(screenRectangle);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_130655_,
               button -> {
                  ListTag newListTag = new ListTag();
                  int idx = 0;

                  for (CompoundTag tag : this.motionSetList) {
                     try {
                        this.validateTagSave(tag);
                        newListTag.add(tag);
                        idx++;
                     } catch (Exception e) {
                        this.f_96541_
                           .m_91152_(
                              new MessageScreen(
                                 "Save Failed",
                                 "Failed to save " + this.motionSetGrid.getValue(idx, "motion_set") + ": " + e.getMessage(),
                                 this,
                                 button2 -> this.f_96541_.m_91152_(this),
                                 180,
                                 90
                              )
                           );
                        return;
                     }
                  }

                  this.rootTag.m_128473_("humanoid_weapon_motions");
                  this.rootTag.m_128365_("humanoid_weapon_motions", newListTag);
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
      this.inputComponentsList
         .m_93437_(screenRectangle.f_263770_() - 172, screenRectangle.f_263800_(), screenRectangle.m_274449_() + 35, screenRectangle.m_274349_() - 50);
      this.inputComponentsList.m_93507_(164);
      this.m_142416_(this.motionSetGrid);
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
      if (!tag.m_128441_("weapon_categories") || tag.m_128437_("weapon_categories", 8).size() == 0) {
         throw new IllegalStateException("Define at least one weapon category");
      }

      if (tag.m_128441_("style") && !StringUtil.isNullOrEmpty(tag.m_128461_("style"))) {
         if (tag.m_128441_("livingmotions") && tag.m_128469_("livingmotions").f_128329_.size() != 0) {
            int idx = 1;

            for (Entry<String, Tag> entry : tag.m_128469_("livingmotions").f_128329_.entrySet()) {
               if (StringUtil.isNullOrEmpty(entry.getKey())) {
                  throw new IllegalStateException("Row " + idx + ": No living motion defined.");
               }

               if (StringUtil.isNullOrEmpty(entry.getValue().m_7916_())) {
                  throw new IllegalStateException("Row " + idx + ": No animation defined.");
               }

               idx++;
            }
         } else {
            throw new IllegalStateException("Define at least one living motion");
         }
      } else {
         throw new IllegalStateException("Define a style");
      }
   }
}
