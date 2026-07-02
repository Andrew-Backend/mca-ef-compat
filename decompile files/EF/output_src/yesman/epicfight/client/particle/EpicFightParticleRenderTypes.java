package yesman.epicfight.client.particle;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class EpicFightParticleRenderTypes {
   public static final ParticleRenderType PARTICLE_MODEL_NO_NORMAL = new ParticleRenderType() {
      public void m_6505_(BufferBuilder bufferBuilder, TextureManager textureManager) {
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.depthMask(true);
         RenderSystem.setShader(GameRenderer::m_172829_);
         bufferBuilder.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_85813_);
      }

      public void m_6294_(Tesselator tesselator) {
         tesselator.m_85914_();
         RenderSystem.enableCull();
      }

      @Override
      public String toString() {
         return "epicfight:PARTICLE_MODEL_NO_NORMAL";
      }
   };
   public static final ParticleRenderType LIGHTNING = new ParticleRenderType() {
      public void m_6505_(BufferBuilder bufferBuilder, TextureManager textureManager) {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
         RenderSystem.depthMask(false);
         RenderSystem.setShader(GameRenderer::m_172753_);
         bufferBuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
      }

      public void m_6294_(Tesselator tesselator) {
         tesselator.m_85914_();
      }

      @Override
      public String toString() {
         return "epicfight:LIGHTING";
      }
   };
   public static final Function<ResourceLocation, ParticleRenderType> TRAIL_EFFECT = Util.m_143827_(textureLocation -> {
      TextureManager texturemanager = Minecraft.m_91087_().m_91097_();
      AbstractTexture abstracttexture = texturemanager.m_118506_(textureLocation);
      RenderSystem.bindTexture(abstracttexture.m_117963_());
      RenderSystem.texParameter(3553, 10242, 33071);
      RenderSystem.texParameter(3553, 10243, 33071);
      return new ParticleRenderType() {
         public void m_6505_(BufferBuilder bufferBuilder, TextureManager textureManager) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, textureLocation);
            bufferBuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85813_);
         }

         public void m_6294_(Tesselator tesselator) {
            tesselator.m_85914_();
            RenderSystem.enableCull();
         }

         @Override
         public String toString() {
            return "epicfight:TRAIL_EFFECT";
         }
      };
   });
   public static final ParticleRenderType TRANSLUCENT_GLOWING = new ParticleRenderType() {
      public void m_6505_(BufferBuilder bufferBuilder, TextureManager textureManager) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.depthMask(true);
         RenderSystem.setShader(GameRenderer::m_172811_);
         bufferBuilder.m_166779_(Mode.TRIANGLES, DefaultVertexFormat.f_85815_);
      }

      public void m_6294_(Tesselator tesselator) {
         tesselator.m_85914_();
      }

      @Override
      public String toString() {
         return "epicfight:TRANSLUCENT_GLOWING";
      }
   };
   public static final ParticleRenderType ENTITY_PARTICLE = new ParticleRenderType() {
      public void m_6505_(BufferBuilder bufferbuilder, TextureManager texManager) {
         RenderSystem.depthMask(true);
         RenderSystem.setShader(GameRenderer::m_172829_);
         bufferbuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85813_);
      }

      public void m_6294_(Tesselator tesselator) {
         tesselator.m_85914_();
         Minecraft.m_91087_().f_91063_.m_109154_().m_109896_();
      }

      @Override
      public String toString() {
         return "epicfight:ENTITY_PARTICLE";
      }
   };
}
