package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.InputComponentList;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;

public class WeaponComboScreen extends Screen {
   private final Screen parentScreen;
   private Grid stylesGrid;
   private Grid comboGrid;
   private PopupBox.AnimationPopupBox dashAttackPopupbox;
   private PopupBox.AnimationPopupBox airSlashPopupbox;
   private final InputComponentList<ListTag> inputComponentsList;
   private final ModelPreviewer modelPreviewer;
   private final List<PackEntry<String, ListTag>> styles = Lists.newArrayList();
   private final CompoundTag rootTag;

   public WeaponComboScreen(Screen parentScreen, CompoundTag rootTag) {
      super(Component.m_237115_("datapack_edit.weapon_type.combos"));
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.parentScreen = parentScreen;
      this.inputComponentsList = new InputComponentList<ListTag>(this, 0, 0, 0, 0, 20) {
         public void importTag(ListTag tag) {
            this.setComponentsActive(true);
            boolean isMount = WeaponComboScreen.this.stylesGrid.getValue(WeaponComboScreen.this.stylesGrid.getRowposition(), "style")
               == CapabilityItem.Styles.MOUNT;
            if (!isMount) {
               while (tag.size() < 2) {
                  tag.add(StringTag.m_129297_(""));
               }
            }

            Grid.PackImporter packImporter = new Grid.PackImporter();
            int tagSize = tag.size();
            if (isMount) {
               for (int i = 0; i < tagSize; i++) {
                  packImporter.newRow().newValue("combo_animation", DatapackEditScreen.animationByKey(tag.m_128778_(i)));
               }

               this.setDataBindingComponenets(new Object[]{packImporter});
            } else {
               for (int i = 0; i < tagSize - 2; i++) {
                  packImporter.newRow().newValue("combo_animation", DatapackEditScreen.animationByKey(tag.m_128778_(i)));
               }

               this.setDataBindingComponenets(
                  new Object[]{
                     packImporter, DatapackEditScreen.animationByKey(tag.m_128778_(tagSize - 2)), DatapackEditScreen.animationByKey(tag.m_128778_(tagSize - 1))
                  }
               );
            }
         }
      };
      this.inputComponentsList.m_93507_(parentScreen.f_96543_ - 205);
      this.rootTag = ParseUtil.getOrDefaultTag(rootTag, "combos", new CompoundTag());
      this.stylesGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(12, 60)
         .xy2(85, 50)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> {
            this.inputComponentsList.importTag(this.styles.get(rowposition).getValue());
            this.reloadAnimationPlayer();
            if (values.get("style") == CapabilityItem.Styles.MOUNT) {
               this.dashAttackPopupbox._setValue(null);
               this.airSlashPopupbox._setValue(null);
               this.dashAttackPopupbox._setActive(false);
               this.airSlashPopupbox._setActive(false);
            }
         })
         .addColumn(Grid.combo("style", Style.ENUM_MANAGER.universalValues()).valueChanged(event -> {
            if (event.prevValue == CapabilityItem.Styles.MOUNT) {
               this.dashAttackPopupbox._setActive(true);
               this.airSlashPopupbox._setActive(true);
               ListTag combosList = this.styles.get(event.rowposition).getValue();
               combosList.add(StringTag.m_129297_(""));
               combosList.add(StringTag.m_129297_(""));
            } else if (event.postValue == CapabilityItem.Styles.MOUNT) {
               this.dashAttackPopupbox._setValue(null);
               this.airSlashPopupbox._setValue(null);
               this.dashAttackPopupbox._setActive(false);
               this.airSlashPopupbox._setActive(false);
               ListTag combosList = this.styles.get(event.rowposition).getValue();
               combosList.remove(combosList.size() - 1);
               combosList.remove(combosList.size() - 1);
            }

            this.styles.get(event.rowposition).setPackKey(ParseUtil.nullParam(event.postValue).toLowerCase(Locale.ROOT));
         }).defaultVal(CapabilityItem.Styles.ONE_HAND))
         .pressAdd((grid, button) -> {
            this.styles.add(PackEntry.of("", ListTag::new));
            int rowposition = grid.addRow();
            grid.setGridFocus(rowposition, "style");
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> this.styles.remove(removedRow));
            if (grid.m_6702_().size() == 0) {
               this.inputComponentsList.setComponentsActive(false);
            }
         })
         .build();
      this.comboGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(177, 40)
         .xy2(14, 80)
         .horizontalSizing(ResizableComponent.HorizontalSizing.WIDTH_RIGHT)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .addColumn(
            Grid.popup("combo_animation", PopupBox.AnimationPopupBox::new)
               .filter(accessor -> accessor.checkType(AttackAnimation.class))
               .editWidgetCreated(popupBox -> popupBox.setModel(Armatures.BIPED, Meshes.BIPED))
               .toDisplayText(animation -> animation == null ? "" : animation.registryName().toString())
               .valueChanged(
                  event -> {
                     ListTag animationList = this.styles.get(this.stylesGrid.getRowposition()).getValue();
                     animationList.remove(event.rowposition);
                     animationList.add(
                        event.rowposition,
                        StringTag.m_129297_(ParseUtil.nullOrToString((AssetAccessor)event.postValue, animation -> animation.registryName().toString()))
                     );
                     if (event.postValue != null) {
                        this.reloadAnimationPlayer();
                     }
                  }
               )
               .width(150)
         )
         .pressAdd((grid, button) -> {
            this.styles.get(this.stylesGrid.getRowposition()).getValue().add(grid.m_6702_().size(), StringTag.m_129297_(""));
            int rowposition = grid.addRow();
            grid.setGridFocus(rowposition, "combo_animation");
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> this.styles.get(this.stylesGrid.getRowposition()).getValue().remove(removedRow));
            this.reloadAnimationPlayer();
         })
         .build();
      this.modelPreviewer = new ModelPreviewer(
         110, 200, 45, 49, ResizableComponent.HorizontalSizing.LEFT_RIGHT, ResizableComponent.VerticalSizing.TOP_BOTTOM, Armatures.BIPED, Meshes.BIPED
      );
      CompoundTag colliderTag = rootTag.m_128469_("collider");

      try {
         Collider collider = ColliderPreset.deserializeSimpleCollider(colliderTag);
         this.modelPreviewer.setCollider(collider);
      } catch (IllegalArgumentException var7) {
      }

      this.dashAttackPopupbox = new PopupBox.AnimationPopupBox(
         this,
         this.f_96547_,
         110,
         15,
         -1,
         15,
         ResizableComponent.HorizontalSizing.WIDTH_RIGHT,
         null,
         Component.m_237115_("datapack_edit.weapon_type.styles.dash_attak"),
         pair -> {
            if (pair.getSecond() != null) {
               ListTag listTag = this.styles.get(this.stylesGrid.getRowposition()).getValue();
               listTag.remove(listTag.size() - 2);
               listTag.add(
                  listTag.size() - 1,
                  StringTag.m_129297_(
                     ParseUtil.nullOrToString((AssetAccessor)pair.getSecond(), animation$2 -> ((AssetAccessor)animation$2).registryName().toString())
                  )
               );
               this.reloadAnimationPlayer();
            }
         }
      );
      this.airSlashPopupbox = new PopupBox.AnimationPopupBox(
         this,
         this.f_96547_,
         110,
         15,
         -1,
         15,
         ResizableComponent.HorizontalSizing.WIDTH_RIGHT,
         null,
         Component.m_237115_("datapack_edit.weapon_type.styles.air_slash"),
         pair -> {
            if (pair.getSecond() != null) {
               ListTag listTag = this.styles.get(this.stylesGrid.getRowposition()).getValue();
               listTag.remove(listTag.size() - 1);
               listTag.add(
                  listTag.size(),
                  StringTag.m_129297_(
                     ParseUtil.nullOrToString((AssetAccessor)pair.getSecond(), animation$2 -> ((AssetAccessor)animation$2).registryName().toString())
                  )
               );
               this.reloadAnimationPlayer();
            }
         }
      );
      this.dashAttackPopupbox.setModel(Armatures.BIPED, Meshes.BIPED);
      this.airSlashPopupbox.setModel(Armatures.BIPED, Meshes.BIPED);
      this.dashAttackPopupbox.applyFilter(animation -> animation.checkType(AttackAnimation.class));
      this.airSlashPopupbox.applyFilter(animation -> animation.checkType(AttackAnimation.class));
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(this, 80, 110, -1, 15, ResizableComponent.HorizontalSizing.WIDTH_RIGHT, null, "datapack_edit.weapon_type.combos.combo_attacks")
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.addComponentCurrentRow(this.comboGrid);
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(this, 60, 130, -1, 15, ResizableComponent.HorizontalSizing.WIDTH_RIGHT, null, "datapack_edit.weapon_type.combos.dash_attak")
         );
      this.inputComponentsList.addComponentCurrentRow(this.dashAttackPopupbox);
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(this, 60, 130, -1, 15, ResizableComponent.HorizontalSizing.WIDTH_RIGHT, null, "datapack_edit.weapon_type.combos.air_slash")
         );
      this.inputComponentsList.addComponentCurrentRow(this.airSlashPopupbox);
      this.inputComponentsList.setComponentsActive(false);
      Grid.PackImporter packImporter = new Grid.PackImporter();

      for (String style : this.rootTag.m_128431_()) {
         this.styles.add(PackEntry.of(style, () -> this.rootTag.m_128437_(style, 8)));
         packImporter.newRow();
         packImporter.newValue("style", Style.ENUM_MANAGER.get(style));
      }

      this.stylesGrid._setValue(packImporter);
   }

   protected void m_7856_() {
      ScreenRectangle screenRectangle = this.m_264198_();
      this.stylesGrid.resize(this.m_264198_());
      this.inputComponentsList.m_93437_(205, screenRectangle.f_263800_(), screenRectangle.m_274449_() + 34, screenRectangle.f_263800_() - 45);
      this.inputComponentsList.m_93507_(this.f_96543_ - 205);
      this.modelPreviewer.resize(screenRectangle);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_130655_,
               button -> {
                  Set<String> styles = Sets.newHashSet();

                  for (PackEntry<String, ListTag> entry : this.styles) {
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

                  boolean allTagsNormal = true;
                  String animation = null;
                  String style = null;

                  label42:
                  for (PackEntry<String, ListTag> entry : this.styles) {
                     for (Tag tag : entry.getValue()) {
                        if (DatapackEditScreen.animationByKey(tag.m_7916_()) == null) {
                           animation = tag.m_7916_();
                           style = entry.getKey();
                           allTagsNormal = false;
                           break label42;
                        }
                     }
                  }

                  if (!allTagsNormal) {
                     this.f_96541_
                        .m_91152_(
                           new MessageScreen(
                              "Save Failed", "No animation named: " + animation + " in " + style, this, button2 -> this.f_96541_.m_91152_(this), 180, 90
                           )
                        );
                  } else {
                     this.rootTag.f_128329_.clear();

                     for (PackEntry<String, ListTag> entry : this.styles) {
                        this.rootTag.m_128365_(entry.getKey(), (Tag)entry.getValue());
                     }

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
            Component.m_237115_("datapack_edit.styles.tooltip.mandatory")
         )
      );
      this.m_142416_(this.stylesGrid);
      this.m_142416_(this.modelPreviewer);
      this.m_142416_(this.inputComponentsList);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
      this.modelPreviewer.onDestroy();
   }

   public void m_86600_() {
      this.modelPreviewer._tick();
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      if (this.modelPreviewer.m_7979_(mouseX, mouseY, button, dx, dy)) {
         return true;
      } else {
         return this.inputComponentsList.m_7979_(mouseX, mouseY, button, dx, dy) ? true : super.m_7979_(mouseX, mouseY, button, dx, dy);
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

   private void reloadAnimationPlayer() {
      List<AssetAccessor<? extends StaticAnimation>> animations = Lists.newArrayList();
      this.comboGrid.visitRows(values -> {
         AssetAccessor<? extends StaticAnimation> animation = (AssetAccessor<? extends StaticAnimation>)values.get("combo_animation");
         if (animation != null) {
            animations.add(animation);
         }
      });
      AssetAccessor<? extends StaticAnimation> dashAttack = (AssetAccessor<? extends StaticAnimation>)this.dashAttackPopupbox._getValue();
      AssetAccessor<? extends StaticAnimation> airSlash = (AssetAccessor<? extends StaticAnimation>)this.airSlashPopupbox._getValue();
      if (dashAttack != null) {
         animations.add(dashAttack);
      }

      if (airSlash != null) {
         animations.add(airSlash);
      }

      this.modelPreviewer.clearAnimations();
      animations.forEach(this.modelPreviewer::addAnimationToPlay);
   }
}
