package forge.net.mca.client.gui.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class WidgetUtils {
   public static void drawRectangle(GuiGraphics context, int x0, int y0, int x1, int y1, int color) {
      context.m_280509_(x0 + 1, y0, x1, y0 + 1, color);
      context.m_280509_(x1 - 1, y0 + 1, x1, y1, color);
      context.m_280509_(x0, y1 - 1, x1 - 1, y1, color);
      context.m_280509_(x0, y0, x0 + 1, y1 - 1, color);
   }

   public static void drawTexturedQuad(Matrix4f matrix, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
      RenderSystem.setShader(GameRenderer::m_172817_);
      BufferBuilder bufferBuilder = Tesselator.m_85913_().m_85915_();
      bufferBuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
      bufferBuilder.m_252986_(matrix, x0, y1, z).m_7421_(u0, v1).m_5752_();
      bufferBuilder.m_252986_(matrix, x1, y1, z).m_7421_(u1, v1).m_5752_();
      bufferBuilder.m_252986_(matrix, x1, y0, z).m_7421_(u1, v0).m_5752_();
      bufferBuilder.m_252986_(matrix, x0, y0, z).m_7421_(u0, v0).m_5752_();
      BufferUploader.m_231202_(bufferBuilder.m_231175_());
   }

   public static void drawBackgroundEntity(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
      float f = (float)Math.atan(mouseX / 40.0F);
      float g = (float)Math.atan(mouseY / 40.0F);
      PoseStack matrixStack = RenderSystem.getModelViewStack();
      matrixStack.m_85836_();
      matrixStack.m_252880_(x, y, 50.0F);
      matrixStack.m_85841_(1.0F, 1.0F, -1.0F);
      RenderSystem.applyModelViewMatrix();
      PoseStack matrixStack2 = new PoseStack();
      matrixStack2.m_252880_(0.0F, 0.0F, 1000.0F);
      matrixStack2.m_85841_(size, size, size);
      Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf quaternionf2 = new Quaternionf().rotateX(g * 20.0F * (float) (Math.PI / 180.0));
      quaternionf.mul(quaternionf2);
      matrixStack2.m_252781_(quaternionf);
      float h = entity.f_20883_;
      float i = entity.m_146908_();
      float j = entity.m_146909_();
      float k = entity.f_20886_;
      float l = entity.f_20885_;
      entity.f_20883_ = 180.0F + f * 20.0F;
      entity.m_146922_(180.0F + f * 40.0F);
      entity.m_146926_(-g * 20.0F);
      entity.f_20885_ = entity.m_146908_();
      entity.f_20886_ = entity.m_146908_();
      Lighting.m_166384_();
      EntityRenderDispatcher entityRenderDispatcher = Minecraft.m_91087_().m_91290_();
      quaternionf2.conjugate();
      entityRenderDispatcher.m_252923_(quaternionf2);
      entityRenderDispatcher.m_114468_(false);
      BufferSource immediate = Minecraft.m_91087_().m_91269_().m_110104_();
      RenderSystem.runAsFancy(() -> entityRenderDispatcher.m_114384_(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrixStack2, immediate, 15728880));
      immediate.m_109911_();
      entityRenderDispatcher.m_114468_(true);
      entity.f_20883_ = h;
      entity.m_146922_(i);
      entity.m_146926_(j);
      entity.f_20886_ = k;
      entity.f_20885_ = l;
      matrixStack.m_85849_();
      RenderSystem.applyModelViewMatrix();
      Lighting.m_84931_();
   }
}
