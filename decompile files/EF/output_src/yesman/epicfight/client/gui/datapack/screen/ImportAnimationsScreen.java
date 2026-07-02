package yesman.epicfight.client.gui.datapack.screen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.util.internal.StringUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.animation.types.datapack.DatapackAnimation;
import yesman.epicfight.api.animation.types.datapack.EditorAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.CheckBox;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.InputComponentList;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.client.gui.datapack.widgets.RowSpliter;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.client.gui.datapack.widgets.SubScreenOpenButton;
import yesman.epicfight.gameasset.ColliderPreset;

public class ImportAnimationsScreen extends Screen {
   private final SelectAnimationScreen parentScreen;
   private final Grid animationGrid;
   private final ModelPreviewer modelPreviewer;
   private final List<EditorAnimation> fakeAnimations = Lists.newArrayList();
   private InputComponentList<EditorAnimation> inputComponentsList;
   private ComboBox<EditorAnimation.AnimationType> animationType;
   private Consumer<EditorAnimation.AnimationType> responder;
   private Map<ResourceLocation, PackEntry<EditorAnimation, DatapackAnimation<? extends StaticAnimation>>> userAnimations;

   public ImportAnimationsScreen(SelectAnimationScreen parentScreen, AssetAccessor<? extends Armature> armature, AssetAccessor<? extends SkinnedMesh> mesh) {
      super(Component.m_237113_("register_animation_screen"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.userAnimations = DatapackEditScreen.getCurrentScreen().getUserAniamtions();
      this.fakeAnimations.addAll(this.userAnimations.values().stream().map(PackEntry::getKey).map(EditorAnimation::deepCopy).toList());
      this.modelPreviewer = new ModelPreviewer(10, 15, 0, 140, ResizableComponent.HorizontalSizing.LEFT_RIGHT, null, armature, mesh);
      this.modelPreviewer.setCollider(ColliderPreset.FIST);
      ScreenRectangle screenRect = parentScreen.m_264198_();
      int split = screenRect.f_263770_() / 2 - 60;
      this.animationGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(8, screenRect.m_274449_() + 14)
         .xy2(split - 10, screenRect.f_263800_() - 21)
         .rowHeight(26)
         .rowEditable(Grid.GridBuilder.RowEditButton.NONE)
         .transparentBackground(true)
         .rowpositionChanged(
            (rowposition, values) -> {
               this.inputComponentsList.importTag(this.fakeAnimations.get(rowposition));
               this.modelPreviewer.setTrailInfo();
               EditorAnimation.AnimationType animationType = this.fakeAnimations.get(rowposition).getAnimationClass();
               if (animationType == EditorAnimation.AnimationType.ATTACK
                  || animationType == EditorAnimation.AnimationType.BASIC_ATTACK
                  || animationType == EditorAnimation.AnimationType.DASH_ATTACK
                  || animationType == EditorAnimation.AnimationType.AIR_SLASH) {
                  this.inputComponentsList.<ResizableComponent>getComponent(7, 1)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(8, 1)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(9, 1)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(10, 1)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(11, 1)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(12, 1)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(13, 1)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(14, 2)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(14, 4)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(14, 6)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(15, 2)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(15, 4)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(15, 6)._setActive(false);
                  this.inputComponentsList.<ResizableComponent>getComponent(16, 1)._setActive(false);
                  if (this.fakeAnimations.get(rowposition).getPropertiesJson().has("trail_effects")) {
                     JsonArray trailArr = this.fakeAnimations.get(rowposition).getPropertiesJson().getAsJsonArray("trail_effects");
                     TrailInfo[] trailInfos = new TrailInfo[trailArr.size()];
                     int i = 0;

                     for (JsonElement element : trailArr) {
                        JsonObject trailObj = element.getAsJsonObject();
                        TrailInfo.Builder builder = TrailInfo.builder()
                           .time(trailObj.get("start_time").getAsFloat(), trailObj.get("end_time").getAsFloat())
                           .joint(trailObj.get("joint").getAsString())
                           .itemSkinHand(InteractionHand.valueOf(trailObj.get("item_skin_hand").getAsString().toUpperCase(Locale.ROOT)));
                        if (trailObj.has("lifetime")) {
                           builder.lifetime(trailObj.get("lifetime").getAsInt());
                        }

                        if (trailObj.has("interpolations")) {
                           builder.interpolations(trailObj.get("interpolations").getAsInt());
                        }

                        trailInfos[i] = TrailInfo.PREVIEWER_DEFAULT_TRAIL.overwrite(builder.create());
                        i++;
                     }

                     this.modelPreviewer.setTrailInfo(trailInfos);
                  }
               }
            }
         )
         .addColumn(
            Grid.editbox("animation_name")
               .editWidgetCreated(editbox -> editbox.m_94153_(ResourceLocation::m_135830_))
               .editable(true)
               .valueChanged(event -> this.fakeAnimations.get(event.rowposition).setParameter("path", event.postValue))
               .width(180)
         )
         .build();
      this.inputComponentsList = new InputComponentList<EditorAnimation>(this, 0, 0, 0, 0, 30) {
         public void importTag(EditorAnimation fakeAnim) {
            ImportAnimationsScreen.this.rearrangeComponents(fakeAnim.getAnimationClass());
            this.setComponentsActive(true);
            if (fakeAnim.getAnimationClass() != null) {
               switch (fakeAnim.getAnimationClass()) {
                  case STATIC:
                  case MOVEMENT:
                     ImportAnimationsScreen.this.animationType._setResponder(null);
                     this.setDataBindingComponenets(
                        new Object[]{
                           fakeAnim.getAnimationClass(), ParseUtil.nullParam(fakeAnim.getParameter("convertTime")), fakeAnim.getParameter("isRepeat")
                        }
                     );
                     ImportAnimationsScreen.this.animationType._setResponder(ImportAnimationsScreen.this.responder);
                     break;
                  case LONG_HIT:
                  case SHORT_HIT:
                     ImportAnimationsScreen.this.animationType._setResponder(null);
                     this.setDataBindingComponenets(new Object[]{fakeAnim.getAnimationClass(), ParseUtil.nullParam(fakeAnim.getParameter("convertTime"))});
                     ImportAnimationsScreen.this.animationType._setResponder(ImportAnimationsScreen.this.responder);
                     break;
                  case ATTACK:
                  case BASIC_ATTACK:
                  case DASH_ATTACK:
                  case AIR_SLASH:
                     CompoundTag colliderTag = new CompoundTag();
                     Collider collider = fakeAnim.getParameter("collider");
                     if (collider != null) {
                        collider.serialize(colliderTag);
                     }

                     ImportAnimationsScreen.this.animationType._setResponder(null);
                     Grid.PackImporter packImporter = new Grid.PackImporter();
                     ListTag phasesTag = fakeAnim.getParameter("phases");

                     for (int i = 0; i < phasesTag.size(); i++) {
                        packImporter.newRow();
                        packImporter.newValue("phase", String.format("Phase%s", i));
                     }

                     this.setDataBindingComponenets(
                        new Object[]{
                           fakeAnim.getAnimationClass(),
                           ParseUtil.nullParam(fakeAnim.getParameter("convertTime")),
                           packImporter,
                           ParseUtil.nullParam(fakeAnim.getParameter("antic")),
                           ParseUtil.nullParam(fakeAnim.getParameter("preDelay")),
                           ParseUtil.nullParam(fakeAnim.getParameter("contact")),
                           ParseUtil.nullParam(fakeAnim.getParameter("recovery")),
                           fakeAnim.getParameter("hand"),
                           null,
                           ParseUtil.nullParam(colliderTag.m_128423_("number")),
                           colliderTag.m_128441_("center")
                              ? ParseUtil.nullParam(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(0), Tag::m_7916_))
                              : "",
                           colliderTag.m_128441_("center")
                              ? ParseUtil.nullParam(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(1), Tag::m_7916_))
                              : "",
                           colliderTag.m_128441_("center")
                              ? ParseUtil.nullParam(ParseUtil.nullOrToString(colliderTag.m_128437_("center", 6).get(2), Tag::m_7916_))
                              : "",
                           colliderTag.m_128441_("size")
                              ? ParseUtil.nullParam(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(0), Tag::m_7916_))
                              : "",
                           colliderTag.m_128441_("size")
                              ? ParseUtil.nullParam(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(1), Tag::m_7916_))
                              : "",
                           colliderTag.m_128441_("size")
                              ? ParseUtil.nullParam(ParseUtil.nullOrToString(colliderTag.m_128437_("size", 6).get(2), Tag::m_7916_))
                              : "",
                           fakeAnim.getParameter("colliderJoint")
                        }
                     );
                     ImportAnimationsScreen.this.animationType._setResponder(ImportAnimationsScreen.this.responder);
                     if (fakeAnim.getPropertiesJson().has("trail_effects")) {
                        JsonArray trailList = fakeAnim.getPropertiesJson().get("trail_effects").getAsJsonArray();
                        TrailInfo[] trailArr = new TrailInfo[trailList.size()];
                        int i = 0;

                        for (JsonElement element : trailList) {
                           JsonObject trailObj = element.getAsJsonObject();
                           TrailInfo.Builder builder = TrailInfo.builder()
                              .time(trailObj.get("start_time").getAsFloat(), trailObj.get("end_time").getAsFloat())
                              .joint(trailObj.get("joint").getAsString())
                              .itemSkinHand(InteractionHand.valueOf(trailObj.get("item_skin_hand").getAsString().toUpperCase(Locale.ROOT)));
                           if (trailObj.has("lifetime")) {
                              builder.lifetime(trailObj.get("lifetime").getAsInt());
                           }

                           if (trailObj.has("interpolations")) {
                              builder.interpolations(trailObj.get("interpolations").getAsInt());
                           }

                           trailArr[i] = TrailInfo.PREVIEWER_DEFAULT_TRAIL.overwrite(builder.create());
                           i++;
                        }

                        ImportAnimationsScreen.this.modelPreviewer.setTrailInfo(trailArr);
                     }
                     break;
                  default:
                     ImportAnimationsScreen.this.animationType._setValue(null);
               }
            } else {
               ImportAnimationsScreen.this.animationType._setResponder(null);
               ImportAnimationsScreen.this.animationType._setValue(null);
               ImportAnimationsScreen.this.animationType._setResponder(ImportAnimationsScreen.this.responder);
            }
         }
      };
      if (this.fakeAnimations.isEmpty()) {
         this.inputComponentsList.newRow();
         this.inputComponentsList
            .addComponentCurrentRow(
               new Static(
                  this,
                  this.inputComponentsList.nextStart(4),
                  5,
                  60,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                  null,
                  "datapack_edit.import_animation.place_tooltip"
               )
            );
      }

      this.responder = clz -> {
         if (clz != null) {
            EditorAnimation fakeAnim = this.fakeAnimations.get(this.animationGrid.getRowposition());
            fakeAnim.setAnimationClass(clz);
            this.rearrangeComponents(clz);
         }
      };
      this.animationType = new ComboBox<>(
         this,
         this.f_96547_,
         0,
         124,
         100,
         15,
         ResizableComponent.HorizontalSizing.LEFT_WIDTH,
         null,
         8,
         Component.m_237115_("datapack_edit.import_animation.type"),
         List.of(EditorAnimation.AnimationType.values()),
         type -> type.toString(),
         this.responder
      );

      for (PackEntry<EditorAnimation, DatapackAnimation<? extends StaticAnimation>> entry : this.userAnimations.values()) {
         this.animationGrid.addRowWithDefaultValues("animation_name", entry.getKey().getParameter("path"));
      }
   }

   public void rearrangeComponents(EditorAnimation.AnimationType animationClass) {
      ScreenRectangle screenRect = this.m_264198_();
      this.modelPreviewer.setCollider(null);
      this.modelPreviewer.setColliderJoint(null);
      this.modelPreviewer.clearAnimations();
      this.modelPreviewer.addAnimationToPlay(this.fakeAnimations.get(this.animationGrid.getRowposition()));
      this.inputComponentsList.clearComponents();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               85,
               60,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               "datapack_edit.import_animation.type"
            )
         );
      this.inputComponentsList.addComponentCurrentRow(this.animationType.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
      if (animationClass != null) {
         switch (animationClass) {
            case STATIC:
            case MOVEMENT: {
               ResizableEditBox convertTime = new ResizableEditBox(
                  this.f_96547_,
                  0,
                  35,
                  0,
                  15,
                  Component.m_237115_("datapack_edit.import_animation.convert_time"),
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null
               );
               Boolean isRepeat = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("isRepeat");
               if (isRepeat == null) {
                  isRepeat = false;
               }

               CheckBox repeat = new CheckBox(
                  this.f_96547_,
                  0,
                  60,
                  0,
                  10,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  isRepeat,
                  Component.m_237113_(""),
                  value -> this.fakeAnimations.get(this.animationGrid.getRowposition()).setParameter("isRepeat", value)
               );
               convertTime.m_94151_(input -> {
                  Object f = StringUtil.isNullOrEmpty(input) ? null : Float.valueOf(input);
                  this.fakeAnimations.get(this.animationGrid.getRowposition()).setParameter("convertTime", f);
               });
               convertTime.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.convert_time"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(convertTime.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.repeat"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(repeat.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.client_data"
                     )
                  );
               this.inputComponentsList
                  .addComponentCurrentRow(
                     SubScreenOpenButton.builder()
                        .subScreen(() -> new StaticAnimationPropertyScreen(this, this.fakeAnimations.get(this.animationGrid.getRowposition())))
                        .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                        .build()
                  );
               break;
            }
            case LONG_HIT:
            case SHORT_HIT:
            case KNOCK_DOWN: {
               ResizableEditBox convertTime = new ResizableEditBox(
                  this.f_96547_,
                  0,
                  35,
                  0,
                  15,
                  Component.m_237115_("datapack_edit.import_animation.convert_time"),
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null
               );
               convertTime.m_94151_(input -> {
                  Object f = StringUtil.isNullOrEmpty(input) ? null : Float.valueOf(input);
                  this.fakeAnimations.get(this.animationGrid.getRowposition()).setParameter("convertTime", f);
               });
               convertTime.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.convert_time"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(convertTime.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               break;
            }
            case ATTACK:
            case BASIC_ATTACK:
            case DASH_ATTACK:
            case AIR_SLASH: {
               this.modelPreviewer.setCollider(ColliderPreset.FIST);
               ResizableEditBox convertTime = new ResizableEditBox(
                  this.f_96547_,
                  0,
                  35,
                  0,
                  15,
                  Component.m_237115_("datapack_edit.import_animation.convert_time"),
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null
               );
               convertTime.m_94151_(input -> {
                  Object f = StringUtil.isNullOrEmpty(input) ? null : Float.valueOf(input);
                  this.fakeAnimations.get(this.animationGrid.getRowposition()).setParameter("convertTime", f);
               });
               convertTime.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
               ResizableEditBox antic = new ResizableEditBox(
                  this.f_96547_,
                  0,
                  35,
                  0,
                  15,
                  Component.m_237115_("datapack_edit.import_animation.antic"),
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null
               );
               ResizableEditBox preDelay = new ResizableEditBox(
                  this.f_96547_,
                  0,
                  35,
                  0,
                  15,
                  Component.m_237115_("datapack_edit.import_animation.preDelay"),
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null
               );
               ResizableEditBox contact = new ResizableEditBox(
                  this.f_96547_,
                  0,
                  35,
                  0,
                  15,
                  Component.m_237115_("datapack_edit.import_animation.contact"),
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null
               );
               ResizableEditBox recovery = new ResizableEditBox(
                  this.f_96547_,
                  0,
                  35,
                  0,
                  15,
                  Component.m_237115_("datapack_edit.import_animation.recovery"),
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null
               );
               ResizableEditBox colliderCount = new ResizableEditBox(
                  this.f_96547_, 0, 40, 0, 15, Component.m_237115_("datapack_edit.collider.count"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
               );
               ResizableEditBox colliderCenterX = new ResizableEditBox(
                  this.f_96547_, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.x"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
               );
               ResizableEditBox colliderCenterY = new ResizableEditBox(
                  this.f_96547_, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.y"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
               );
               ResizableEditBox colliderCenterZ = new ResizableEditBox(
                  this.f_96547_, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.center.z"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
               );
               ResizableEditBox colliderSizeX = new ResizableEditBox(
                  this.f_96547_, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.x"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
               );
               ResizableEditBox colliderSizeY = new ResizableEditBox(
                  this.f_96547_, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.y"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
               );
               ResizableEditBox colliderSizeZ = new ResizableEditBox(
                  this.f_96547_, 0, 35, 0, 15, Component.m_237115_("datapack_edit.collider.size.z"), ResizableComponent.HorizontalSizing.LEFT_WIDTH, null
               );
               ComboBox<Joint> colliderJoint = new ComboBox<>(
                  this,
                  this.f_96547_,
                  0,
                  124,
                  100,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  8,
                  Component.m_237115_("datapack_edit.import_animation.joint"),
                  this.modelPreviewer.getArmature().get().rootJoint.getAllJoints(),
                  Joint::getName,
                  null
               );
               ComboBox<InteractionHand> interactionHand = new ComboBox<>(
                  this,
                  this.f_96547_,
                  0,
                  124,
                  100,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                  null,
                  8,
                  Component.m_237115_("datapack_edit.import_animation.hand"),
                  List.of(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND),
                  ParseUtil::snakeToSpacedCamel,
                  null
               );
               PopupBox.ColliderPopupBox colliderPopup = new PopupBox.ColliderPopupBox(
                  this,
                  this.f_96547_,
                  0,
                  30,
                  130,
                  15,
                  ResizableComponent.HorizontalSizing.LEFT_RIGHT,
                  null,
                  Component.m_237115_("datapack_edit.collider"),
                  null
               );
               antic._setActive(false);
               preDelay._setActive(false);
               contact._setActive(false);
               recovery._setActive(false);
               colliderCount._setActive(false);
               colliderCenterX._setActive(false);
               colliderCenterY._setActive(false);
               colliderCenterZ._setActive(false);
               colliderSizeX._setActive(false);
               colliderSizeY._setActive(false);
               colliderSizeZ._setActive(false);
               colliderJoint._setActive(false);
               interactionHand._setActive(false);
               colliderPopup._setActive(false);
               Grid phasesGrid = Grid.builder(this, this.f_96541_)
                  .xy1(8, 0)
                  .xy2(12, 80)
                  .horizontalSizing(ResizableComponent.HorizontalSizing.LEFT_RIGHT)
                  .rowHeight(26)
                  .rowEditable(Grid.GridBuilder.RowEditButton.ADD_REMOVE)
                  .transparentBackground(false)
                  .rowpositionChanged((rowposition, values) -> {
                     EditorAnimation fakeAnimation = this.fakeAnimations.get(this.animationGrid.getRowposition());
                     ListTag phases = fakeAnimation.getParameter("phases");
                     CompoundTag tag = phases.m_128728_(rowposition);
                     antic.m_94144_(tag.m_128441_("antic") ? ParseUtil.valueOfOmittingType(tag.m_128423_("antic").m_7916_()) : "");
                     preDelay.m_94144_(tag.m_128441_("preDelay") ? ParseUtil.valueOfOmittingType(tag.m_128423_("preDelay").m_7916_()) : "");
                     contact.m_94144_(tag.m_128441_("contact") ? ParseUtil.valueOfOmittingType(tag.m_128423_("contact").m_7916_()) : "");
                     recovery.m_94144_(tag.m_128441_("recovery") ? ParseUtil.valueOfOmittingType(tag.m_128423_("recovery").m_7916_()) : "");
                     if (tag.m_128441_("joint")) {
                        String armature$joint = tag.m_128461_("joint");
                        String joinName = armature$joint.substring(armature$joint.lastIndexOf(46) + 1);
                        colliderJoint._setValue(this.modelPreviewer.getArmature().get().searchJointByName(joinName));
                     } else {
                        colliderJoint._setValue(null);
                     }

                     interactionHand._setValue(tag.m_128441_("hand") ? InteractionHand.valueOf(tag.m_128461_("hand")) : null);
                     if (tag.m_128441_("collider")) {
                        CompoundTag colliderTag = tag.m_128469_("collider");
                        colliderCount.m_94144_(ParseUtil.valueOfOmittingType(colliderTag.m_128423_("number")));
                        colliderCenterX.m_94144_(ParseUtil.valueOfOmittingType(colliderTag.m_128437_("center", 6).get(0)));
                        colliderCenterY.m_94144_(ParseUtil.valueOfOmittingType(colliderTag.m_128437_("center", 6).get(1)));
                        colliderCenterZ.m_94144_(ParseUtil.valueOfOmittingType(colliderTag.m_128437_("center", 6).get(2)));
                        colliderSizeX.m_94144_(ParseUtil.valueOfOmittingType(colliderTag.m_128437_("size", 6).get(0)));
                        colliderSizeY.m_94144_(ParseUtil.valueOfOmittingType(colliderTag.m_128437_("size", 6).get(1)));
                        colliderSizeZ.m_94144_(ParseUtil.valueOfOmittingType(colliderTag.m_128437_("size", 6).get(2)));
                     } else {
                        colliderPopup._setValue(null);
                        colliderCount.m_94144_("");
                        colliderCenterX.m_94144_("");
                        colliderCenterY.m_94144_("");
                        colliderCenterZ.m_94144_("");
                        colliderSizeX.m_94144_("");
                        colliderSizeY.m_94144_("");
                        colliderSizeZ.m_94144_("");
                     }

                     if (rowposition > -1) {
                        antic._setActive(true);
                        preDelay._setActive(true);
                        contact._setActive(true);
                        recovery._setActive(true);
                        colliderCount._setActive(true);
                        colliderCenterX._setActive(true);
                        colliderCenterY._setActive(true);
                        colliderCenterZ._setActive(true);
                        colliderSizeX._setActive(true);
                        colliderSizeY._setActive(true);
                        colliderSizeZ._setActive(true);
                        colliderJoint._setActive(true);
                        interactionHand._setActive(true);
                        colliderPopup._setActive(true);
                     }
                  })
                  .addColumn(Grid.editbox("phase").editable(false).width(200))
                  .pressAdd((grid, button) -> {
                     EditorAnimation fakeAnimation = this.fakeAnimations.get(this.animationGrid.getRowposition());
                     ListTag phases = fakeAnimation.getParameter("phases");
                     if (phases.isEmpty()) {
                        antic._setActive(true);
                        preDelay._setActive(true);
                        contact._setActive(true);
                        recovery._setActive(true);
                        colliderCount._setActive(true);
                        colliderCenterX._setActive(true);
                        colliderCenterY._setActive(true);
                        colliderCenterZ._setActive(true);
                        colliderSizeX._setActive(true);
                        colliderSizeY._setActive(true);
                        colliderSizeZ._setActive(true);
                        colliderJoint._setActive(true);
                        interactionHand._setActive(true);
                        colliderPopup._setActive(true);
                     }

                     phases.add(new CompoundTag());
                     int rowposition = grid.addRowWithDefaultValues("phase", String.format("Phase%d", grid.m_6702_().size()));
                     grid.setGridFocus(rowposition, "phase");
                  })
                  .pressRemove((grid, button) -> grid.removeRow(removedRow -> {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");

                     for (int i = 0; i < grid.m_6702_().size(); i++) {
                        grid.setValue(i, "phase", "Phase" + i);
                     }

                     phases.remove(removedRow);
                     if (phases.isEmpty()) {
                        antic._setActive(false);
                        preDelay._setActive(false);
                        contact._setActive(false);
                        recovery._setActive(false);
                        colliderCount._setActive(false);
                        colliderCenterX._setActive(false);
                        colliderCenterY._setActive(false);
                        colliderCenterZ._setActive(false);
                        colliderSizeX._setActive(false);
                        colliderSizeY._setActive(false);
                        colliderSizeZ._setActive(false);
                        colliderJoint._setActive(false);
                        interactionHand._setActive(false);
                        colliderPopup._setActive(false);
                     }
                  }))
                  .build();
               antic.m_94151_(input -> {
                  if (!StringUtil.isNullOrEmpty(input)) {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");
                     CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());
                     phaseTag.m_128350_("antic", Float.valueOf(input));
                  }
               });
               preDelay.m_94151_(input -> {
                  if (!StringUtil.isNullOrEmpty(input)) {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");
                     CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());
                     float f = Float.valueOf(input);
                     phaseTag.m_128350_("preDelay", f);
                     this.modelPreviewer.setAttackTimeBegin(f);
                  }
               });
               contact.m_94151_(input -> {
                  if (!StringUtil.isNullOrEmpty(input)) {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");
                     CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());
                     float f = Float.valueOf(input);
                     phaseTag.m_128350_("contact", f);
                     this.modelPreviewer.setAttackTimeEnd(f);
                  }
               });
               recovery.m_94151_(input -> {
                  if (!StringUtil.isNullOrEmpty(input)) {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");
                     CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());
                     phaseTag.m_128350_("recovery", Float.valueOf(input));
                  }
               });
               antic.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
               preDelay.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
               contact.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
               recovery.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
               Runnable setCollider = () -> {
                  CompoundTag tag = new CompoundTag();
                  if (ParseUtil.isParsable(colliderCount.m_94155_(), Integer::parseInt)) {
                     tag.m_128405_("number", Integer.parseInt(colliderCount.m_94155_()));
                  }

                  ListTag center = new ListTag();
                  if (ParseUtil.isParsable(colliderCenterX.m_94155_(), Double::parseDouble)) {
                     center.add(DoubleTag.m_128500_(Double.parseDouble(colliderCenterX.m_94155_())));
                  }

                  if (ParseUtil.isParsable(colliderCenterY.m_94155_(), Double::parseDouble)) {
                     center.add(DoubleTag.m_128500_(Double.parseDouble(colliderCenterY.m_94155_())));
                  }

                  if (ParseUtil.isParsable(colliderCenterZ.m_94155_(), Double::parseDouble)) {
                     center.add(DoubleTag.m_128500_(Double.parseDouble(colliderCenterZ.m_94155_())));
                  }

                  tag.m_128365_("center", center);
                  ListTag size = new ListTag();
                  if (ParseUtil.isParsable(colliderSizeX.m_94155_(), Double::parseDouble)) {
                     size.add(DoubleTag.m_128500_(Double.parseDouble(colliderSizeX.m_94155_())));
                  }

                  if (ParseUtil.isParsable(colliderSizeY.m_94155_(), Double::parseDouble)) {
                     size.add(DoubleTag.m_128500_(Double.parseDouble(colliderSizeY.m_94155_())));
                  }

                  if (ParseUtil.isParsable(colliderSizeZ.m_94155_(), Double::parseDouble)) {
                     size.add(DoubleTag.m_128500_(Double.parseDouble(colliderSizeZ.m_94155_())));
                  }

                  tag.m_128365_("size", size);
                  EditorAnimation fakeAnimation = this.fakeAnimations.get(this.animationGrid.getRowposition());
                  ListTag phases = fakeAnimation.getParameter("phases");
                  CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());

                  try {
                     Collider collider = ColliderPreset.deserializeSimpleCollider(tag);
                     phaseTag.m_128365_("collider", tag);
                     this.modelPreviewer.setCollider(collider);
                  } catch (Exception e) {
                     phaseTag.m_128473_("collider");
                     this.modelPreviewer.setCollider(ColliderPreset.FIST);
                  }
               };
               colliderCount.m_94151_(input -> setCollider.run());
               colliderCenterX.m_94151_(input -> setCollider.run());
               colliderCenterY.m_94151_(input -> setCollider.run());
               colliderCenterZ.m_94151_(input -> setCollider.run());
               colliderSizeX.m_94151_(input -> setCollider.run());
               colliderSizeY.m_94151_(input -> setCollider.run());
               colliderSizeZ.m_94151_(input -> setCollider.run());
               colliderCount.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Integer::parseInt));
               colliderCenterX.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
               colliderCenterY.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
               colliderCenterZ.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsableAllowingMinus(context, Double::parseDouble));
               colliderSizeX.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
               colliderSizeY.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
               colliderSizeZ.m_94153_(context -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
               colliderJoint._setResponder(joint -> {
                  if (joint != null) {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");
                     CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());
                     phaseTag.m_128359_("joint", this.modelPreviewer.getArmature().toString() + "." + joint.getName());
                     this.modelPreviewer.setColliderJoint(joint);
                  }
               });
               interactionHand._setResponder(hand -> {
                  if (hand != null) {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");
                     CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());
                     phaseTag.m_128359_("hand", hand.toString());
                  }
               });
               colliderPopup._setResponder(pair -> {
                  if (pair.getSecond() != null) {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");
                     CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());
                     CompoundTag colliderTag = new CompoundTag();
                     ((Collider)pair.getSecond()).serialize(colliderTag);
                     colliderCount.m_94144_(String.valueOf(colliderTag.m_128451_("number")));
                     ListTag centerVec = colliderTag.m_128437_("center", 6);
                     colliderCenterX.m_94144_(String.valueOf(centerVec.m_128772_(0)));
                     colliderCenterY.m_94144_(String.valueOf(centerVec.m_128772_(1)));
                     colliderCenterZ.m_94144_(String.valueOf(centerVec.m_128772_(2)));
                     ListTag sizeVec = colliderTag.m_128437_("size", 6);
                     colliderSizeX.m_94144_(String.valueOf(sizeVec.m_128772_(0)));
                     colliderSizeY.m_94144_(String.valueOf(sizeVec.m_128772_(1)));
                     colliderSizeZ.m_94144_(String.valueOf(sizeVec.m_128772_(2)));
                     phaseTag.m_128365_("collider", colliderTag);
                     this.modelPreviewer.setCollider((Collider)pair.getSecond(), colliderJoint._getValue());
                  } else {
                     ListTag phases = this.fakeAnimations.get(this.animationGrid.getRowposition()).getParameter("phases");
                     CompoundTag phaseTag = phases.m_128728_(phasesGrid.getRowposition());
                     if (phaseTag.m_128441_("collider")) {
                        colliderCount.m_94144_("");
                        colliderCenterX.m_94144_("");
                        colliderCenterY.m_94144_("");
                        colliderCenterZ.m_94144_("");
                        colliderSizeX.m_94144_("");
                        colliderSizeY.m_94144_("");
                        colliderSizeZ.m_94144_("");
                     }

                     this.modelPreviewer.setCollider(null);
                  }
               });
               colliderPopup.applyFilter(collider -> collider instanceof OBBCollider || collider instanceof MultiOBBCollider);
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.convert_time"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(convertTime.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new RowSpliter(this.inputComponentsList.nextStart(5), 10, 60, 15, ResizableComponent.HorizontalSizing.LEFT_RIGHT, null)
                  );
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.phases"
                     )
                  );
               this.inputComponentsList.newRow();
               this.inputComponentsList.newRow();
               this.inputComponentsList.addComponentCurrentRow(phasesGrid);
               this.inputComponentsList.newRow();
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.antic"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(antic.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.pre_delay"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(preDelay.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.contact"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(contact.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.recovery"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(recovery.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.hand"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(interactionHand.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.collider"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderPopup.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(20),
                        40,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.collider.count"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderCount.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(20),
                        40,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        Component.m_237115_("datapack_edit.collider.center")
                     )
                  );
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(5),
                        8,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        Component.m_237113_("X: ")
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderCenterX.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(8),
                        8,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        Component.m_237113_("Y: ")
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderCenterY.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(8),
                        8,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        Component.m_237113_("Z: ")
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderCenterZ.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(20),
                        40,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        Component.m_237115_("datapack_edit.collider.size")
                     )
                  );
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(5),
                        8,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        Component.m_237113_("X: ")
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderSizeX.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(8),
                        8,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        Component.m_237113_("Y: ")
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderSizeY.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(8),
                        8,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        Component.m_237113_("Z: ")
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderSizeZ.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.joint"
                     )
                  );
               this.inputComponentsList.addComponentCurrentRow(colliderJoint.relocateX(screenRect, this.inputComponentsList.nextStart(5)));
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new RowSpliter(this.inputComponentsList.nextStart(5), 10, 60, 15, ResizableComponent.HorizontalSizing.LEFT_RIGHT, null)
                  );
               this.inputComponentsList.newRow();
               this.inputComponentsList
                  .addComponentCurrentRow(
                     new Static(
                        this,
                        this.inputComponentsList.nextStart(4),
                        85,
                        60,
                        15,
                        ResizableComponent.HorizontalSizing.LEFT_WIDTH,
                        null,
                        "datapack_edit.import_animation.client_data"
                     )
                  );
               this.inputComponentsList
                  .addComponentCurrentRow(
                     SubScreenOpenButton.builder()
                        .subScreen(
                           () -> new AttackAnimationPropertyScreen(
                              this,
                              this.fakeAnimations.get(this.animationGrid.getRowposition()),
                              this.modelPreviewer.getArmature().get().rootJoint.getAllJoints(),
                              this.modelPreviewer
                           )
                        )
                        .bounds(this.inputComponentsList.nextStart(4), 0, 15, 15)
                        .build()
                  );
            }
         }
      }

      this.inputComponentsList.newRow();
      this.inputComponentsList
         .addComponentCurrentRow(
            new Static(
               this,
               this.inputComponentsList.nextStart(4),
               60,
               60,
               15,
               ResizableComponent.HorizontalSizing.LEFT_WIDTH,
               null,
               Component.m_237115_("datapack_edit.import_animation.preview")
            )
         );
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      int split = screenRect.f_263770_() / 2 - 60;
      this.inputComponentsList.addComponentCurrentRow(this.modelPreviewer);
      this.inputComponentsList.newRow();
      this.inputComponentsList.newRow();
      this.inputComponentsList
         .m_93437_(screenRect.f_263770_() - (split + 8), screenRect.f_263800_() - 21, screenRect.m_274449_() + 14, screenRect.m_274349_() - 36);
      this.inputComponentsList.m_93507_(split + 2);
   }

   protected void m_7856_() {
      ScreenRectangle screenRect = this.m_264198_();
      int split = screenRect.f_263770_() / 2 - 60;
      this.animationGrid.m_93437_(split - 10, screenRect.f_263800_() - 21, screenRect.m_274449_() + 14, screenRect.m_274349_() - 36);
      this.animationGrid.m_93507_(8);
      this.animationGrid.resize(screenRect);
      this.inputComponentsList
         .m_93437_(screenRect.f_263770_() - (split + 8), screenRect.f_263800_() - 21, screenRect.m_274449_() + 14, screenRect.m_274349_() - 36);
      this.inputComponentsList.m_93507_(split + 2);
      this.m_142416_(this.animationGrid);
      this.m_142416_(this.inputComponentsList);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_286989_,
               button -> {
                  String errorMessage = this.createRealAnimations(this.fakeAnimations);
                  if (errorMessage != null) {
                     this.f_96541_
                        .m_91152_(
                           new MessageScreen("Failed to import the animations", errorMessage, this, button2 -> this.f_96541_.m_91152_(this), 300, 70)
                              .autoCalculateHeight()
                        );
                  } else {
                     this.m_7379_();
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

   public void m_86600_() {
      this.animationGrid._tick();
      this.inputComponentsList.tick();
   }

   public void m_7379_() {
      this.parentScreen.refreshAnimationList();
      this.f_96541_.m_91152_(this.parentScreen);
      this.modelPreviewer.onDestroy();
   }

   public void m_7400_(List<Path> paths) {
      this.f_96541_
         .m_91152_(
            new MessageScreen<>(
               "",
               "Enter the mod id",
               this,
               modid -> {
                  this.animationGrid.setValueChangeEnabled(false);

                  for (Path path : paths) {
                     InputStream stream = null;

                     try {
                        File file = path.toFile();
                        stream = new FileInputStream(file);
                        JsonAssetLoader jsonLoader = new JsonAssetLoader(stream, ResourceLocation.fromNamespaceAndPath(modid, file.getName()));
                        String armatureName = this.modelPreviewer.getArmature().get().toString();
                        armatureName = armatureName.substring(armatureName.indexOf(":") + 1);
                        String animationPath = modid
                           + ":"
                           + armatureName.substring(armatureName.lastIndexOf("/") + 1)
                           + "/"
                           + file.getName().replace(".json", "");
                        EditorAnimation animation = new EditorAnimation(
                           animationPath,
                           this.modelPreviewer.getArmature(),
                           jsonLoader.loadAnimationClip(this.modelPreviewer.getArmature().get()),
                           jsonLoader.getRootJson().getAsJsonArray("animation")
                        );
                        this.fakeAnimations.add(animation);
                        this.animationGrid.addRowWithDefaultValues("animation_name", animationPath);
                     } catch (Exception e) {
                        e.printStackTrace();
                     } finally {
                        try {
                           stream.close();
                        } catch (IOException var18) {
                        }
                     }
                  }

                  this.animationGrid.setValueChangeEnabled(true);
                  this.f_96541_.m_91152_(this);
               },
               button -> this.f_96541_.m_91152_(this),
               new ResizableEditBox(this.f_96541_.f_91062_, 0, 0, 0, 16, Component.m_237113_("datapack_edit.import_animation.input"), null, null),
               120,
               80
            )
         );
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      if (this.modelPreviewer.m_7979_(mouseX, mouseY, button, dx, dy)) {
         return true;
      } else {
         return this.inputComponentsList.m_7979_(mouseX, mouseY, button, dx, dy) ? true : super.m_7979_(mouseX, mouseY, button, dx, dy);
      }
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280039_(guiGraphics);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   private String createRealAnimations(List<EditorAnimation> fakeAnimations) {
      this.userAnimations.clear();
      StringBuilder sb = new StringBuilder();
      boolean hasException = false;
      List<Object> uniquepaths = fakeAnimations.stream().map(fakeAnim -> fakeAnim.getParameter("path")).distinct().toList();
      if (uniquepaths.size() != fakeAnimations.size()) {
         hasException = true;
         sb.append("Duplicated animation path.");
      } else {
         for (EditorAnimation fakeAnimation : fakeAnimations) {
            try {
               DatapackAnimation<? extends StaticAnimation> result = fakeAnimation.createAnimation();
               this.userAnimations.put(fakeAnimation.getRegistryName(), PackEntry.ofValue(fakeAnimation, result));
            } catch (Throwable e) {
               hasException = true;
               sb.append(String.format("%s : %s\n", fakeAnimation.getParameter("path"), e.getMessage()));
               e.printStackTrace();
            }
         }
      }

      return hasException ? sb.toString() : null;
   }
}
