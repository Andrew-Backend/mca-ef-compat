package yesman.epicfight.client.gui.screen.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class BlendingTextureOverlay extends OverlayManager.Overlay {
   public ResourceLocation texture;
   private boolean isAlive = true;

   public BlendingTextureOverlay(ResourceLocation texture) {
      this.texture = texture;
   }

   public void remove() {
      this.isAlive = false;
   }

   @Override
   public boolean render(int xResolution, int yResolution) {
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderTexture(0, this.texture);
      GlStateManager._enableBlend();
      GlStateManager._disableDepthTest();
      GlStateManager._blendFunc(770, 771);
      Tesselator tessellator = Tesselator.m_85913_();
      BufferBuilder bufferbuilder = tessellator.m_85915_();
      bufferbuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
      bufferbuilder.m_5483_(0.0, 0.0, 1.0).m_7421_(0.0F, 0.0F).m_5752_();
      bufferbuilder.m_5483_(0.0, yResolution, 1.0).m_7421_(0.0F, 1.0F).m_5752_();
      bufferbuilder.m_5483_(xResolution, yResolution, 1.0).m_7421_(1.0F, 1.0F).m_5752_();
      bufferbuilder.m_5483_(xResolution, 0.0, 1.0).m_7421_(1.0F, 0.0F).m_5752_();
      tessellator.m_85914_();
      return !this.isAlive;
   }
}
