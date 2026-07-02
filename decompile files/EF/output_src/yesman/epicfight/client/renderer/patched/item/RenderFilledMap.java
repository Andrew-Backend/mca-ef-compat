package yesman.epicfight.client.renderer.patched.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RenderFilledMap extends RenderItemBase {
   private static final RenderType MAP_BACKGROUND = RenderType.m_110497_(ResourceLocation.parse("textures/map/map_background.png"));

   public RenderFilledMap(JsonElement jsonElement) {
      super(jsonElement);
   }

   @Override
   public void renderItemInHand(
      ItemStack stack,
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
      if (hand == InteractionHand.MAIN_HAND && entitypatch.getOriginal().m_21206_().m_41619_()) {
         poseStack.m_85841_(2.0F, 2.0F, 2.0F);
      }

      itemInHandRenderer.m_109366_(poseStack, buffer, packedLight, stack);
      VertexConsumer vertexconsumer = buffer.m_6299_(MAP_BACKGROUND);
      Matrix4f matrix4f = poseStack.m_85850_().m_252922_();
      vertexconsumer.m_252986_(matrix4f, -7.0F, -7.0F, 0.0F).m_6122_(255, 255, 255, 255).m_7421_(0.0F, 0.0F).m_85969_(packedLight).m_5752_();
      vertexconsumer.m_252986_(matrix4f, 135.0F, -7.0F, 0.0F).m_6122_(255, 255, 255, 255).m_7421_(1.0F, 0.0F).m_85969_(packedLight).m_5752_();
      vertexconsumer.m_252986_(matrix4f, 135.0F, 135.0F, 0.0F).m_6122_(255, 255, 255, 255).m_7421_(1.0F, 1.0F).m_85969_(packedLight).m_5752_();
      vertexconsumer.m_252986_(matrix4f, -7.0F, 135.0F, 0.0F).m_6122_(255, 255, 255, 255).m_7421_(0.0F, 1.0F).m_85969_(packedLight).m_5752_();
      poseStack.m_85849_();
   }
}
