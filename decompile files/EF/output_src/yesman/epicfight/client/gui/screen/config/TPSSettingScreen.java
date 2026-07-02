package yesman.epicfight.client.gui.screen.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

public class TPSSettingScreen extends Screen {
   private static final ResourceLocation BUTTON_TEXTURE = EpicFightMod.identifier("textures/gui/widget/camera_settings.png");
   protected final Screen parentScreen;
   protected Button up;
   protected Button down;
   protected Button left;
   protected Button right;
   protected TPSSettingScreen.ZoomScroll zoomScroll;

   protected TPSSettingScreen(Screen parentScreen) {
      super(Component.m_237115_(String.format("gui.%s.configuration.tps_mode_setting", "epicfight")));
      this.parentScreen = parentScreen;
   }

   public void m_7856_() {
      this.up = new TPSSettingScreen.CameraMoveButton(this.f_96543_ / 2 - 7, this.f_96544_ / 5 - 7, 14, 14, button -> {
         ClientConfig.cameraVerticalLocation = Math.min(5, ClientConfig.cameraVerticalLocation + 1);
         this.down.f_93623_ = true;
         if (ClientConfig.cameraVerticalLocation >= 5) {
            this.up.f_93623_ = false;
         }
      }, TPSSettingScreen.CameraMoveButton.Direction.UP);
      this.down = new TPSSettingScreen.CameraMoveButton(this.f_96543_ / 2 - 7, this.f_96544_ / 5 * 4 - 7, 14, 14, button -> {
         ClientConfig.cameraVerticalLocation = Math.max(-2, ClientConfig.cameraVerticalLocation - 1);
         this.up.f_93623_ = true;
         if (ClientConfig.cameraVerticalLocation <= -2) {
            this.down.f_93623_ = false;
         }
      }, TPSSettingScreen.CameraMoveButton.Direction.DOWN);
      this.left = new TPSSettingScreen.CameraMoveButton(this.f_96543_ / 5 - 7, this.f_96544_ / 2 - 7, 14, 14, button -> {
         ClientConfig.cameraHorizontalLocation = Math.min(10, ClientConfig.cameraHorizontalLocation + 1);
         this.right.f_93623_ = true;
         if (ClientConfig.cameraHorizontalLocation >= 10) {
            this.left.f_93623_ = false;
         }
      }, TPSSettingScreen.CameraMoveButton.Direction.LEFT);
      this.right = new TPSSettingScreen.CameraMoveButton(this.f_96543_ / 5 * 4 - 7, this.f_96544_ / 2 - 7, 14, 14, button -> {
         ClientConfig.cameraHorizontalLocation = Math.max(-10, ClientConfig.cameraHorizontalLocation - 1);
         this.left.f_93623_ = true;
         if (ClientConfig.cameraHorizontalLocation <= -10) {
            this.right.f_93623_ = false;
         }
      }, TPSSettingScreen.CameraMoveButton.Direction.RIGHT);
      this.zoomScroll = new TPSSettingScreen.ZoomScroll(this.f_96543_ / 2 + 24, this.f_96544_ / 2 - 26, ClientConfig.cameraZoom - 3);
      if (ClientConfig.cameraVerticalLocation >= 5) {
         this.up.f_93623_ = false;
      }

      if (ClientConfig.cameraVerticalLocation <= -2) {
         this.down.f_93623_ = false;
      }

      if (ClientConfig.cameraHorizontalLocation >= 10) {
         this.left.f_93623_ = false;
      }

      if (ClientConfig.cameraHorizontalLocation <= -10) {
         this.right.f_93623_ = false;
      }

      this.m_142416_(this.up);
      this.m_142416_(this.down);
      this.m_142416_(this.left);
      this.m_142416_(this.right);
      this.m_142416_(this.zoomScroll);
      this.f_96541_.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.m_280273_(guiGraphics);
      guiGraphics.m_280430_(this.f_96547_, this.f_96539_, 6, 6, 16777215);
      guiGraphics.m_280430_(this.f_96547_, Component.m_237113_("Exit"), this.f_96543_ - 24, 6, mouseX > this.f_96543_ - 24 && mouseY < 16 ? 7368816 : 16777215);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
   }

