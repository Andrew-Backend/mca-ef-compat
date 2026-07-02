package yesman.epicfight.client.gui.datapack.screen;

import java.util.function.Consumer;
import java.util.function.Predicate;
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
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.gui.datapack.widgets.ModelPreviewer;

public class SelectAnimationScreen extends Screen {
   private final Screen parentScreen;
   private final SelectAnimationScreen.AnimationList animationList;
   private final ModelPreviewer modelPreviewer;
   private final Consumer<AssetAccessor<? extends StaticAnimation>> selectCallback;
   private final Consumer<AssetAccessor<? extends StaticAnimation>> cancelCallback;
   private final Predicate<AssetAccessor<? extends StaticAnimation>> filter;
   private final EditBox searchBox;

   public SelectAnimationScreen(
      Screen parentScreen,
      Consumer<AssetAccessor<? extends StaticAnimation>> selectCallback,
      Consumer<AssetAccessor<? extends StaticAnimation>> cancelCallback,
      Predicate<AssetAccessor<? extends StaticAnimation>> filter,
      AssetAccessor<? extends Armature> armature,
      AssetAccessor<? extends SkinnedMesh> mesh
   ) {
      super(Component.m_237115_("gui.epicfight.select.animations"));
      this.parentScreen = parentScreen;
      this.f_96541_ = parentScreen.getMinecraft();
      this.f_96547_ = parentScreen.getMinecraft().f_91062_;
      this.modelPreviewer = new ModelPreviewer(10, 20, 36, 60, null, null, armature, mesh);
      this.animationList = new SelectAnimationScreen.AnimationList(parentScreen.getMinecraft(), this.f_96543_, this.f_96544_, 36, this.f_96544_ - 16, 21);
      this.animationList.m_93496_(false);
      this.selectCallback = selectCallback;
      this.cancelCallback = cancelCallback;
      this.filter = filter;
      int var10004 = this.f_96543_ / 2;
      this.searchBox = new EditBox(parentScreen.getMinecraft().f_91062_, var10004, 12, this.f_96543_ / 2 - 12, 16, Component.m_237113_("datapack_edit.keyword"));
      this.searchBox.m_94151_(this.animationList::refreshAniamtionList);
      this.animationList.refreshAniamtionList(null);
      if (armature != null) {
         this.searchBox.m_94144_(armature.get().toString().substring(armature.get().toString().indexOf("/") + 1));
         this.searchBox.m_94192_(0);
      }
   }

   public void refreshAnimationList() {
      this.animationList.refreshAniamtionList(this.searchBox.m_94155_());
   }

   protected void m_7856_() {
      int split = this.f_96543_ / 2 - 80;
      this.modelPreviewer._setWidth(split - 10);
      this.modelPreviewer._setHeight(this.f_96544_ - 68);
      this.modelPreviewer.resize(null);
      this.animationList.m_93437_(this.f_96543_ - split, this.f_96544_, 36, this.f_96544_ - 32);
      this.animationList.m_93507_(split);
      this.searchBox.m_252865_(this.f_96543_ / 2);
      this.searchBox.m_253211_(12);
      this.searchBox.m_93674_(this.f_96543_ / 2 - 12);
      this.searchBox.setHeight(16);
      this.m_142416_(this.searchBox);
      this.m_142416_(
         Button.m_253074_(
               Component.m_237115_("datapack_edit.import_animation"),
               button -> Minecraft.m_91087_().m_91152_(new ImportAnimationsScreen(this, this.modelPreviewer.getArmature(), this.modelPreviewer.getMesh()))
            )
            .m_252794_(10, 10)
            .m_253046_(100, 21)
            .m_253136_()
      );
      this.m_142416_(this.modelPreviewer);
      this.m_142416_(this.animationList);
      this.m_142416_(Button.m_253074_(CommonComponents.f_286989_, button -> {
         if (this.animationList.m_93511_() != null) {
            this.selectCallback.accept(((SelectAnimationScreen.AnimationList.AnimationEntry)this.animationList.m_93511_()).animation);
         }

         this.m_7379_();
      }).m_252794_(this.f_96543_ / 2 - 162, this.f_96544_ - 28).m_253046_(160, 21).m_253136_());
      this.m_142416_(Button.m_253074_(CommonComponents.f_130656_, button -> {
         this.cancelCallback.accept(null);
         this.m_7379_();
      }).m_252794_(this.f_96543_ / 2 + 2, this.f_96544_ - 28).m_253046_(160, 21).m_253136_());
   }

