package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import yesman.epicfight.api.utils.math.Vec3f;

public class RenderingTool {
   public static void drawQuad(PoseStack poseStack, VertexConsumer vertexBuilder, Vec3f pos, float size, float r, float g, float b) {
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
   }

   public static void drawCube(PoseStack poseStack, VertexConsumer vertexBuilder, Vec3f pos, float size, float r, float g, float b) {
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y - size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y - size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y - size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y - size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y + size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y + size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y + size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y + size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y + size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y + size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y - size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y - size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y + size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y + size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y - size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y - size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y + size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y + size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y - size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y - size, pos.z - size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y + size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y + size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x - size, pos.y - size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
      vertexBuilder.m_252986_(poseStack.m_85850_().m_252922_(), pos.x + size, pos.y - size, pos.z + size).m_85950_(r, g, b, 1.0F).m_5752_();
   }
}