   public void m_7379_() {
      ClientConfig.saveChanges();
      this.f_96541_.m_91152_(this.parentScreen);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (mouseX > this.f_96543_ - 24 && mouseY < 16.0) {
         ClientConfig.saveChanges();
         this.f_96541_.m_91152_(null);
         return true;
      } else {
         return super.m_6375_(mouseX, mouseY, button);
      }
   }

   public boolean m_6050_(double pMouseX, double pMouseY, double pDelta) {
      if (!super.m_6050_(pMouseX, pMouseY, pDelta)) {
         this.zoomScroll.setScrollPosition(this.zoomScroll.scrollPosition - pDelta);
         return false;
      } else {
         return true;
      }
   }

   private class CameraMoveButton extends Button {
      private final TPSSettingScreen.CameraMoveButton.Direction direction;

      protected CameraMoveButton(int x, int y, int width, int height, OnPress onPress, TPSSettingScreen.CameraMoveButton.Direction direction) {
         super(x, y, width, height, Component.m_237119_(), onPress, Button.f_252438_);
         this.direction = direction;
      }

      protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, this.f_93625_);
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         guiGraphics.m_280168_().m_85836_();
         int u = this.f_93623_ ? 0 : 16;
         int v = this.f_93623_ && this.m_274382_() ? 16 : 0;
         guiGraphics.m_280168_().m_252880_(this.m_252754_(), this.m_252907_(), 0.0F);
         guiGraphics.m_280168_().m_252880_(8.0F, 8.0F, 0.0F);
         switch (this.direction) {
            case UP:
               guiGraphics.m_280168_().m_252781_(Axis.f_252403_.m_252977_(90.0F));
               break;
            case DOWN:
               guiGraphics.m_280168_().m_252781_(Axis.f_252403_.m_252977_(270.0F));
               break;
            case RIGHT:
               guiGraphics.m_280168_().m_252781_(Axis.f_252403_.m_252977_(180.0F));
         }

         guiGraphics.m_280168_().m_252880_(-8.0F, -8.0F, 0.0F);
         guiGraphics.m_280398_(TPSSettingScreen.BUTTON_TEXTURE, 0, 0, 0, u, v, 16, 16, 64, 64);
         guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
         guiGraphics.m_280168_().m_85849_();
      }

      public enum Direction {
         UP,
         DOWN,
         LEFT,
         RIGHT;
      }
   }

   private class ZoomScroll extends AbstractWidget {
      private double scrollPosition;

      public ZoomScroll(int x, int y, int initPos) {
         super(x, y, 12, 64, Component.m_237119_());
         this.scrollPosition = initPos;
      }

      protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         guiGraphics.m_280398_(TPSSettingScreen.BUTTON_TEXTURE, this.m_252754_(), this.m_252907_(), 0, 52.0F, 0.0F, this.f_93618_, this.f_93619_, 64, 64);
         int scrollCoord = (int)this.scrollPosition * 6;
         guiGraphics.m_280509_(this.m_252754_() + 3, this.m_252907_() + scrollCoord + 3, this.m_252754_() + 9, this.m_252907_() + scrollCoord + 7, -1);
      }

      public void m_5716_(double mouseX, double mouseY) {
         if (this.m_252754_() + 3 <= mouseX && mouseX <= this.m_252754_() + 9 && this.m_252907_() + 1 <= mouseY && mouseY <= this.m_252907_() + 63) {
            this.setScrollBaseOnYPressed(mouseY);
         }
      }

      protected void m_7212_(double mouseX, double mouseY, double dragX, double dragY) {
         if (this.m_252754_() + 3 <= mouseX
            && mouseX <= this.m_252754_() + 9
            && this.m_252907_() + 1 <= mouseY + dragY
            && mouseY + dragY <= this.m_252907_() + 63) {
            this.setScrollBaseOnYPressed(mouseY + dragY);
         }
      }

      private void setScrollBaseOnYPressed(double y) {
         double relativeY = y - this.m_252907_() + 1.0;
         this.setScrollPosition(relativeY / 6.0);
      }

      private void setScrollPosition(double pos) {
         this.scrollPosition = (int)Mth.m_14008_(pos, 0.0, 7.0);
         ClientConfig.cameraZoom = (int)this.scrollPosition + 3;
      }

      protected void m_168797_(NarrationElementOutput narrationElementOutput) {
         narrationElementOutput.m_169146_(NarratedElementType.TITLE, this.m_5646_());
      }
   }
}
