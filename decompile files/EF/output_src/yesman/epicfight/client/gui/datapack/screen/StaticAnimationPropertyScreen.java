package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.datapack.EditorAnimation;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.JointMask;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.InputComponentList;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.Static;

public class StaticAnimationPropertyScreen extends Screen {
   private final InputComponentList<JsonObject> inputComponentsList;
   private final ComboBox<StaticAnimationPropertyScreen.LayerOptions> layerTypeCombo;
   private final Consumer<StaticAnimationPropertyScreen.LayerOptions> layerTypeResponder;
   private final Screen parentScreen;
   private final EditorAnimation animation;
   private Layer.Priority baseLayerPriority;
   private Layer.Priority compositeLayerPriority;
   private List<PackEntry<LivingMotion, JointMask.JointMaskSet>> baseLayerMasks = Lists.newArrayList();
   private List<PackEntry<LivingMotion, JointMask.JointMaskSet>> compositeLayerMasks = Lists.newArrayList();

   protected StaticAnimationPropertyScreen(Screen parentScreen, EditorAnimation animation) {
      super(Component.m_237115_("datapack_edit.import_animation.client_data"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.animation = animation;
      this.inputComponentsList = new InputComponentList<JsonObject>(this, 0, 0, 0, 0, 30) {
         public void importTag(JsonObject tag) {
            this.clearComponents();
            this.setComponentsActive(true);
            StaticAnimationPropertyScreen.LayerOptions layerOption = null;
            if (tag.has("layer")) {
               layerOption = StaticAnimationPropertyScreen.LayerOptions.valueOf(GsonHelper.m_13906_(tag, "layer"));
            } else if (tag.has("multilayer")) {
               layerOption = StaticAnimationPropertyScreen.LayerOptions.MULTILAYER;
            }

            StaticAnimationPropertyScreen.this.rearrangeComponents(layerOption);
            Object[] data;
            if (layerOption == StaticAnimationPropertyScreen.LayerOptions.BASE_LAYER) {
               StaticAnimationPropertyScreen.this.baseLayerPriority = Layer.Priority.valueOf(GsonHelper.m_13906_(tag, "priority"));
               data = new Object[]{layerOption, StaticAnimationPropertyScreen.this.baseLayerPriority};
            } else if (layerOption == StaticAnimationPropertyScreen.LayerOptions.COMPOSITE_LAYER) {
               StaticAnimationPropertyScreen.this.compositeLayerPriority = Layer.Priority.valueOf(GsonHelper.m_13906_(tag, "priority"));
               Grid.PackImporter packImporter = new Grid.PackImporter();

               for (JsonElement maskTag : tag.getAsJsonArray("masks")) {
                  JsonObject maskCompoundTag = maskTag.getAsJsonObject();
                  LivingMotion livingMotion = LivingMotion.ENUM_MANAGER.get(GsonHelper.m_13906_(maskCompoundTag, "livingmotion"));
                  JointMask.JointMaskSet jointMask = JointMaskReloadListener.getJointMaskEntry(GsonHelper.m_13906_(maskCompoundTag, "type"));
                  packImporter.newRow();
                  packImporter.newValue("living_motion", livingMotion);
                  packImporter.newValue("joint_mask", jointMask);
                  StaticAnimationPropertyScreen.this.compositeLayerMasks.add(PackEntry.ofValue(livingMotion, jointMask));
               }

               data = new Object[]{layerOption, StaticAnimationPropertyScreen.this.compositeLayerPriority, packImporter};
            } else if (layerOption == StaticAnimationPropertyScreen.LayerOptions.MULTILAYER) {
               JsonObject base = tag.getAsJsonObject("multilayer").getAsJsonObject("base");
               JsonObject composite = tag.getAsJsonObject("multilayer").getAsJsonObject("composite");
               StaticAnimationPropertyScreen.this.baseLayerPriority = Layer.Priority.valueOf(GsonHelper.m_13906_(base, "priority"));
               Grid.PackImporter basePackImporter = new Grid.PackImporter();

               for (JsonElement maskTag : base.getAsJsonArray("masks")) {
                  JsonObject maskCompoundTag = maskTag.getAsJsonObject();
                  LivingMotion livingMotion = LivingMotion.ENUM_MANAGER.get(GsonHelper.m_13906_(maskCompoundTag, "livingmotion"));
                  JointMask.JointMaskSet jointMask = JointMaskReloadListener.getJointMaskEntry(GsonHelper.m_13906_(maskCompoundTag, "type"));
                  basePackImporter.newRow();
                  basePackImporter.newValue("living_motion", livingMotion);
                  basePackImporter.newValue("joint_mask", jointMask);
                  StaticAnimationPropertyScreen.this.baseLayerMasks.add(PackEntry.ofValue(livingMotion, jointMask));
               }

               StaticAnimationPropertyScreen.this.compositeLayerPriority = Layer.Priority.valueOf(GsonHelper.m_13906_(composite, "priority"));
               Grid.PackImporter compositePackImporter = new Grid.PackImporter();

               for (JsonElement maskTag : composite.getAsJsonArray("masks")) {
                  JsonObject maskCompoundTag = maskTag.getAsJsonObject();
                  LivingMotion livingMotion = LivingMotion.ENUM_MANAGER.get(GsonHelper.m_13906_(maskCompoundTag, "livingmotion"));
                  JointMask.JointMaskSet jointMask = JointMaskReloadListener.getJointMaskEntry(GsonHelper.m_13906_(maskCompoundTag, "type"));
                  compositePackImporter.newRow();
                  compositePackImporter.newValue("living_motion", livingMotion);
                  compositePackImporter.newValue("joint_mask", jointMask);
                  StaticAnimationPropertyScreen.this.compositeLayerMasks.add(PackEntry.ofValue(livingMotion, jointMask));
               }

               data = new Object[]{
                  layerOption,
                  StaticAnimationPropertyScreen.this.baseLayerPriority,
                  basePackImporter,
                  StaticAnimationPropertyScreen.this.compositeLayerPriority,
                  compositePackImporter
               };
            } else {
               data = new Object[]{null};
            }

            StaticAnimationPropertyScreen.this.layerTypeCombo._setResponder(null);
            this.setDataBindingComponenets(data);
            StaticAnimationPropertyScreen.this.layerTypeCombo._setResponder(StaticAnimationPropertyScreen.this.layerTypeResponder);
         }
      };
      this.layerTypeResponder = layerType -> this.rearrangeComponents(layerType);
      this.layerTypeCombo = new ComboBox<>(
         this,
         this.f_96547_,
         0,
         124,
         100,
         15,
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         null,
         8,
         Component.m_237115_("datapack_edit.import_animation.client_data.layer_type"),
         List.of(StaticAnimationPropertyScreen.LayerOptions.values()),
         ParseUtil::snakeToSpacedCamel,
         this.layerTypeResponder
      );
      this.inputComponentsList.importTag(animation.getPropertiesJson());
   }

   protected void rearrangeComponents(StaticAnimationPropertyScreen.LayerOptions layerType) {
      ScreenRectangle screenRect = this.m_264198_();
      this.inputComponentsList.clearComponents();
      this.baseLayerPriority = null;
      this.compositeLayerPriority = null;
      this.baseLayerMasks.clear();
      this.compositeLayerMasks.clear();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               90,
               60,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.import_animation.client_data.layer_type"
            )
         );
      this.inputComponentsList.addComponentCurrentRow(this.layerTypeCombo.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
      if (layerType == StaticAnimationPropertyScreen.LayerOptions.BASE_LAYER || layerType == StaticAnimationPropertyScreen.LayerOptions.COMPOSITE_LAYER) {
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  this,
                  this.inputComponentsList.nextStart(4),
                  90,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.import_animation.client_data.priority"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new ComboBox<>(
                  this,
                  this.f_96547_,
                  this.inputComponentsList.nextStart(5),
                  124,
                  100,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  8,
                  Component.m_237115_("datapack_edit.import_animation.client_data.layer_type"),
                  List.of(Layer.Priority.values()),
                  ParseUtil::snakeToSpacedCamel,
                  priority -> {
                     if (layerType == StaticAnimationPropertyScreen.LayerOptions.BASE_LAYER) {
                        this.baseLayerPriority = priority;
                     } else {
                        this.compositeLayerPriority = priority;
                     }
                  }
               )
            );
         if (layerType == StaticAnimationPropertyScreen.LayerOptions.COMPOSITE_LAYER) {
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  new Static(
                     this,
                     this.inputComponentsList.nextStart(4),
                     90,
                     60,
                     15,
                     ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                     null,
                     "datapack_edit.import_animation.client_data.mask"
                  )
               );
            this.inputComponentsList.newRow();
            this.inputComponentsList.newRow();
            this.inputComponentsList
               .addComponentCurrentRow(
                  Grid.builder(this, this.parentScreen.getMinecraft())
                     .xy1(5, 120)
                     .xy2(16, 80)
                     .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
                     .rowHeight(21)
                     .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
                     .transparentBackground(false)
                     .addColumn(
                        Grid.combo("living_motion", LivingMotion.ENUM_MANAGER.universalValues())
                           .valueChanged(event -> this.compositeLayerMasks.get(event.rowposition).setPackKey(event.postValue))
                           .defaultVal(LivingMotions.IDLE)
                     )
                     .addColumn(
                        Grid.popup("joint_mask", PopupBox.JointMaskPopupBox::new)
                           .valueChanged(event -> this.compositeLayerMasks.get(event.rowposition).setValue(event.postValue))
                           .toDisplayText(jointMask -> ParseUtil.nullOrToString(jointMask, m -> JointMaskReloadListener.getKey(m).toString()))
                           .width(150)
                     )
                     .pressAdd((grid, button) -> {
                        this.compositeLayerMasks.add(PackEntry.ofValue(LivingMotions.IDLE, null));
                        int rowposition = grid.addRow();
                        grid.setGridFocus(rowposition, "style");
                     })
                     .pressRemove((grid, button) -> grid.removeRow(removedRow -> this.compositeLayerMasks.remove(removedRow)))
                     .build()
               );
            this.inputComponentsList.newRow();
         }
      } else if (layerType == StaticAnimationPropertyScreen.LayerOptions.MULTILAYER) {
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  this,
                  this.inputComponentsList.nextStart(12),
                  90,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.import_animation.client_data.base_layer"
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  this,
                  this.inputComponentsList.nextStart(26),
                  90,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.import_animation.client_data.priority"
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new ComboBox<>(
                  this,
                  this.f_96547_,
                  this.inputComponentsList.nextStart(5),
                  124,
                  100,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  8,
                  Component.m_237115_("datapack_edit.import_animation.client_data.layer_type"),
                  List.of(Layer.Priority.values()),
                  ParseUtil::snakeToSpacedCamel,
                  priority -> this.baseLayerPriority = priority
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  this,
                  this.inputComponentsList.nextStart(26),
                  90,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.import_animation.client_data.mask"
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               Grid.builder(this, this.parentScreen.getMinecraft())
                  .xy1(26, 120)
                  .xy2(16, 80)
                  .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
                  .rowHeight(21)
                  .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
                  .transparentBackground(false)
                  .addColumn(
                     Grid.combo("living_motion", LivingMotion.ENUM_MANAGER.universalValues())
                        .valueChanged(event -> this.baseLayerMasks.get(event.rowposition).setPackKey(event.postValue))
                        .defaultVal(LivingMotions.IDLE)
                  )
                  .addColumn(
                     Grid.popup("joint_mask", PopupBox.JointMaskPopupBox::new)
                        .valueChanged(event -> this.baseLayerMasks.get(event.rowposition).setValue(event.postValue))
                        .toDisplayText(jointMask -> ParseUtil.nullOrToString(jointMask, m -> JointMaskReloadListener.getKey(m).toString()))
                        .width(150)
                  )
                  .pressAdd((grid, button) -> {
                     this.baseLayerMasks.add(PackEntry.ofValue(LivingMotions.IDLE, null));
                     int rowposition = grid.addRow();
                     grid.setGridFocus(rowposition, "style");
                  })
                  .pressRemove((grid, button) -> grid.removeRow(removedRow -> this.baseLayerMasks.remove(removedRow)))
                  .build()
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  this,
                  this.inputComponentsList.nextStart(12),
                  90,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  Component.m_237115_("datapack_edit.import_animation.client_data.composite_layer")
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  this,
                  this.inputComponentsList.nextStart(26),
                  90,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  Component.m_237115_("datapack_edit.import_animation.client_data.priority")
               )
            );
         this.inputComponentsList
            .addComponentCurrentRow(
               new ComboBox<>(
                  this,
                  this.f_96547_,
                  this.inputComponentsList.nextStart(5),
                  124,
                  100,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  8,
                  Component.m_237115_("datapack_edit.import_animation.client_data.layer_type"),
                  List.of(Layer.Priority.values()),
                  ParseUtil::snakeToSpacedCamel,
                  priority -> this.compositeLayerPriority = priority
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  this,
                  this.inputComponentsList.nextStart(26),
                  90,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  "datapack_edit.import_animation.client_data.mask"
               )
            );
         this.inputComponentsList.newRow();
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               Grid.builder(this, this.parentScreen.getMinecraft())
                  .xy1(26, 120)
                  .xy2(16, 80)
                  .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
                  .rowHeight(21)
                  .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
                  .transparentBackground(false)
                  .addColumn(
                     Grid.combo("living_motion", LivingMotion.ENUM_MANAGER.universalValues())
                        .valueChanged(event -> this.compositeLayerMasks.get(event.rowposition).setPackKey(event.postValue))
                        .defaultVal(LivingMotions.IDLE)
                  )
                  .addColumn(
                     Grid.popup("joint_mask", PopupBox.JointMaskPopupBox::new)
                        .valueChanged(event -> this.compositeLayerMasks.get(event.rowposition).setValue(event.postValue))
                        .toDisplayText(jointMask -> ParseUtil.nullOrToString(jointMask, m -> JointMaskReloadListener.getKey(m).toString()))
                        .width(150)
                  )
                  .pressAdd((grid, button) -> {
                     this.compositeLayerMasks.add(PackEntry.ofValue(LivingMotions.IDLE, null));
                     int rowposition = grid.addRow();
                     grid.setGridFocus(rowposition, "style");
                  })
                  .pressRemove((grid, button) -> grid.removeRow(removedRow -> this.compositeLayerMasks.remove(removedRow)))
                  .build()
            );
         this.inputComponentsList.newRow();
      }

      this.inputComponentsList.m_93437_(screenRect.f_263770_() - 15, screenRect.f_263800_() - 68, screenRect.m_274449_() + 32, screenRect.m_274349_() - 48);
      this.inputComponentsList.m_93507_(15);
   }

   protected void m_7856_() {
      ScreenRectangle screenRect = this.m_264198_();
      this.inputComponentsList.m_93437_(screenRect.f_263770_() - 15, screenRect.f_263800_() - 68, screenRect.m_274449_() + 32, screenRect.m_274349_() - 48);
      this.inputComponentsList.m_93507_(15);
      this.m_142416_(this.inputComponentsList);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_286989_,
               button -> {
                  try {
                     this.save();
                     this.m_7379_();
                  } catch (IllegalStateException e) {
                     this.f_96541_
                        .m_91152_(
                           new MessageScreen("Failed to save", e.getMessage(), this, button3 -> this.f_96541_.m_91152_(this), 300, 70).autoCalculateHeight()
                        );
                  }
               }
            )
            .m_252794_(this.f_96543_ / 2 - 162, this.f_96544_ - 28)
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
            .m_252794_(this.f_96543_ / 2 + 2, this.f_96544_ - 28)
            .m_253046_(160, 21)
            .m_253136_()
      );
   }

   public void save() throws IllegalStateException {
      this.animation.getPropertiesJson().asMap().clear();
      StaticAnimationPropertyScreen.LayerOptions layerOption = this.layerTypeCombo._getValue();
      if (layerOption == null) {
         throw new IllegalStateException("Layer type is not defined!");
      }

      if (layerOption == StaticAnimationPropertyScreen.LayerOptions.MULTILAYER) {
         if (this.baseLayerPriority == null) {
            throw new IllegalStateException("Base layer priority is not defined!");
         }

         if (this.compositeLayerPriority == null) {
            throw new IllegalStateException("Composite layer priority is not defined!");
         }

         JsonObject multilayer = new JsonObject();
         JsonObject base = new JsonObject();
         JsonObject composite = new JsonObject();
         base.addProperty("priority", this.baseLayerPriority.toString());
         composite.addProperty("priority", this.compositeLayerPriority.toString());
         JsonArray baseMasks = new JsonArray();
         int idx = 0;

         for (PackEntry<LivingMotion, JointMask.JointMaskSet> entry : this.baseLayerMasks) {
            idx++;
            JsonObject jointEntryTag = new JsonObject();
            if (entry.getKey() == null) {
               throw new IllegalStateException(String.format("Row %s: Living motion is not defined!", idx));
            }

            if (entry.getValue() == null) {
               throw new IllegalStateException(String.format("Row %s: Joint mask is not defined!", idx));
            }

            jointEntryTag.addProperty("livingmotion", entry.getKey().toString());
            jointEntryTag.addProperty("type", JointMaskReloadListener.getKey(entry.getValue()).toString());
            baseMasks.add(jointEntryTag);
         }

         JsonArray compositeMasks = new JsonArray();
         idx = 0;

         for (PackEntry<LivingMotion, JointMask.JointMaskSet> entry : this.compositeLayerMasks) {
            idx++;
            JsonObject jointEntryTag = new JsonObject();
            if (entry.getKey() == null) {
               throw new IllegalStateException(String.format("Row %s: Living motion is not defined!", idx));
            }

            if (entry.getValue() == null) {
               throw new IllegalStateException(String.format("Row %s: Joint mask is not defined!", idx));
            }

            jointEntryTag.addProperty("livingmotion", entry.getKey().toString());
            jointEntryTag.addProperty("type", JointMaskReloadListener.getKey(entry.getValue()).toString());
            compositeMasks.add(jointEntryTag);
         }

         base.add("masks", baseMasks);
         composite.add("masks", compositeMasks);
         multilayer.add("base", base);
         multilayer.add("composite", composite);
         this.animation.getPropertiesJson().add("multilayer", multilayer);
      } else {
         if (layerOption == StaticAnimationPropertyScreen.LayerOptions.BASE_LAYER && this.baseLayerPriority == null) {
            throw new IllegalStateException("Base layer priority is not defined!");
         }

         if (layerOption == StaticAnimationPropertyScreen.LayerOptions.COMPOSITE_LAYER && this.compositeLayerPriority == null) {
            throw new IllegalStateException("Composite layer priority is not defined!");
         }

         this.animation.getPropertiesJson().addProperty("layer", layerOption.toString());
         this.animation
            .getPropertiesJson()
            .addProperty(
               "priority",
               layerOption == StaticAnimationPropertyScreen.LayerOptions.BASE_LAYER
                  ? this.baseLayerPriority.toString()
                  : this.compositeLayerPriority.toString()
            );
         JsonArray masks = new JsonArray();
         List<PackEntry<LivingMotion, JointMask.JointMaskSet>> list = layerOption == StaticAnimationPropertyScreen.LayerOptions.BASE_LAYER
            ? this.baseLayerMasks
            : this.compositeLayerMasks;
         int idx = 0;

         for (PackEntry<LivingMotion, JointMask.JointMaskSet> entry : list) {
            idx++;
            JsonObject jointEntryTag = new JsonObject();
            if (entry.getKey() == null) {
               throw new IllegalStateException(String.format("Row %s: Living motion is not defined!", idx));
            }

            if (entry.getValue() == null) {
               throw new IllegalStateException(String.format("Row %s: Joint mask is not defined!", idx));
            }

            jointEntryTag.addProperty("livingmotion", entry.getKey().toString());
            jointEntryTag.addProperty("type", JointMaskReloadListener.getKey(entry.getValue()).toString());
            masks.add(jointEntryTag);
         }

         this.animation.getPropertiesJson().add("masks", masks);
      }
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      return this.inputComponentsList.m_7979_(mouseX, mouseY, button, dx, dy) ? true : super.m_7979_(mouseX, mouseY, button, dx, dy);
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280039_(guiGraphics);
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
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   public enum LayerOptions {
      BASE_LAYER,
      COMPOSITE_LAYER,
      MULTILAYER;
   }
}
