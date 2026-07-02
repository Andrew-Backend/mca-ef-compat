package yesman.epicfight.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import yesman.epicfight.client.gui.ScreenCalculations;
import yesman.epicfight.config.OptionHandler;
import yesman.epicfight.main.EpicFightMod;

public class UIComponentPop<T extends UIComponent> extends Screen implements ContainerEventHandler {
   protected final T parentWidget;
   protected int width;
   protected int height;
   public int x;
   public int y;
   private boolean enable;

   public UIComponentPop(int width, int height, T parentWidget) {
      super(Component.m_237113_(""));
      this.width = width;
      this.height = height;
      this.parentWidget = parentWidget;
      this.m_7856_();
   }

   public void m_7856_() {
      this.m_169413_();
      this.m_142416_(createButton(this.x + 10, this.y - 2, 11, 8, button -> {
         this.parentWidget.verticalBasis.setValue(ScreenCalculations.VerticalBasis.TOP);
         this.parentWidget.yCoord.setValue(ScreenCalculations.VerticalBasis.TOP.saveCoordGetter.apply(this.parentWidget.parentScreen.f_96544_, this.y));
      }));
      this.m_142416_(createButton(this.x - 2, this.y + 11, 11, 7, button -> {
         this.parentWidget.horizontalBasis.setValue(ScreenCalculations.HorizontalBasis.LEFT);
         this.parentWidget.xCoord.setValue(ScreenCalculations.HorizontalBasis.LEFT.saveCoordGetter.apply(this.parentWidget.parentScreen.f_96543_, this.x));
      }));
      this.m_142416_(createButton(this.x + 22, this.y + 11, 11, 7, button -> {
         this.parentWidget.horizontalBasis.setValue(ScreenCalculations.HorizontalBasis.RIGHT);
         this.parentWidget.xCoord.setValue(ScreenCalculations.HorizontalBasis.RIGHT.saveCoordGetter.apply(this.parentWidget.parentScreen.f_96543_, this.x));
      }));
      this.m_142416_(createButton(this.x + 10, this.y + 24, 11, 8, button -> {
         this.parentWidget.verticalBasis.setValue(ScreenCalculations.VerticalBasis.BOTTOM);
         this.parentWidget.yCoord.setValue(ScreenCalculations.VerticalBasis.BOTTOM.saveCoordGetter.apply(this.parentWidget.parentScreen.f_96544_, this.y));
      }));
      this.m_142416_(createButton(this.x + 10, this.y + 11, 11, 7, button -> {
         this.parentWidget.verticalBasis.setValue(ScreenCalculations.VerticalBasis.CENTER);
         this.parentWidget.horizontalBasis.setValue(ScreenCalculations.HorizontalBasis.CENTER);
         this.parentWidget.xCoord.setValue(ScreenCalculations.HorizontalBasis.CENTER.saveCoordGetter.apply(this.parentWidget.parentScreen.f_96543_, this.x));
         this.parentWidget.yCoord.setValue(ScreenCalculations.VerticalBasis.CENTER.saveCoordGetter.apply(this.parentWidget.parentScreen.f_96544_, this.y));
      }));
   }

   public static Button createButton(int x, int y, int width, int height, OnPress onpress) {
      return Button.m_253074_(Component.m_237113_(""), onpress).m_252987_(x, y, width, height).m_253136_();
   }

   public void openPop() {
      this.enable = true;
      this.m_7856_();
   }

   public void closePop() {
      this.enable = false;
   }

   protected boolean isHoverd(double x, double y) {
      return this.enable && x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
   }

   public boolean isOpen() {
      return this.enable;
   }

