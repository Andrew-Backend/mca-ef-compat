package yesman.epicfight.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.gui.ScreenCalculations;
import yesman.epicfight.client.gui.screen.config.UISetupScreen;
import yesman.epicfight.config.OptionHandler;

public class UIComponent extends Button {
   protected final UISetupScreen parentScreen;
   protected final ResourceLocation texture;
   protected int texU;
   protected int texV;
   protected int texW;
   protected int texH;
   protected int resolutionDivW;
   protected int resolutionDivH;
   protected int draggingTime;
   protected float r;
   protected float g;
   protected float b;
   private double pressX;
   private double pressY;
   public final OptionHandler<Integer> xCoord;
   public final OptionHandler<Integer> yCoord;
   public final OptionHandler<ScreenCalculations.HorizontalBasis> horizontalBasis;
   public final OptionHandler<ScreenCalculations.VerticalBasis> verticalBasis;
   public UIComponentPop<?> popupScreen;

   public UIComponent(
      int x,
      int y,
      OptionHandler<Integer> xCoord,
      OptionHandler<Integer> yCoord,
      OptionHandler<ScreenCalculations.HorizontalBasis> horizontalBasis,
      OptionHandler<ScreenCalculations.VerticalBasis> verticalBasis,
      int width,
      int height,
      int texU,
      int texV,
      int texW,
      int texH,
      int resolutionDivW,
      int resolutionDivH,
      int r,
      int g,
      int b,
      UISetupScreen parentScreen,
      ResourceLocation texture
   ) {
      super(x, y, width, height, Component.m_237113_(""), button -> {}, Button.f_252438_);
      this.texture = texture;
      this.texU = texU;
      this.texV = texV;
      this.texW = texW;
      this.texH = texH;
      this.resolutionDivW = resolutionDivW;
      this.resolutionDivH = resolutionDivH;
      this.r = r / 255.0F;
      this.g = g / 255.0F;
      this.b = b / 255.0F;
      this.xCoord = xCoord;
      this.yCoord = yCoord;
      this.horizontalBasis = horizontalBasis;
      this.verticalBasis = verticalBasis;
      this.parentScreen = parentScreen;
      this.popupScreen = new UIComponentPop<>(30, 30, this);
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      if (this.f_93623_ && this.f_93624_) {
         if (this.m_7972_(button)) {
            this.draggingTime = 0;
            if (this.m_93680_(mouseX, mouseY)) {
               this.parentScreen.beginToDrag(this);
               this.pressX = mouseX - this.m_252754_();
               this.pressY = mouseY - this.m_252907_();
               this.m_7435_(Minecraft.m_91087_().m_91106_());
               if (!this.popupScreen.isHoverd(this.m_252754_(), this.m_252907_())) {
                  this.popupScreen.closePop();
               }

               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   protected void m_7212_(double x, double y, double dx, double dy) {
      if (this.parentScreen.isDraggingComponent(this)) {
         this.m_252865_((int)(x - this.pressX));
         this.m_253211_((int)(y - this.pressY));
         this.draggingTime++;
      }
   }

   public boolean m_6348_(double mouseX, double mouseY, int button) {
      if (this.m_7972_(button)) {
         this.m_7691_(mouseX, mouseY);
         this.parentScreen.endDragging();
         int xCoord = this.horizontalBasis.getValue().saveCoordGetter.apply(this.parentScreen.f_96543_, this.m_252754_());
         int yCoord = this.verticalBasis.getValue().saveCoordGetter.apply(this.parentScreen.f_96544_, this.m_252907_());
         this.xCoord.setValue(xCoord);
         this.yCoord.setValue(yCoord);
         return true;
      } else {
         return false;
      }
   }

   public void m_7691_(double x, double y) {
      if (!this.popupScreen.isOpen() && this.draggingTime < 2) {
         if (x + this.popupScreen.width > this.parentScreen.f_96543_) {
            this.popupScreen.x = (int)x - this.popupScreen.width;
         } else {
            this.popupScreen.x = (int)x;
         }

         if (y + this.popupScreen.height > this.parentScreen.f_96544_) {
            this.popupScreen.y = (int)y - this.popupScreen.height;
         } else {
            this.popupScreen.y = (int)y;
         }

         this.popupScreen.openPop();
      }
   }

   public void drawOutline(GuiGraphics guiGraphics) {
      PoseStack poseStack = guiGraphics.m_280168_();
      float screenX = this.m_252754_() - 1;
      float screenXEnd = this.m_252754_() + this.f_93618_ + 1;
      float screenY = this.m_252907_() - 1;
      float screenYEnd = this.m_252907_() + this.f_93619_ + 1;
      RenderSystem.disableCull();
      RenderSystem.lineWidth(2.0F);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShader(GameRenderer::m_172757_);
      BufferBuilder bufferbuilder = Tesselator.m_85913_().m_85915_();
      bufferbuilder.m_166779_(Mode.LINES, DefaultVertexFormat.f_166851_);
      bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
      bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenXEnd, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
      bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenXEnd, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenXEnd, screenYEnd, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(0.0F, -1.0F, 0.0F).m_5752_();
      bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenXEnd, screenYEnd, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(-1.0F, 0.0F, 0.0F).m_5752_();
      bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, screenYEnd, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(-1.0F, 0.0F, 0.0F).m_5752_();
      bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, screenYEnd, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(0.0F, 1.0F, 0.0F).m_5752_();
      bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(0.0F, 1.0F, 0.0F).m_5752_();
      if (this.horizontalBasis.getValue() == ScreenCalculations.HorizontalBasis.CENTER) {
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX + (screenXEnd - screenX) / 2.0F, screenY + (screenYEnd - screenY) / 2.0F, 0.0F)
            .m_6122_(69, 166, 244, 255)
            .m_5601_(1.0F, 0.0F, 0.0F)
            .m_5752_();
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), this.parentScreen.f_96543_ / 2, screenY + (screenYEnd - screenY) / 2.0F, 0.0F)
            .m_6122_(69, 166, 244, 255)
            .m_5601_(1.0F, 0.0F, 0.0F)
            .m_5752_();
      } else if (this.horizontalBasis.getValue() == ScreenCalculations.HorizontalBasis.LEFT) {
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), 0.0F, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
      } else if (this.horizontalBasis.getValue() == ScreenCalculations.HorizontalBasis.RIGHT) {
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(1.0F, 0.0F, 0.0F).m_5752_();
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), this.parentScreen.f_96543_, screenY, 0.0F)
            .m_6122_(69, 166, 244, 255)
            .m_5601_(1.0F, 0.0F, 0.0F)
            .m_5752_();
      }

      if (this.verticalBasis.getValue() == ScreenCalculations.VerticalBasis.CENTER) {
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX + (screenXEnd - screenX) / 2.0F, screenY + (screenYEnd - screenY) / 2.0F, 0.0F)
            .m_6122_(69, 166, 244, 255)
            .m_5601_(0.0F, 1.0F, 0.0F)
            .m_5752_();
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX + (screenXEnd - screenX) / 2.0F, this.parentScreen.f_96544_ / 2, 0.0F)
            .m_6122_(69, 166, 244, 255)
            .m_5601_(0.0F, 1.0F, 0.0F)
            .m_5752_();
      } else if (this.verticalBasis.getValue() == ScreenCalculations.VerticalBasis.TOP) {
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(0.0F, 1.0F, 0.0F).m_5752_();
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, 0.0F, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(0.0F, 1.0F, 0.0F).m_5752_();
      } else if (this.verticalBasis.getValue() == ScreenCalculations.VerticalBasis.BOTTOM) {
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, screenY, 0.0F).m_6122_(69, 166, 244, 255).m_5601_(0.0F, 1.0F, 0.0F).m_5752_();
         bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), screenX, this.parentScreen.f_96544_, 0.0F)
            .m_6122_(69, 166, 244, 255)
            .m_5601_(0.0F, 1.0F, 0.0F)
            .m_5752_();
      }

      BufferUploader.m_231202_(bufferbuilder.m_231175_());
   }

   protected void m_87963_(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderColor(this.r, this.g, this.b, this.f_93625_);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      guiGraphics.m_280411_(
         this.texture,
         this.m_252754_(),
         this.m_252907_(),
         this.f_93618_,
         this.f_93619_,
         this.texU,
         this.texV,
         this.texW,
         this.texH,
         this.resolutionDivW,
         this.resolutionDivH
      );
      if (this.m_198029_() || this.popupScreen.isOpen()) {
         this.drawOutline(guiGraphics);
      }

      if (this.popupScreen.isOpen()) {
         this.popupScreen.m_88315_(guiGraphics, x, y, partialTicks);
      }
   }

   public static class PassiveUIComponent extends UIComponent {
      public final OptionHandler<ScreenCalculations.AlignDirection> alignDirection;
      protected final ResourceLocation texture2;

      public PassiveUIComponent(
         int x,
         int y,
         OptionHandler<Integer> xCoord,
         OptionHandler<Integer> yCoord,
         OptionHandler<ScreenCalculations.HorizontalBasis> horizontalBasis,
         OptionHandler<ScreenCalculations.VerticalBasis> verticalBasis,
         OptionHandler<ScreenCalculations.AlignDirection> alignDirection,
         int width,
         int height,
         int texU,
         int texV,
         int texW,
         int texH,
         int resolutionDivW,
         int resolutionDivH,
         int r,
         int g,
         int b,
         UISetupScreen parentScreen,
         ResourceLocation texture,
         ResourceLocation texture2
      ) {
         super(
            x,
            y,
            xCoord,
            yCoord,
            horizontalBasis,
            verticalBasis,
            width,
            height,
            texU,
            texV,
            texW,
            texH,
            resolutionDivW,
            resolutionDivH,
            r,
            g,
            b,
            parentScreen,
            texture
         );
         this.popupScreen = new UIComponentPop.PassivesUIComponentPop(30, 44, this);
         this.alignDirection = alignDirection;
         this.texture2 = texture2;
      }

      @Override
      protected void m_87963_(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
         Vec2i startPos = this.alignDirection.getValue()
            .startCoordGetter
            .get(this.m_252754_(), this.m_252907_(), this.f_93618_, this.f_93619_, 2, this.horizontalBasis.getValue(), this.verticalBasis.getValue());
         RenderSystem.setShader(GameRenderer::m_172817_);
         RenderSystem.setShaderColor(this.r, this.g, this.b, this.f_93625_);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         guiGraphics.m_280411_(
            this.texture,
            startPos.x,
            startPos.y,
            this.f_93618_,
            this.f_93619_,
            this.texU,
            this.texV,
            this.texW,
            this.texH,
            this.resolutionDivW,
            this.resolutionDivH
         );
         if (this.m_198029_() || this.popupScreen.isOpen()) {
            this.drawOutline(guiGraphics);
         }

         if (this.popupScreen.isOpen()) {
            this.popupScreen.m_88315_(guiGraphics, x, y, partialTicks);
         }

         Vec2i nextPos = this.alignDirection.getValue()
            .nextPositionGetter
            .getNext(this.horizontalBasis.getValue(), this.verticalBasis.getValue(), startPos, this.f_93618_, this.f_93619_);
         RenderSystem.setShader(GameRenderer::m_172817_);
         RenderSystem.setShaderColor(this.r, this.g, this.b, this.f_93625_);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         guiGraphics.m_280411_(
            this.texture2,
            nextPos.x,
            nextPos.y,
            this.f_93618_,
            this.f_93619_,
            this.texU,
            this.texV,
            this.texW,
            this.texH,
            this.resolutionDivW,
            this.resolutionDivH
         );
      }
   }
}
