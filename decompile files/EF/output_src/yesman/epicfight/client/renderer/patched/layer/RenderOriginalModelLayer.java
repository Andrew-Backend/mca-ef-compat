package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RenderOriginalModelLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>>
   extends PatchedLayer<E, T, M, RenderLayer<E, M>> {
   private final String parentJoint;
   private final Vec3f vec;
   private final Vec3f rot;

   public RenderOriginalModelLayer(String parentJoint, Vec3f vec, Vec3f rot) {
      this.parentJoint = parentJoint;
      this.vec = vec;
      this.rot = rot;
   }

   @Override
   protected void renderLayer(
      T entitypatch,
      E entityliving,
      RenderLayer<E, M> vanillaLayer,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      OpenMatrix4f[] poses,
      float bob,
      float yRot,
      float xRot,
      float partialTicks
   ) {
      OpenMatrix4f modelMatrix = poses[entitypatch.getArmature().searchJointByName(this.parentJoint).getId()];
      poseStack.m_85836_();
      MathUtils.mulStack(poseStack, modelMatrix);
      poseStack.m_252880_(this.vec.x, this.vec.y, this.vec.z);
      poseStack.m_252781_(Axis.f_252436_.m_252977_(this.rot.y));
      poseStack.m_252781_(Axis.f_252529_.m_252977_(this.rot.x));
      poseStack.m_252781_(Axis.f_252403_.m_252977_(this.rot.z));
      poseStack.m_85841_(-1.0F, -1.0F, 1.0F);
      vanillaLayer.m_6494_(
         poseStack, buffer, packedLight, entityliving, entityliving.f_267362_.m_267756_(), entityliving.f_267362_.m_267731_(), partialTicks, bob, yRot, xRot
      );
      poseStack.m_85849_();
   }
}