   public boolean m_6375_(double x, double y, int pressType) {
      if (!this.enable) {
         return false;
      }

      boolean clicked = false;

      for (GuiEventListener listener : this.m_6702_()) {
         clicked |= listener.m_6375_(x, y, pressType);
      }

      return clicked;
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (this.enable) {
         boolean popupOut = mouseX < this.x - 3 || mouseY < this.y - 3 || mouseX >= this.x + this.width + 3 || mouseY >= this.y + this.height + 3;
         boolean parentOut = mouseX < this.parentWidget.m_252754_() - 3
            || mouseY < this.parentWidget.m_252907_() - 3
            || mouseX >= this.parentWidget.m_252754_() + this.parentWidget.m_5711_() + 3
            || mouseY >= this.parentWidget.m_252907_() + this.parentWidget.m_93694_() + 3;
         if (popupOut && parentOut) {
            this.enable = false;
         }

         PoseStack poseStack = guiGraphics.m_280168_();
         poseStack.m_85836_();
         poseStack.m_252880_(0.0F, 0.0F, 200.0F);
         this.renderPopup(guiGraphics, this.x, this.y, this.width, this.height);
         super.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
         poseStack.m_85849_();
      }
   }

   protected void renderPopup(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      int i = width;
      int j = height;
      int j2 = x;
      int k2 = y;
      RenderSystem.setShader(GameRenderer::m_172811_);
      int backgroundStart = -267386864;
      int backgroundEnd = -267386864;
      int boarderStart = 1347420415;
      int boarderEnd = 1344798847;
      guiGraphics.m_280120_(j2 - 3, k2 - 4, j2 + i + 3, k2 - 3, 0, backgroundStart, backgroundStart);
      guiGraphics.m_280120_(j2 - 3, k2 + j + 3, j2 + i + 3, k2 + j + 4, 0, backgroundEnd, backgroundEnd);
      guiGraphics.m_280120_(j2 - 3, k2 - 3, j2 + i + 3, k2 + j + 3, 0, backgroundStart, backgroundEnd);
      guiGraphics.m_280120_(j2 - 4, k2 - 3, j2 - 3, k2 + j + 3, 0, backgroundStart, backgroundEnd);
      guiGraphics.m_280120_(j2 + i + 3, k2 - 3, j2 + i + 4, k2 + j + 3, 0, backgroundStart, backgroundEnd);
      guiGraphics.m_280120_(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + j + 3 - 1, 0, boarderStart, boarderEnd);
      guiGraphics.m_280120_(j2 + i + 2, k2 - 3 + 1, j2 + i + 3, k2 + j + 3 - 1, 0, boarderStart, boarderEnd);
      guiGraphics.m_280120_(j2 - 3, k2 - 3, j2 + i + 3, k2 - 3 + 1, 0, boarderStart, boarderStart);
      guiGraphics.m_280120_(j2 - 3, k2 + j + 2, j2 + i + 3, k2 + j + 3, 0, boarderEnd, boarderEnd);
   }

   public static class PassivesUIComponentPop extends UIComponentPop<UIComponent.PassiveUIComponent> {
      public PassivesUIComponentPop(int width, int height, UIComponent.PassiveUIComponent parentWidget) {
         super(width, height, parentWidget);
      }

      @Override
      protected void renderPopup(GuiGraphics guiGraphics, int x, int y, int width, int height) {
         super.renderPopup(guiGraphics, x, y + 14, width, height - 14);
      }

      @Override
      public void m_7856_() {
         super.m_7856_();

         for (GuiEventListener gui : this.m_6702_()) {
            if (gui instanceof AbstractWidget widget) {
               widget.m_253211_(widget.m_252907_() + 14);
            }
         }

         this.m_142416_(
            new UIComponentPop.PassivesUIComponentPop.AlignButton(
               this.x - 3,
               this.y,
               12,
               10,
               this.parentWidget.horizontalBasis,
               this.parentWidget.verticalBasis,
               this.parentWidget.alignDirection,
               button -> {
                  ScreenCalculations.AlignDirection newAlignDirection = ScreenCalculations.AlignDirection.values()[(
                        this.parentWidget.alignDirection.getValue().ordinal() + 1
                     )
                     % ScreenCalculations.AlignDirection.values().length];
                  this.parentWidget.alignDirection.setValue(newAlignDirection);
               }
            )
         );
      }

