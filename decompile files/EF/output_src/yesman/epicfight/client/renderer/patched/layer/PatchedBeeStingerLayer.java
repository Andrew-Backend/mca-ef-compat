package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedBeeStingerLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends PlayerModel<E>>
   extends PatchedStuckInBodyLayer<E, T, M, BeeStingerLayer<E, M>> {
   private static final ResourceLocation BEE_STINGER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_stinger.png");

   @Override
   protected void renderStuckItem(
      PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity entity, float pf1, float pf2, float pf3, float partialTick
   ) {
      float f = Mth.m_14116_(pf1 * pf1 + pf3 * pf3);
      float f1 = (float)(Math.atan2(pf1, pf3) * 180.0F / (float)Math.PI);
      float f2 = (float)(Math.atan2(pf2, f) * 180.0F / (float)Math.PI);
      poseStack.m_252880_(0.0F, 0.0F, 0.0F);
      poseStack.m_252781_(Axis.f_252436_.m_252977_(f1 - 90.0F));
      poseStack.m_252781_(Axis.f_252403_.m_252977_(f2));
      poseStack.m_252781_(Axis.f_252529_.m_252977_(45.0F));
      poseStack.m_85841_(0.03125F, 0.03125F, 0.03125F);
      poseStack.m_252880_(2.5F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = buffer.m_6299_(RenderType.m_110458_(BEE_STINGER_LOCATION));

      for (int i = 0; i < 4; i++) {
         poseStack.m_252781_(Axis.f_252529_.m_252977_(90.0F));
         Pose posestack$pose = poseStack.m_85850_();
         Matrix4f matrix4f = posestack$pose.m_252922_();
         Matrix3f matrix3f = posestack$pose.m_252943_();
         vertex(vertexconsumer, matrix4f, matrix3f, -4.5F, -1, 0.0F, 0.0F, packedLight);
         vertex(vertexconsumer, matrix4f, matrix3f, 4.5F, -1, 0.125F, 0.0F, packedLight);
         vertex(vertexconsumer, matrix4f, matrix3f, 4.5F, 1, 0.125F, 0.0625F, packedLight);
         vertex(vertexconsumer, matrix4f, matrix3f, -4.5F, 1, 0.0F, 0.0625F, packedLight);
      }
   }

   private static void vertex(VertexConsumer vertexConsumer, Matrix4f pose, Matrix3f normal, float x, int y, float z, float u, int v) {
      vertexConsumer.m_252986_(pose, x, y, 0.0F)
         .m_6122_(255, 255, 255, 255)
         .m_7421_(z, u)
         .m_86008_(OverlayTexture.f_118083_)
         .m_85969_(v)
         .m_252939_(normal, 0.0F, 1.0F, 0.0F)
         .m_5752_();
   }

   @Override
   protected int numStuck(E entity) {
      return entity.m_21235_();
   }
}
