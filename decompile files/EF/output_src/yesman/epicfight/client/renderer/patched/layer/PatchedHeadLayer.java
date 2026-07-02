package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedHeadLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E> & HeadedModel>
   extends PatchedLayer<E, T, M, CustomHeadLayer<E, M>> {
   protected void renderLayer(
      T entitypatch,
      E entityliving,
      CustomHeadLayer<E, M> vanillaLayer,
      PoseStack postStack,
      MultiBufferSource buffer,
      int packedLightIn,
      OpenMatrix4f[] poses,
      float bob,
      float yRot,
      float xRot,
      float partialTicks
   ) {
      ItemStack itemstack = entityliving.m_6844_(EquipmentSlot.HEAD);
      if (!itemstack.m_41619_()) {
         ModelPart model = ((HeadedModel)vanillaLayer.m_117386_()).m_5585_();
         E entity = entitypatch.getOriginal();
         OpenMatrix4f modelMatrix = new OpenMatrix4f();
         modelMatrix.scale(new Vec3f(-1.0F, -1.0F, 1.0F)).mulFront(poses[9]).translate(0.0F, 0.02F, 0.0F);
         model.f_104200_ = 0.0F;
         model.f_104201_ = 0.0F;
         model.f_104202_ = 0.0F;
         model.f_104203_ = 0.0F;
         model.f_104204_ = 0.0F;
         model.f_104205_ = 0.0F;
         postStack.m_85836_();
         MathUtils.mulStack(postStack, modelMatrix);
         if (entitypatch.getOriginal().m_6162_()) {
            postStack.m_252880_(0.0F, -1.2F, 0.0F);
            postStack.m_85841_(1.6F, 1.6F, 1.6F);
         }

         vanillaLayer.m_6494_(
            postStack, buffer, packedLightIn, entity, entity.f_267362_.m_267756_(), entity.f_267362_.m_267731_(), packedLightIn, entity.f_19797_, yRot, xRot
         );
         postStack.m_85849_();
      }
   }
}