      public static class AlignButton extends Button {
         private static final ResourceLocation BATTLE_ICONS = EpicFightMod.identifier("textures/gui/battle_icons.png");
         private final OptionHandler<ScreenCalculations.HorizontalBasis> horBasis;
         private final OptionHandler<ScreenCalculations.VerticalBasis> verBasis;
         private final OptionHandler<ScreenCalculations.AlignDirection> alignDirection;

         public AlignButton(
            int x,
            int y,
            int width,
            int height,
            OptionHandler<ScreenCalculations.HorizontalBasis> horBasis,
            OptionHandler<ScreenCalculations.VerticalBasis> verBasis,
            OptionHandler<ScreenCalculations.AlignDirection> alignDirection,
            OnPress onpress
         ) {
            super(x, y, width, height, Component.m_237113_(""), onpress, Button.f_252438_);
            this.horBasis = horBasis;
            this.verBasis = verBasis;
            this.alignDirection = alignDirection;
         }

         protected void m_87963_(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
            RenderSystem.setShader(GameRenderer::m_172817_);
            RenderSystem.setShaderTexture(0, BATTLE_ICONS);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.f_93625_);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            Vec2[] texCoords = new Vec2[4];
            float startX;
            float startY;
            float width;
            float height;
            if (this.f_93622_) {
               startX = 0.5176471F;
               startY = 0.0F;
               width = 0.14117648F;
               height = 0.14117648F;
            } else {
               startX = 0.38039216F;
               startY = 0.007843138F;
               width = 0.12156863F;
               height = 0.12156863F;
            }

            Vec2 uv0 = new Vec2(startX, startY);
            Vec2 uv1 = new Vec2(startX + width, startY);
            Vec2 uv2 = new Vec2(startX + width, startY + height);
            Vec2 uv3 = new Vec2(startX, startY + height);
            texCoords[0] = uv0;
            texCoords[1] = uv1;
            texCoords[2] = uv2;
            texCoords[3] = uv3;
            if (this.alignDirection.getValue() == ScreenCalculations.AlignDirection.HORIZONTAL) {
               if (this.horBasis.getValue() == ScreenCalculations.HorizontalBasis.LEFT) {
                  texCoords[0] = uv1;
                  texCoords[1] = uv2;
                  texCoords[2] = uv3;
                  texCoords[3] = uv0;
               } else {
                  texCoords[0] = uv3;
                  texCoords[1] = uv0;
                  texCoords[2] = uv1;
                  texCoords[3] = uv2;
               }
            } else if (this.verBasis.getValue() == ScreenCalculations.VerticalBasis.BOTTOM) {
               texCoords[0] = uv2;
               texCoords[1] = uv3;
               texCoords[2] = uv0;
               texCoords[3] = uv1;
            }

            this.blitRotate(guiGraphics, texCoords);
         }

         public void blitRotate(GuiGraphics guiGraphics, Vec2[] texCoords) {
            PoseStack poseStack = guiGraphics.m_280168_();
            RenderSystem.setShader(GameRenderer::m_172817_);
            BufferBuilder bufferbuilder = Tesselator.m_85913_().m_85915_();
            bufferbuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
            bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), this.m_252754_(), this.m_252907_(), this.getBlitOffset())
               .m_7421_(texCoords[0].f_82470_, texCoords[0].f_82471_)
               .m_5752_();
            bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), this.m_252754_() + this.f_93618_, this.m_252907_(), this.getBlitOffset())
               .m_7421_(texCoords[1].f_82470_, texCoords[1].f_82471_)
               .m_5752_();
            bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), this.m_252754_() + this.f_93618_, this.m_252907_() + this.f_93619_, this.getBlitOffset())
               .m_7421_(texCoords[2].f_82470_, texCoords[2].f_82471_)
               .m_5752_();
            bufferbuilder.m_252986_(poseStack.m_85850_().m_252922_(), this.m_252754_(), this.m_252907_() + this.f_93619_, this.getBlitOffset())
               .m_7421_(texCoords[3].f_82470_, texCoords[3].f_82471_)
               .m_5752_();
            BufferUploader.m_231202_(bufferbuilder.m_231175_());
         }

         public int getBlitOffset() {
            return 0;
         }
      }
   }
}
