package yesman.epicfight.client.gui.datapack.screen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.asset.SelfAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.gui.datapack.widgets.Grid;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.client.gui.datapack.widgets.Static;

public class ImportModelScreen extends Screen {
   private final SelectModelScreen parentScreen;
   private final Grid meshGrid;
   private final Grid armatureGrid;
   private final ModelPreviewer modelPreviewer;
   private List<PackEntry<String, AssetAccessor<? extends SkinnedMesh>>> userMeshes;
   private List<PackEntry<String, AssetAccessor<? extends Armature>>> userArmatures;

   public ImportModelScreen(SelectModelScreen parentScreen) {
      super(Component.m_237113_("register_model_screen"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      Stream<PackEntry<String, AssetAccessor<? extends SkinnedMesh>>> meshesStream = DatapackEditScreen.getCurrentScreen()
         .getUserMeshes()
         .entrySet()
         .stream()
         .map(entryx -> PackEntry.ofValue(((ResourceLocation)entryx.getKey()).toString(), (AssetAccessor<? extends SkinnedMesh>)entryx.getValue()));
      this.userMeshes = new ArrayList<>(meshesStream.toList());
      Stream<PackEntry<String, AssetAccessor<? extends Armature>>> armaturesStream = DatapackEditScreen.getCurrentScreen()
         .getUserArmatures()
         .entrySet()
         .stream()
         .map(entryx -> PackEntry.ofValue(((ResourceLocation)entryx.getKey()).toString(), (AssetAccessor<? extends Armature>)entryx.getValue()));
      this.userArmatures = new ArrayList<>(armaturesStream.toList());
      this.modelPreviewer = new ModelPreviewer(
         0, 10, 30, 30, ResizableComponent.HorizontalSizing.LEFT_RIGHT, ResizableComponent.VerticalSizing.TOP_BOTTOM, null, null
      );
      ScreenRectangle screenRect = parentScreen.m_264198_();
      int split = screenRect.f_263770_() / 2 - 60;
      this.meshGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(8, screenRect.m_274449_() + 14)
         .xy2(split - 10, screenRect.f_263800_() - 21)
         .rowHeight(26)
         .rowEditable(Grid.GridBuilder.RowEditButton.REMOVE)
         .transparentBackground(true)
         .rowpositionChanged((rowposition, values) -> this.modelPreviewer.setMesh(this.userMeshes.get(rowposition).getValue()))
         .addColumn(
            Grid.editbox("mesh_name")
               .editWidgetCreated(editbox -> editbox.m_94153_(ResourceLocation::m_135830_))
               .valueChanged(event -> this.userMeshes.get(event.rowposition).setPackKey(event.postValue))
               .editable(true)
               .width(180)
         )
         .pressRemove((grid, button) -> grid.removeRow(rowposition -> this.userMeshes.remove(rowposition)))
         .build();
      this.armatureGrid = Grid.builder(this, parentScreen.getMinecraft())
         .xy1(8, screenRect.m_274449_() + 14)
         .xy2(split - 10, screenRect.f_263800_() - 21)
         .rowHeight(26)
         .rowEditable(Grid.GridBuilder.RowEditButton.REMOVE)
         .transparentBackground(true)
         .addColumn(
            Grid.editbox("armature_name")
               .editWidgetCreated(editbox -> editbox.m_94153_(ResourceLocation::m_135830_))
               .valueChanged(event -> this.userArmatures.get(event.rowposition).setPackKey(event.postValue))
               .editable(true)
               .width(180)
         )
         .pressRemove((grid, button) -> grid.removeRow(rowposition -> this.userArmatures.remove(rowposition)))
         .build();

      for (PackEntry<String, AssetAccessor<? extends SkinnedMesh>> entry : this.userMeshes) {
         this.meshGrid.addRowWithDefaultValues("mesh_name", entry.getKey());
      }

      for (PackEntry<String, AssetAccessor<? extends Armature>> entry : this.userArmatures) {
         this.armatureGrid.addRowWithDefaultValues("armature_name", entry.getKey());
      }
   }

   protected void m_7856_() {
      ScreenRectangle screenRect = this.m_264198_();
      int widthSplit = screenRect.f_263770_() / 2 - 20;
      int heightSplit = screenRect.f_263800_() / 2;
      this.meshGrid.m_93437_(widthSplit - 10, heightSplit - 20, screenRect.m_274449_() + 30, heightSplit - 10);
      this.meshGrid.m_93507_(10);
      this.meshGrid.resize(screenRect);
      this.armatureGrid.m_93437_(widthSplit - 10, heightSplit - 18, heightSplit + 8, screenRect.m_274349_() - 30);
      this.armatureGrid.m_93507_(10);
      this.armatureGrid.resize(screenRect);
      this.m_142416_(new Static(this, 10, 100, 14, 15, null, null, Component.m_237115_("datapack_edit.import_model.meshes"), Component.m_237113_("")));
      this.m_142416_(this.meshGrid);
      this.m_142416_(
         new Static(this, 10, 100, heightSplit - 8, 15, null, null, Component.m_237115_("datapack_edit.import_model.armatures"), Component.m_237113_(""))
      );
      this.m_142416_(this.armatureGrid);
      this.modelPreviewer.setX1(widthSplit + 10);
      this.modelPreviewer.resize(screenRect);
      this.m_142416_(this.modelPreviewer);
      this.m_142416_(Button.m_253074_(CommonComponents.f_286989_, button -> {
         Map<ResourceLocation, AssetAccessor<? extends SkinnedMesh>> userMeshes = DatapackEditScreen.getCurrentScreen().getUserMeshes();
         Map<ResourceLocation, AssetAccessor<? extends Armature>> userArmatures = DatapackEditScreen.getCurrentScreen().getUserArmatures();
         userMeshes.clear();
         userArmatures.clear();
         this.userMeshes.forEach(packEntry -> userMeshes.put(ResourceLocation.parse(packEntry.getKey()), packEntry.getValue()));
         this.userArmatures.forEach(packEntry -> userArmatures.put(ResourceLocation.parse(packEntry.getKey()), packEntry.getValue()));
         this.m_7379_();
      }).m_252794_(this.f_96543_ / 2 - 162, this.f_96544_ - 26).m_253046_(160, 21).m_253136_());
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
            .m_252794_(this.f_96543_ / 2 + 2, this.f_96544_ - 26)
            .m_253046_(160, 21)
            .m_253136_()
      );
   }

   public void m_86600_() {
      this.meshGrid._tick();
      this.armatureGrid._tick();
   }

   public void m_7379_() {
      this.parentScreen.refreshModelList();
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
                  this.meshGrid.setValueChangeEnabled(false);
                  this.armatureGrid.setValueChangeEnabled(false);

                  for (Path path : paths) {
                     InputStream stream = null;

                     try {
                        File file = path.toFile();
                        stream = new FileInputStream(file);
                        String modelPath = modid + ":" + file.getName().replace(".json", "");
                        ResourceLocation modelId = ResourceLocation.parse(modelPath);
                        JsonAssetLoader jsonLoader = new JsonAssetLoader(stream, modelId);
                        SkinnedMesh mesh = jsonLoader.loadSkinnedMesh(SkinnedMesh::new);
                        Armature armature = jsonLoader.loadArmature(Armature::new);
                        this.userMeshes.add(PackEntry.ofValue(modelPath, SelfAccessor.create(modelId, mesh)));
                        this.userArmatures.add(PackEntry.ofValue(modelPath, SelfAccessor.create(modelId, armature)));
                        this.meshGrid.addRowWithDefaultValues("mesh_name", modelPath);
                        this.armatureGrid.addRowWithDefaultValues("armature_name", modelPath);
                     } catch (Exception e) {
                        e.printStackTrace();
                     } finally {
                        try {
                           stream.close();
                        } catch (IOException var19) {
                        }
                     }
                  }

                  this.meshGrid.setValueChangeEnabled(true);
                  this.armatureGrid.setValueChangeEnabled(true);
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
      return this.modelPreviewer.m_7979_(mouseX, mouseY, button, dx, dy) ? true : super.m_7979_(mouseX, mouseY, button, dx, dy);
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280039_(guiGraphics);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }
}
