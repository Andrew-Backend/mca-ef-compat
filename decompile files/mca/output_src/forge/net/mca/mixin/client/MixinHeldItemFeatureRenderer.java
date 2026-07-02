package forge.net.mca.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.entity.VillagerLike;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class MixinHeldItemFeatureRenderer {
   @Shadow
   protected abstract void m_117184_(
      LivingEntity var1, ItemStack var2, ItemDisplayContext var3, HumanoidArm var4, PoseStack var5, MultiBufferSource var6, int var7
   );

   @Inject(
      method = "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void render(
      PoseStack matrixStack,
      MultiBufferSource vertexConsumerProvider,
      int i,
      LivingEntity livingEntity,
      float f,
      float g,
      float h,
      float j,
      float k,
      float l,
      CallbackInfo ci
   ) {
      if (livingEntity instanceof VillagerLike) {
         boolean bl = livingEntity.m_5737_() == HumanoidArm.RIGHT;
         ItemStack itemStack = bl ? livingEntity.m_21206_() : livingEntity.m_21205_();
         ItemStack itemStack2 = bl ? livingEntity.m_21205_() : livingEntity.m_21206_();
         if (!itemStack.m_41619_() || !itemStack2.m_41619_()) {
            matrixStack.m_85836_();
            this.m_117184_(livingEntity, itemStack2, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStack, vertexConsumerProvider, i);
            this.m_117184_(livingEntity, itemStack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStack, vertexConsumerProvider, i);
            matrixStack.m_85849_();
         }

         ci.cancel();
      }
   }
}
