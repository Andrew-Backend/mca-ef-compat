package yesman.epicfight.client.renderer.patched.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RenderShield extends RenderItemBase {
   public RenderShield(JsonElement jsonElement) {
      super(jsonElement);
   }

   @Override
   public void renderItemInHand(
      ItemStack itemstack,
      LivingEntityPatch<?> entitypatch,
      InteractionHand hand,
      OpenMatrix4f[] poses,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, hand, poses);
      poseStack.m_85836_();
      MathUtils.mulStack(poseStack, modelMatrix);
      ItemDisplayContext transformType = hand == InteractionHand.MAIN_HAND
         ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
         : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
      BakedModel model = itemRenderer.m_115103_().m_109406_(itemstack);
      itemRenderer.m_115143_(itemstack, transformType, hand != InteractionHand.MAIN_HAND, poseStack, buffer, packedLight, OverlayTexture.f_118083_, model);
      poseStack.m_85849_();
   }
}
