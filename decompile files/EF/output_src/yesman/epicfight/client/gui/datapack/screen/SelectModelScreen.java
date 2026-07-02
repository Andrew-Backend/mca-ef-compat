package yesman.epicfight.client.gui.datapack.screen;

import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;

public class SelectModelScreen extends Screen {
   private final Screen parentScreen;
   private final SelectModelScreen.ModelList modelList;
   private final ModelPreviewer modelPreviewer;
   private final EditBox searchBox;
   private final BiConsumer<String, AssetAccessor<SkinnedMesh>> selectCallback;
   private final BiConsumer<String, AssetAccessor<SkinnedMesh>> cancelCallback;

   public SelectModelScreen(
      Screen parentScreen, BiConsumer<String, AssetAccessor<SkinnedMesh>> selectCallback, BiConsumer<String, AssetAccessor<SkinnedMesh>> cancelCallback
   ) {
      super(Component.m_237115_("gui.epicfight.select.models"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.modelPreviewer = new ModelPreviewer(10, 20, 36, 60, null, null, null, null);
      this.modelList = new SelectModelScreen.ModelList(parentScreen.getMinecraft(), this.f_96543_, this.f_96544_, 36, this.f_96544_ - 16, 21);
      this.modelList.m_93496_(false);
      int var10004 = this.f_96543_ / 2;
      this.searchBox = new EditBox(parentScreen.getMinecraft().f_91062_, var10004, 12, this.f_96543_ / 2 - 12, 16, Component.m_237113_("datapack_edit.keyword"));
      this.searchBox.m_94151_(this.modelList::refreshModelList);
      this.selectCallback = selectCallback;
      this.cancelCallback = cancelCallback;
      this.modelList.refreshModelList(null);
   }

   public void refreshModelList() {
      this.modelList.refreshModelList(this.searchBox.m_94155_());
   }

   protected void m_7856_() {
      int split = this.f_96543_ / 2 - 80;
      this.modelPreviewer._setWidth(split - 10);
      this.modelPreviewer._setHeight(this.f_96544_ - 68);
      this.modelPreviewer.resize(null);
      this.modelList.m_93437_(this.f_96543_ - split, this.f_96544_, 36, this.f_96544_ - 32);
      this.modelList.m_93507_(split);
      this.searchBox.m_252865_(this.f_96543_ / 2);
      this.searchBox.m_253211_(12);
      this.searchBox.m_93674_(this.f_96543_ / 2 - 12);
      this.searchBox.setHeight(16);
      this.m_142416_(this.searchBox);
      this.m_142416_(
         Button.m_253074_(Component.m_237115_("datapack_edit.import_model"), button -> Minecraft.m_91087_().m_91152_(new ImportModelScreen(this)))
            .m_252794_(10, 10)
            .m_253046_(100, 21)
            .m_253136_()
      );
      this.m_142416_(this.modelPreviewer);
      this.m_142416_(this.modelList);
      this.m_142416_(
         Button.m_253074_(
               CommonComponents.f_286989_,
               button -> {
                  if (this.modelList.m_93511_() == null) {
                     this.f_96541_.m_91152_(new MessageScreen("", "Select an item from the list", this, button$2 -> this.f_96541_.m_91152_(this), 180, 60));
                  } else {
                     try {
                        this.selectCallback
                           .accept(
                              ((SelectModelScreen.ModelList.ModelEntry)this.modelList.m_93511_()).registryName,
                              ((SelectModelScreen.ModelList.ModelEntry)this.modelList.m_93511_()).mesh
                           );
                        this.m_7379_();
                     } catch (Exception e) {
                        this.f_96541_
                           .m_91152_(
                              new MessageScreen("", e.getMessage(), this.parentScreen, button$2 -> this.f_96541_.m_91152_(this.parentScreen), 180, 70)
                                 .autoCalculateHeight()
                           );
                     }
                  }
               }
            )
            .m_252794_(this.f_96543_ / 2 - 162, this.f_96544_ - 28)
            .m_253046_(160, 21)
            .m_253136_()
      );
      this.m_142416_(Button.m_253074_(CommonComponents.f_130656_, button -> {
         this.cancelCallback.accept("", null);
         this.m_7379_();
      }).m_252794_(this.f_96543_ / 2 + 2, this.f_96544_ - 28).m_253046_(160, 21).m_253136_());
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      return this.modelPreviewer.m_7979_(mouseX, mouseY, button, dx, dy) ? true : super.m_7979_(mouseX, mouseY, button, dx, dy);
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
      this.modelPreviewer.onDestroy();
   }

   public void m_86600_() {
      this.modelPreviewer._tick();
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280039_(guiGraphics);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   class ModelList extends ObjectSelectionList<SelectModelScreen.ModelList.ModelEntry> {
      public ModelList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
         super(minecraft, width, height, y0, y1, itemHeight);
      }

      public void setSelected(@Nullable SelectModelScreen.ModelList.ModelEntry selEntry) {
         super.m_6987_(selEntry);
         SelectModelScreen.this.modelPreviewer.setMesh(selEntry.mesh);
      }

      public int m_5759_() {
         return this.f_93388_;
      }

      protected int m_5756_() {
         return this.f_93392_ - 6;
      }

      public void refreshModelList(String keyward) {
         this.m_93410_(0.0);
         this.m_6702_().clear();
         Set<AssetAccessor<SkinnedMesh>> skinnedMeshes = Meshes.entry(SkinnedMesh.class);
         DatapackEditScreen.getCurrentScreen().getUserMeshes().values().forEach(accessor -> skinnedMeshes.add((AssetAccessor<SkinnedMesh>)accessor));
         skinnedMeshes.stream()
            .filter(accessor -> StringUtil.m_14408_(keyward) ? true : accessor.registryName().toString().contains(keyward))
            .map(accessor -> new SelectModelScreen.ModelList.ModelEntry(accessor.registryName().toString(), (AssetAccessor<SkinnedMesh>)accessor))
            .sorted((entry$1, entry$2) -> entry$1.registryName.compareTo(entry$2.registryName))
            .forEach(x$0 -> this.m_7085_(x$0));
      }

      class ModelEntry extends Entry<SelectModelScreen.ModelList.ModelEntry> {
         private final String registryName;
         private final AssetAccessor<SkinnedMesh> mesh;

         public ModelEntry(String registryName, AssetAccessor<SkinnedMesh> mesh) {
            this.registryName = registryName;
            this.mesh = mesh;
         }

         public void m_6311_(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks
         ) {
            guiGraphics.m_280056_(SelectModelScreen.this.f_96541_.f_91062_, this.registryName, left + 5, top + 5, 16777215, false);
         }

         public Component m_142172_() {
            return Component.m_237115_("narrator.select");
         }

         public boolean m_6375_(double mouseX, double mouseY, int button) {
            if (button == 0) {
               if (ModelList.this.m_93511_() == this) {
                  try {
                     SelectModelScreen.this.selectCallback.accept(this.registryName, this.mesh);
                     SelectModelScreen.this.m_7379_();
                  } catch (Exception e) {
                     SelectModelScreen.this.f_96541_
                        .m_91152_(
                           new MessageScreen(
                                 "",
                                 e.getMessage(),
                                 SelectModelScreen.this.parentScreen,
                                 button$2 -> SelectModelScreen.this.f_96541_.m_91152_(SelectModelScreen.this.parentScreen),
                                 180,
                                 70
                              )
                              .autoCalculateHeight()
                        );
                  }

                  return true;
               } else {
                  ModelList.this.setSelected(this);
                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }
}