   public void m_7379_() {
      this.f_96541_.m_91152_(this.parentScreen);
      this.modelPreviewer.onDestroy();
   }

   public void m_86600_() {
      this.modelPreviewer._tick();
   }

   public boolean m_7979_(double mouseX, double mouseY, int button, double dx, double dy) {
      return this.modelPreviewer.m_7979_(mouseX, mouseY, button, dx, dy) ? true : super.m_7979_(mouseX, mouseY, button, dx, dy);
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280039_(guiGraphics);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
   }

   class AnimationList extends ObjectSelectionList<SelectAnimationScreen.AnimationList.AnimationEntry> {
      public AnimationList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
         super(minecraft, width, height, y0, y1, itemHeight);
      }

      public void setSelected(@Nullable SelectAnimationScreen.AnimationList.AnimationEntry selEntry) {
         super.m_6987_(selEntry);
         SelectAnimationScreen.this.modelPreviewer.clearAnimations();
         SelectAnimationScreen.this.modelPreviewer.addAnimationToPlay(selEntry.animation);
      }

      public int m_5759_() {
         return this.f_93388_;
      }

      protected int m_5756_() {
         return this.f_93392_ - 6;
      }

      public void refreshAniamtionList(String keyword) {
         this.m_93410_(0.0);
         this.m_6702_().clear();
         AnimationManager.getInstance()
            .getAnimations(SelectAnimationScreen.this.filter)
            .values()
            .stream()
            .filter(accessor -> StringUtil.m_14408_(keyword) ? true : accessor.registryName().toString().contains(keyword))
            .map(x$0 -> new SelectAnimationScreen.AnimationList.AnimationEntry(x$0))
            .sorted((a1, a2) -> Integer.compare(a1.animation.get().getId(), a2.animation.get().getId()))
            .forEach(x$0 -> this.m_7085_(x$0));
         DatapackEditScreen.getCurrentScreen()
            .getUserAniamtions()
            .values()
            .stream()
            .map(packEntry -> packEntry.getValue())
            .filter(SelectAnimationScreen.this.filter)
            .map(x$0 -> new SelectAnimationScreen.AnimationList.AnimationEntry(x$0))
            .sorted((a1, a2) -> a1.animation.registryName().compareTo(a2.animation.registryName()))
            .forEach(x$0 -> this.m_7085_(x$0));
      }

      class AnimationEntry extends Entry<SelectAnimationScreen.AnimationList.AnimationEntry> {
         private final AssetAccessor<? extends StaticAnimation> animation;

         public AnimationEntry(AssetAccessor<? extends StaticAnimation> animation) {
            this.animation = animation;
         }

         public void m_6311_(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks
         ) {
            guiGraphics.m_280056_(SelectAnimationScreen.this.f_96541_.f_91062_, this.animation.registryName().toString(), left + 5, top + 5, 16777215, false);
         }

         public Component m_142172_() {
            return Component.m_237115_("narrator.select");
         }

         public boolean m_6375_(double mouseX, double mouseY, int button) {
            if (button == 0) {
               if (AnimationList.this.m_93511_() == this) {
                  SelectAnimationScreen.this.selectCallback.accept(this.animation);
                  SelectAnimationScreen.this.f_96541_.m_91152_(SelectAnimationScreen.this.parentScreen);
                  return true;
               } else {
                  AnimationList.this.setSelected(this);
                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }
}
