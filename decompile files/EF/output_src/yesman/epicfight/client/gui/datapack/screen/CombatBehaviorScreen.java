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
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.CheckBox;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.InputComponentList;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.client.gui.datapack.widgets.RowSpliter;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.main.EpicFightMod;

public class CombatBehaviorScreen extends Screen {
   private InputComponentList<CompoundTag> inputComponentsList;
   private final List<CompoundTag> movesetList = Lists.newLinkedList();
   private final Screen parentScreen;
   private final CompoundTag rootTag;
   private final ModelPreviewer modelPreviewer;
   private final boolean isHumanoidSubTag;
   private Grid movesetGrid;
   private Grid behaviorGrid;
   private Grid conditionGrid;
   private Grid parameterGrid;

   protected CombatBehaviorScreen(
      Screen parentScreen, CompoundTag rootTag, AssetAccessor<? extends Armature> armature, AssetAccessor<? extends SkinnedMesh> mesh, boolean isHumanoidSubTag
   ) {
      super(Component.m_237115_("datapack_edit.mob_patch.combat_behavior"));
      this.isHumanoidSubTag = isHumanoidSubTag;
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.rootTag = rootTag;
      this.modelPreviewer = new ModelPreviewer(
         0, 10, 35, 50, ResizableComponent.HorizontalSizing.LEFT_RIGHT, ResizableComponent.VerticalSizing.TOP_BOTTOM, armature, mesh
      );
      PopupBox.AnimationPopupBox animationPopupBox = new PopupBox.AnimationPopupBox(
         this,
         this.f_96547_,
         0,
         11,
         0,
         15,
         ResizableComponent.HorizontalSizing.LEFT_RIGHT,
         null,
         Component.m_237113_("datapack_edit.mob_patch.combat_behavior.animation"),
         pair -> {
            ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
            CompoundTag behaviorTag = behaviorListTag.m_128728_(this.behaviorGrid.getRowposition());
            behaviorTag.m_128359_("animation", (String)pair.getFirst());
            this.rearrangeAttackAnimation();
         }
      );
      animationPopupBox.applyFilter(animation -> animation.checkType(AttackAnimation.class));
      animationPopupBox.setModel(armature, mesh);
      this.movesetGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(8, 45)
         .xy2(55, 50)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
         .verticalSizing(ResizableComponent.VerticalSizing.TOP_BOTTOM)
         .rowHeight(26)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> {
            this.inputComponentsList.importTag(this.movesetList.get(rowposition));
            this.conditionGrid._setActive(false);
            this.parameterGrid._setActive(false);
            animationPopupBox._setActive(false);
            this.rearrangeAttackAnimation();
         })
         .addColumn(Grid.editbox("movement_set").editable(false).width(180))
         .pressAdd((grid, button) -> {
            grid.setValueChangeEnabled(false);
            int rowposition = grid.addRowWithDefaultValues("movement_set", String.format("moveset%d", grid.m_6702_().size() + 1));
            this.movesetList.add(rowposition, new CompoundTag());
            grid.setGridFocus(rowposition, "movement_set");
            grid.setValueChangeEnabled(true);
            this.m_7522_(grid);
            if (grid.m_6702_().size() > 0) {
               this.inputComponentsList.setComponentsActive(true);
               this.conditionGrid._setActive(false);
               this.parameterGrid._setActive(false);
               animationPopupBox._setActive(false);
            }
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> this.movesetList.remove(removedRow));
            if (this.movesetList.size() == 0) {
               this.inputComponentsList.setComponentsActive(false);
            }
         })
         .build();
      this.behaviorGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(2, 0)
         .xy2(55, 245)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_WIDTH)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> {
            ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
            CompoundTag tag = behaviorListTag.m_128728_(rowposition);
            ListTag conditions = tag.m_128437_("conditions", 10);
            String animation = tag.m_128461_("animation");
            Grid.PackImporter conditionImporter = new Grid.PackImporter();

            for (Tag conditionTag : conditions) {
               String condtionName = ((CompoundTag)conditionTag).m_128461_("predicate");
               if (!condtionName.contains(":")) {
                  condtionName = EpicFightMod.prefix(condtionName);
                  ((CompoundTag)conditionTag).m_128359_("predicate", condtionName);
               }

               conditionImporter.newRow();
               conditionImporter.newValue("condition", EpicFightConditions.getConditionOrNull(ResourceLocation.parse(condtionName)));
            }

            this.parameterGrid.reset();
            this.conditionGrid._setValue(conditionImporter);
            if (this.conditionGrid.m_6702_().size() > 0) {
               this.conditionGrid.setGridFocus(0, "condition");
            }

            animationPopupBox._setValue(DatapackEditScreen.animationByKey(animation));
            this.conditionGrid._setActive(true);
            this.parameterGrid._setActive(true);
            animationPopupBox._setActive(true);
         })
         .addColumn(Grid.editbox("behavior").editable(false).width(180))
         .pressAdd((grid, button) -> {
            grid.setValueChangeEnabled(false);
            int rowposition = grid.addRowWithDefaultValues("behavior", String.format("behavior%d", grid.m_6702_().size() + 1));
            ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
            behaviorListTag.add(rowposition, new CompoundTag());
            grid.setGridFocus(rowposition, "behavior");
            grid.setValueChangeEnabled(true);
            this.m_7522_(grid);
            if (grid.m_6702_().size() > 0) {
               this.conditionGrid._setActive(true);
               this.parameterGrid._setActive(true);
               animationPopupBox._setActive(true);
            }
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> {
               ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
               behaviorListTag.remove(removedRow);
            });
            if (grid.m_6702_().size() == 0) {
               this.conditionGrid._setActive(false);
               this.parameterGrid._setActive(false);
               animationPopupBox._setActive(false);
            }
         })
         .build();
      this.inputComponentsList = new InputComponentList<CompoundTag>(this, 0, 0, 0, 0, 28) {
         public void importTag(CompoundTag tag) {
            this.resetComponents();
            Grid.PackImporter behaviorSet = new Grid.PackImporter();

            for (int i = 0; i < tag.m_128437_("behaviors", 10).size(); i++) {
               behaviorSet.newRow();
               behaviorSet.newValue("behavior", "behavior" + (i + 1));
            }

            if (!tag.m_128441_("canBeInterrupted")) {
               tag.m_128379_("canBeInterrupted", false);
            }

            if (!tag.m_128441_("looping")) {
               tag.m_128379_("looping", false);
            }

            this.setDataBindingComponenets(
               new Object[]{
                  tag.m_128441_("weight") ? ParseUtil.valueOfOmittingType(tag.m_128459_("weight")) : "",
                  tag.m_128441_("cooldown") ? ParseUtil.valueOfOmittingType(tag.m_128451_("cooldown")) : "",
                  tag.m_128471_("canBeInterrupted"),
                  tag.m_128471_("looping"),
                  null,
                  behaviorSet
               }
            );
            this.setComponentsActive(true);
         }
      };
      this.inputComponentsList.m_93507_(164);
      ResizableEditBox weightEditBox = new ResizableEditBox(
         this.f_96547_,
         0,
         50,
         0,
         15,
         Component.m_237115_("datapack_edit.mob_patch.combat_behavior.weight"),
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         null
      );
      weightEditBox.m_94151_(value -> {
         if (StringUtil.isNullOrEmpty(value)) {
            this.movesetList.get(this.movesetGrid.getRowposition()).m_128473_("weight");
         } else {
            this.movesetList.get(this.movesetGrid.getRowposition()).m_128347_("weight", Double.parseDouble(value));
         }
      });
      weightEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
      ResizableEditBox cooldownEditBox = new ResizableEditBox(
         this.f_96547_,
         0,
         50,
         0,
         15,
         Component.m_237115_("datapack_edit.mob_patch.combat_behavior.weight"),
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         null
      );
      cooldownEditBox.m_94151_(value -> {
         if (StringUtil.isNullOrEmpty(value)) {
            this.movesetList.get(this.movesetGrid.getRowposition()).m_128473_("cooldown");
         } else {
            this.movesetList.get(this.movesetGrid.getRowposition()).m_128405_("cooldown", Integer.parseInt(value));
         }
      });
      cooldownEditBox.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Integer::parseInt));
      this.conditionGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(63, 0)
         .xy2(10, 80)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
         .transparentBackground(false)
         .rowpositionChanged((rowposition, values) -> {
            this.parameterGrid.reset();
            Supplier<Condition<?>> conditionProvider = (Supplier<Condition<?>>)values.get("condition");
            if (conditionProvider != null) {
               Condition<?> condition = conditionProvider.get();
               ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
               ListTag conditionsList = ParseUtil.getOrDefaultTag(behaviorListTag.m_128728_(this.behaviorGrid.getRowposition()), "conditions", new ListTag());
               CompoundTag comp = (CompoundTag)conditionsList.get(rowposition);
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
                  ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
                  ListTag conditionsList = ParseUtil.getOrDefaultTag(behaviorListTag.m_128728_(this.behaviorGrid.getRowposition()), "conditions", new ListTag());
                  CompoundTag comp = (CompoundTag)conditionsList.get(event.rowposition);
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
            ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
            ListTag conditionsList = ParseUtil.getOrDefaultTag(behaviorListTag.m_128728_(this.behaviorGrid.getRowposition()), "conditions", new ListTag());
            conditionsList.add(rowposition, new CompoundTag());
            grid.setGridFocus(rowposition, "weapon_category");
            grid.setValueChangeEnabled(true);
         })
         .pressRemove((grid, button) -> {
            grid.removeRow(removedRow -> {
               ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
               ListTag conditionsList = ParseUtil.getOrDefaultTag(behaviorListTag.m_128728_(this.behaviorGrid.getRowposition()), "conditions", new ListTag());
               conditionsList.remove(removedRow);
            });
            if (grid.m_6702_().size() == 0) {
               this.parameterGrid.reset();
            }
         })
         .build();
      this.parameterGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(63, 0)
         .xy2(10, 80)
         .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
         .rowHeight(21)
         .rowEditable(Grid.GridBuilder.RowEditButton.NONE)
         .transparentBackground(false)
         .addColumn(Grid.wildcard("parameter_key").editable(false).toDisplayText(editor -> editor.editWidget.m_6035_().getString()).width(100))
         .addColumn(Grid.wildcard("parameter_value").editWidgetProvider(row -> {
            Condition.ParameterEditor editor = row.getValue("parameter_key");
            return editor.editWidget;
         }).toDisplayText(ParseUtil::snakeToSpacedCamel).editable(true).valueChanged(event -> {
            ListTag behaviorListTag = ParseUtil.getOrDefaultTag(this.movesetList.get(this.movesetGrid.getRowposition()), "behaviors", new ListTag());
            ListTag conditionsList = ParseUtil.getOrDefaultTag(behaviorListTag.m_128728_(this.behaviorGrid.getRowposition()), "conditions", new ListTag());
            CompoundTag conditionTag = conditionsList.m_128728_(this.conditionGrid.getRowposition());
            Condition.ParameterEditor editor = event.grid.getValue(event.rowposition, "parameter_key");
            if (StringUtil.isNullOrEmpty(ParseUtil.nullParam(event.postValue))) {
               conditionTag.m_128473_(editor.editWidget.m_6035_().getString());
            } else {
               conditionTag.m_128365_(editor.editWidget.m_6035_().getString(), editor.toTag.apply(event.postValue));
            }
         }).width(150))
         .build();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.combat_behavior.weight"
            )
         );
      this.inputComponentsList.addComponentCurrentRow(weightEditBox.relocateX(parentScreen.m_264198_(), this.inputComponentsList.nextStart(5)));
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.combat_behavior.cooldown"
            )
         );
      this.inputComponentsList.addComponentCurrentRow(cooldownEditBox.relocateX(parentScreen.m_264198_(), this.inputComponentsList.nextStart(5)));
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.combat_behavior.interceptable"
            )
         );
      this.inputComponentsList
         .addComponentCurrentRow(
            new CheckBox(
               this.f_96547_,
               this.inputComponentsList.nextStart(5),
               60,
               0,
               10,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               null,
               Component.m_237113_(""),
               value -> this.movesetList.get(this.movesetGrid.getRowposition()).m_128379_("canBeInterrupted", value)
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.combat_behavior.loop"
            )
         );
      this.inputComponentsList
         .addComponentCurrentRow(
            new CheckBox(
               this.f_96547_,
               this.inputComponentsList.nextStart(5),
               60,
               0,
               10,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               null,
               Component.m_237113_(""),
               value -> this.movesetList.get(this.movesetGrid.getRowposition()).m_128379_("looping", value)
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(new RowSpliter(this.inputComponentsList.nextStart(0), 10, 60, 15, ResizableComponent.HorizontalSizing.LEFT_RIGHT, null));
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(63),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.combat_behavior.conditions"
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.addComponentCurrentRow(this.conditionGrid.relocateX(parentScreen.m_264198_(), this.inputComponentsList.nextStart(60)));
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.addComponentCurrentRow(this.behaviorGrid);
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(7),
               100,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.combat_behavior.parameters"
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.addComponentCurrentRow(this.parameterGrid.relocateX(parentScreen.m_264198_(), this.inputComponentsList.nextStart(60)));
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(63),
               55,
               0,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.mob_patch.combat_behavior.animation"
            )
         );
      this.inputComponentsList.addComponentCurrentRow(animationPopupBox.relocateX(parentScreen.m_264198_(), this.inputComponentsList.nextStart(5)));
      if (this.rootTag.m_128441_("combat_behavior") || this.isHumanoidSubTag && this.rootTag.m_128441_("behavior_series")) {
         Grid.PackImporter packImporter = new Grid.PackImporter();
         ListTag list = this.rootTag.m_128437_(this.isHumanoidSubTag ? "behavior_series" : "combat_behavior", 10);

         for (int i = 0; i < list.size(); i++) {
            this.movesetList.add(list.m_128728_(i));
            packImporter.newRow();
            packImporter.newValue("movement_set", "movement set" + (i + 1));
         }

         this.movesetGrid._setValue(packImporter);
      }

      this.inputComponentsList.setComponentsActive(false);
   }

   protected void m_7856_() {
      ScreenRectangle screenRectangle = this.m_264198_();
      this.movesetGrid.resize(screenRectangle);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_130655_,
               button -> {
                  ListTag newListTag = new ListTag();
                  int idx = 0;

                  for (CompoundTag tag : this.movesetList) {
                     try {
                        this.validateTagSave(tag);
                        newListTag.add(tag);
                        idx++;
                     } catch (Exception e) {
                        this.f_96541_
                           .m_91152_(
                              new MessageScreen(
                                    "Save Failed",
                                    "Failed to save " + this.movesetGrid.getValue(idx, "movement_set") + ": " + e.getMessage(),
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

                  if (this.isHumanoidSubTag) {
                     this.rootTag.m_128473_("behavior_series");
                     this.rootTag.m_128365_("behavior_series", newListTag);
                  } else {
                     this.rootTag.m_128473_("combat_behavior");
                     this.rootTag.m_128365_("combat_behavior", newListTag);
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
      int splitPos = (int)(this.f_96543_ * 0.6F);
      this.inputComponentsList.m_93437_(splitPos - 70, screenRectangle.f_263800_(), screenRectangle.m_274449_() + 35, screenRectangle.m_274349_() - 50);
      this.inputComponentsList.m_93507_(70);
      this.modelPreviewer.setX1(splitPos + 6);
      this.modelPreviewer.resize(screenRectangle);
      this.m_142416_(this.movesetGrid);
      this.m_142416_(this.inputComponentsList);
      this.m_142416_(this.modelPreviewer);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
      this.modelPreviewer.onDestroy();
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

   public void m_86600_() {
      this.modelPreviewer._tick();
   }

   private void rearrangeAttackAnimation() {
      this.modelPreviewer.clearAnimations();
      CompoundTag tag = this.movesetList.get(this.movesetGrid.getRowposition());

      for (Tag behaviorTag : tag.m_128437_("behaviors", 10)) {
         CompoundTag behaviorCompound = (CompoundTag)behaviorTag;
         AssetAccessor<? extends StaticAnimation> animation = DatapackEditScreen.animationByKey(behaviorCompound.m_128461_("animation"));
         if (animation != null) {
            this.modelPreviewer.addAnimationToPlay(animation);
         }
      }
   }

   private void validateTagSave(CompoundTag tag) throws IllegalStateException {
      if (!tag.m_128441_("weight")) {
         throw new IllegalStateException("Define a weight value");
      }

      if (tag.m_128451_("cooldown") < 0) {
         throw new IllegalStateException("Cooldown can't be a negative value");
      }

      if (!tag.m_128441_("canBeInterrupted")) {
         throw new IllegalStateException("Define interceptability");
      }

      if (!tag.m_128441_("looping")) {
         throw new IllegalStateException("Define looping");
      }

      if (tag.m_128441_("behaviors") && tag.m_128437_("behaviors", 10).size() != 0) {
         int idx = 1;

         for (Tag behaviorTag : tag.m_128437_("behaviors", 10)) {
            CompoundTag behaviorCompound = (CompoundTag)behaviorTag;
            if (behaviorCompound.m_128441_("animation") && !StringUtil.isNullOrEmpty(behaviorCompound.m_128461_("animation"))) {
               if (behaviorCompound.m_128441_("conditions") && behaviorCompound.m_128437_("conditions", 10).size() != 0) {
                  for (Tag conditionTag : behaviorCompound.m_128437_("conditions", 10)) {
                     CompoundTag conditionCompound = (CompoundTag)conditionTag;
                     if (!conditionCompound.m_128441_("predicate") || StringUtil.isNullOrEmpty(conditionCompound.m_128461_("predicate"))) {
                        throw new IllegalStateException("Behavior" + idx + ": Condition not specified");
                     }

                     try {
                        Supplier<Condition<?>> condition = EpicFightConditions.getConditionOrThrow(
                           ResourceLocation.parse(conditionCompound.m_128461_("predicate"))
                        );
                        condition.get().read(conditionCompound);
                     } catch (Exception e) {
                        throw new IllegalStateException("Behavior" + idx + ": " + e.getMessage());
                     }
                  }

                  idx++;
                  continue;
               }

               throw new IllegalStateException("Behavior" + idx + ": Define at least one condition");
            }

            throw new IllegalStateException("Behavior" + idx + ": No animation defined.");
         }
      } else {
         throw new IllegalStateException("Define at least one behavior");
      }
   }
}
