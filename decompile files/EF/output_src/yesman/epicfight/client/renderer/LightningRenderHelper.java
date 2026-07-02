package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.api.utils.math.QuaternionUtils;

public class LightningRenderHelper {
   private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);

   private static void vertex01(VertexConsumer vertexConsumer, Matrix4f matrix4f, int alpha) {
      vertexConsumer.m_252986_(matrix4f, 0.0F, 0.0F, 0.0F).m_6122_(255, 255, 255, alpha).m_5752_();
   }

   private static void vertex2(VertexConsumer vertexConsumer, Matrix4f matrix4f, float height, float width, int rCol, int gCol, int bCol) {
      vertexConsumer.m_252986_(matrix4f, -HALF_SQRT_3 * width, height, -0.5F * width).m_6122_(rCol, gCol, bCol, 0).m_5752_();
   }

   private static void vertex3(VertexConsumer vertexConsumer, Matrix4f matrix4f, float height, float width, int rCol, int gCol, int bCol) {
      vertexConsumer.m_252986_(matrix4f, HALF_SQRT_3 * width, height, -0.5F * width).m_6122_(rCol, gCol, bCol, 0).m_5752_();
   }

   private static void vertex4(VertexConsumer vertexConsumer, Matrix4f matrix4f, float width, float height, int rCol, int gCol, int bCol) {
      vertexConsumer.m_252986_(matrix4f, 0.0F, width, height).m_6122_(rCol, gCol, bCol, 0).m_5752_();
   }

   public static void renderCyclingLight(
      VertexConsumer vertexConsumer, PoseStack poseStack, int rCol, int gCol, int bCol, int density, float size, float progression, float repeater
   ) {
      Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
      Random random = new Random(123L);

      for (int i = 0; i < density; i++) {
         poseStack.m_252781_(QuaternionUtils.XP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.m_252781_(QuaternionUtils.ZP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.m_252781_(QuaternionUtils.XP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(random.nextFloat() * 360.0F));
         poseStack.m_252781_(QuaternionUtils.ZP.rotationDegrees(random.nextFloat() * 360.0F + progression * 90.0F));
         float height = (random.nextFloat() * 20.0F + 5.0F + repeater * 10.0F) * size;
         float width = (random.nextFloat() * 2.0F + 1.0F + repeater * 2.0F) * size;
         float randomf = random.nextFloat();
         float alpha = ((float)Math.sin((randomf + progression) * Math.PI) + 1.0F) * 0.5F;
         int j = (int)(255.0F * alpha);
         vertex01(vertexConsumer, matrix4f, j);
         vertex2(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex3(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex01(vertexConsumer, matrix4f, j);
         vertex3(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex4(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex01(vertexConsumer, matrix4f, j);
         vertex4(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex2(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
      }
   }

   public static void renderFlashingLight(
      VertexConsumer vertexConsumer, PoseStack poseStack, int rCol, int gCol, int bCol, int density, float size, float progression
   ) {
      double d1 = progression * 3.0F * Math.PI / 2.0;
      float sinDelta = Math.max((float)(d1 < Math.PI / 2 ? Math.sin(d1) : Math.sin((d1 + (Math.PI / 2)) / 2.0)), 0.0F);
      float linearDelta = progression < 0.3F ? progression + 0.7F : 1.0F;
      Random random = new Random(432L);

      for (int i = 0; i < density; i++) {
         poseStack.m_85836_();
         Vector3f randomAxis = new Vector3f(-0.5F + random.nextFloat(), 0.0F, -0.5F + random.nextFloat());
         randomAxis.normalize();
         float randomDegree = -120.0F + random.nextFloat() * 240.0F;
         Quaternionf randomRotation = QuaternionUtils.rotationDegrees(randomAxis, randomDegree);
         poseStack.m_252781_(randomRotation);
         poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(random.nextFloat() * 360.0F));
         float height = 14.0F * linearDelta * size;
         float width = (0.3F + random.nextFloat()) * size;
         Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
         int alpha = (int)(255.0F * sinDelta);
         vertex01(vertexConsumer, matrix4f, alpha);
         vertex2(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex3(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex01(vertexConsumer, matrix4f, alpha);
         vertex3(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex4(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex01(vertexConsumer, matrix4f, alpha);
         vertex4(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         vertex2(vertexConsumer, matrix4f, height, width, rCol, gCol, bCol);
         poseStack.m_85849_();
      }
   }
}
