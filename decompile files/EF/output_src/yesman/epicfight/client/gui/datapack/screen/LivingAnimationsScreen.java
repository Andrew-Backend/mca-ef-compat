package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.datastruct.ModifiablePair;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;

public class LivingAnimationsScreen extends Screen {
   private final Screen parentScreen;
   private final Grid stylesGrid;
   private Grid animationsGrid;
   private final ModelPreviewer modelPreviewer;
   private final List<PackEntry<String, List<ModifiablePair<String, String>>>> styles = Lists.newArrayList();
   private final CompoundTag rootTag;
   private static final Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> LIVING_ANIMTIONS = Maps.newHashMap();

   public LivingAnimationsScreen(Screen parentScreen, CompoundTag rootTag) {
      super(Component.m_237115_("datapack_edit.weapon_type.living_animations"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.rootTag = rootTag;
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.modelPreviewer = new ModelPreviewer(
         106, 200, 60, 49, ResizableComponent.HorizontalSizing.LEFT_RIGHT, ResizableComponent.VerticalSizing.TOP_BOTTOM, Armatures.BIPED, Meshes.BIPED
      );
      this.stylesGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(12, 60)
         .xy2(85, 50)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged(
            (rowposition, values) -> {
               Grid.PackImporter importer = new Grid.PackImporter();

               for (ModifiablePair<String, String> entryx : this.styles.get(rowposition).getValue()) {
                  importer.newRow()
                     .newValue("living_motion", LivingMotion.ENUM_MANAGER.get(entryx.getFirst()))
                     .newValue("living_animation", DatapackEditScreen.animationByKey(entryx.getSecond()));
               }

               this.animationsGrid._setActive(true);
               this.animationsGrid._setValue(importer);
            }
         )
         .addColumn(
            Grid.combo("style", Style.ENUM_MANAGER.universalValues())
               .valueChanged(event -> this.styles.get(event.rowposition).setPackKey(ParseUtil.nullParam(event.postValue).toLowerCase(Locale.ROOT)))
               .defaultVal(CapabilityItem.Styles.ONE_HAND)
         )
         .pressAdd((grid, button) -> {
            this.styles.add(PackEntry.ofValue("", Lists.newArrayList()));
            int rowposition = grid.addRow();
            grid.setGridFocus(rowposition, "style");
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> this.styles.remove(removedRow));
            if (grid.m_6702_().size() == 0) {
               this.animationsGrid._setActive(false);
            }
         })
         .build();
      this.animationsGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(177, 60)
         .xy2(14, 49)
         .horizontalSizing(ResizableComponent.HorizontalSizing.WIDTH_RIGHT)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .addColumn(
            Grid.combo(
                  "living_motion",
                  List.of(
                     LivingMotions.IDLE,
                     LivingMotions.WALK,
                     LivingMotions.RUN,
                     LivingMotions.SNEAK,
                     LivingMotions.SWIM,
                     LivingMotions.FLOAT,
                     LivingMotions.KNEEL,
                     LivingMotions.FALL,
                     LivingMotions.SIT,
                     LivingMotions.FLY,
                     LivingMotions.CREATIVE_FLY,
                     LivingMotions.CREATIVE_IDLE,
                     LivingMotions.BLOCK,
                     LivingMotions.RELOAD,
                     LivingMotions.AIM,
                     LivingMotions.SHOT
                  )
               )
               .valueChanged(event -> {
                  List<ModifiablePair<String, String>> styleAnimations = this.styles.get(this.stylesGrid.getRowposition()).getValue();
                  styleAnimations.get(event.rowposition).setFirst(ParseUtil.nullParam(event.postValue).toLowerCase(Locale.ROOT));
                  this.modelPreviewer.clearAnimations();
                  this.modelPreviewer.getAnimator().getEntityPatch().currentLivingMotion = event.postValue;
                  this.modelPreviewer.getAnimator().getEntityPatch().currentCompositeMotion = event.postValue;
                  if (LIVING_ANIMTIONS.containsKey(event.postValue)) {
                     this.modelPreviewer.addAnimationToPlay(LIVING_ANIMTIONS.get(event.postValue));
                  }

                  AssetAccessor<? extends StaticAnimation> livingAnimation = this.animationsGrid.getValue(event.rowposition, "living_animation");
                  if (livingAnimation != null) {
                     this.modelPreviewer.getAnimator().playAnimation(livingAnimation, 0.0F);
                  }
               })
               .defaultVal(LivingMotions.IDLE)
         )
         .addColumn(
            Grid.popup("living_animation", PopupBox.AnimationPopupBox::new)
               .filter(accessor -> !accessor.checkType(MainFrameAnimation.class))
               .editWidgetCreated(popupBox -> popupBox.setModel(Armatures.BIPED, Meshes.BIPED))
               .valueChanged(event -> {
                  this.modelPreviewer.clearAnimations();
                  LivingMotion livingMotion = event.grid.getValue(event.rowposition, "living_motion");
                  List<ModifiablePair<String, String>> styleAnimations = this.styles.get(this.stylesGrid.getRowposition()).getValue();
                  styleAnimations.get(event.rowposition).setSecond(ParseUtil.nullOrToString(event.postValue, animation -> animation.registryName().toString()));
                  if (LIVING_ANIMTIONS.containsKey(livingMotion)) {
                     this.modelPreviewer.addAnimationToPlay(LIVING_ANIMTIONS.get(livingMotion));
                  }

                  if (event.postValue != null) {
                     this.modelPreviewer.addAnimationToPlay(event.postValue);
                  }
               })
               .toDisplayText(animation -> animation == null ? "" : animation.registryName().toString())
               .width(150)
         )
         .pressAdd((grid, button) -> {
            this.styles.get(this.stylesGrid.getRowposition()).getValue().add(ModifiablePair.of("", ""));
            int rowposition = grid.addRow();
            grid.setGridFocus(rowposition, "living_animation");
         })
         .pressRemove((grid, button) -> {
            this.styles.get(this.stylesGrid.getRowposition()).getValue().remove(grid.getRowposition());
            grid.removeRow();
         })
         .rowpositionChanged((rowposition, values) -> {
            this.modelPreviewer.clearAnimations();
            LivingMotion livingMotion = (LivingMotion)values.get("living_motion");
            this.modelPreviewer.getAnimator().getEntityPatch().currentLivingMotion = livingMotion;
            this.modelPreviewer.getAnimator().getEntityPatch().currentCompositeMotion = livingMotion;
            if (LIVING_ANIMTIONS.containsKey(livingMotion)) {
               this.modelPreviewer.addAnimationToPlay(LIVING_ANIMTIONS.get(livingMotion));
            }

            AssetAccessor<? extends StaticAnimation> animation = (AssetAccessor<? extends StaticAnimation>)values.get("living_animation");
            if (animation != null) {
               this.modelPreviewer.getAnimator().playAnimation(animation, 0.0F);
            }
         })
         .build();
      this.animationsGrid._setActive(false);
      Grid.PackImporter packImporter = new Grid.PackImporter();

      for (Entry<String, Tag> entry : rootTag.f_128329_.entrySet()) {
         List<ModifiablePair<String, String>> list = Lists.newArrayList();

         for (Entry<String, Tag> entry$2 : ((CompoundTag)entry.getValue()).f_128329_.entrySet()) {
            list.add(ModifiablePair.of(entry$2.getKey(), entry$2.getValue().m_7916_()));
         }

         this.styles.add(PackEntry.ofValue(entry.getKey(), list));
         packImporter.newRow();
         packImporter.newValue("style", Style.ENUM_MANAGER.get(entry.getKey()));
      }

      this.stylesGrid._setValue(packImporter);
   }

   protected void m_7856_() {
      ScreenRectangle screenRectangle = this.m_264198_();
      this.stylesGrid.resize(screenRectangle);
      this.animationsGrid.resize(screenRectangle);
      this.modelPreviewer.resize(screenRectangle);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_130655_,
               button -> {
                  Set<String> styles = Sets.newHashSet();

                  for (PackEntry<String, List<ModifiablePair<String, String>>> entry : this.styles) {
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

                  label51:
                  for (PackEntry<String, List<ModifiablePair<String, String>>> entry : this.styles) {
                     for (ModifiablePair<String, String> pair : entry.getValue()) {
                        if (DatapackEditScreen.animationByKey(pair.getSecond()) == null) {
                           animation = pair.getSecond();
                           style = entry.getKey();
                           allTagsNormal = false;
                           break label51;
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

                     for (PackEntry<String, List<ModifiablePair<String, String>>> entry : this.styles) {
                        CompoundTag compTag = new CompoundTag();

                        for (ModifiablePair<String, String> pair : entry.getValue()) {
                           compTag.m_128359_(pair.getFirst(), pair.getSecond());
                        }

                        this.rootTag.m_128365_(entry.getKey(), compTag);
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
            14,
            100,
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
      this.m_142416_(
         new Static(
            this,
            this.f_96543_ - 188,
            100,
            40,
            15,
            ResizableComponent.HorizontalSizing.LEFT_WIDTH,
            null,
            "datapack_edit.weapon_type.living_animations.modifiers"
         )
      );
      this.m_142416_(this.animationsGrid);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
      this.modelPreviewer.onDestroy();
   }

   public void m_86600_() {
      this.modelPreviewer._tick();
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

   static {
      LIVING_ANIMTIONS.put(LivingMotions.IDLE, Animations.BIPED_IDLE);
      LIVING_ANIMTIONS.put(LivingMotions.WALK, Animations.BIPED_WALK);
      LIVING_ANIMTIONS.put(LivingMotions.RUN, Animations.BIPED_RUN);
      LIVING_ANIMTIONS.put(LivingMotions.SNEAK, Animations.BIPED_SNEAK);
      LIVING_ANIMTIONS.put(LivingMotions.SWIM, Animations.BIPED_SWIM);
      LIVING_ANIMTIONS.put(LivingMotions.FLOAT, Animations.BIPED_FLOAT);
      LIVING_ANIMTIONS.put(LivingMotions.KNEEL, Animations.BIPED_KNEEL);
      LIVING_ANIMTIONS.put(LivingMotions.FALL, Animations.BIPED_FALL);
      LIVING_ANIMTIONS.put(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
      LIVING_ANIMTIONS.put(LivingMotions.SIT, Animations.BIPED_SIT);
      LIVING_ANIMTIONS.put(LivingMotions.FLY, Animations.BIPED_FLYING);
      LIVING_ANIMTIONS.put(LivingMotions.DEATH, Animations.BIPED_DEATH);
      LIVING_ANIMTIONS.put(LivingMotions.JUMP, Animations.BIPED_JUMP);
      LIVING_ANIMTIONS.put(LivingMotions.CLIMB, Animations.BIPED_CLIMBING);
      LIVING_ANIMTIONS.put(LivingMotions.SLEEP, Animations.BIPED_SLEEPING);
      LIVING_ANIMTIONS.put(LivingMotions.CREATIVE_FLY, Animations.BIPED_CREATIVE_FLYING);
      LIVING_ANIMTIONS.put(LivingMotions.CREATIVE_IDLE, Animations.BIPED_CREATIVE_IDLE);
   }
}
