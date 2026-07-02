package yesman.epicfight.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import yesman.epicfight.client.renderer.LightningRenderHelper;

public class DroppedNetherStarRenderer extends ItemEntityRenderer {
   public DroppedNetherStarRenderer(Context context) {
      super(context);
   }

   public void m_7392_(ItemEntity entityIn, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
      super.m_7392_(entityIn, yRot, partialTicks, poseStack, multiBufferSource, packedLight);
      poseStack.m_85836_();
      poseStack.m_85837_(0.0, entityIn.m_20206_() + Mth.m_14031_((entityIn.m_32059_() + partialTicks) / 10.0F + entityIn.f_31983_) * 0.1F + 0.1F, 0.0);
      VertexConsumer vertexBuilder = multiBufferSource.m_6299_(RenderType.m_110502_());
      float progression = (entityIn.f_19797_ + partialTicks) * 0.01F;
      float repeater = ((float)Math.sin(progression * 5.0F) + 1.0F) * 0.5F;
      LightningRenderHelper.renderCyclingLight(vertexBuilder, poseStack, 32, 0, 255, 9, 0.05F, progression, repeater);
      poseStack.m_85849_();
   }
}
