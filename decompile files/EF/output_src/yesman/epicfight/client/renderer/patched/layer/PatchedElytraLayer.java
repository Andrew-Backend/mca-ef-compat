package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedElytraLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>>
   extends PatchedLayer<E, T, M, ElytraLayer<E, M>> {
   protected void renderLayer(
      T entitypatch,
      E livingentity,
      ElytraLayer<E, M> vanillaLayer,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      OpenMatrix4f[] poses,
      float bob,
      float yRot,
      float xRot,
      float partialTicks
   ) {
      if (vanillaLayer.shouldRender(livingentity.m_6844_(EquipmentSlot.CHEST), livingentity)) {
         vanillaLayer.m_117386_().m_102624_(vanillaLayer.f_116935_);
         OpenMatrix4f modelMatrix = new OpenMatrix4f();
         modelMatrix.scale(new Vec3f(-0.9F, -0.9F, 0.9F)).translate(new Vec3f(0.0F, -0.5F, -0.1F)).mulFront(poses[8]);
         poseStack.m_85836_();
         MathUtils.mulStack(poseStack, modelMatrix);
         vanillaLayer.m_6494_(
            poseStack, buffer, packedLight, livingentity, livingentity.f_267362_.m_267756_(), livingentity.f_267362_.m_267731_(), partialTicks, bob, yRot, xRot
         );
         poseStack.m_85849_();
      }
   }
}
