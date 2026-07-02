package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.projectile.ThrownTridentPatch;

@Mixin(ThrownTridentRenderer.class)
public abstract class MixinThrownTridentRenderer extends EntityRenderer<ThrownTrident> {
   protected MixinThrownTridentRenderer(Context p_174008_) {
      super(p_174008_);
   }

   @Inject(
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 0),
      method = "render(Lnet/minecraft/world/entity/projectile/ThrownTrident;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
      cancellable = true
   )
   private void epicfight_render(
      ThrownTrident tridentEntity, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiSourceBuffer, int packedLight, CallbackInfo info
   ) {
      ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(tridentEntity, ThrownTridentPatch.class);
      if (tridentPatch != null && tridentPatch.isInnateActivated()) {
         Entity owner = tridentEntity.m_19749_();
         Vec3 toOwner = owner.m_20182_().m_82546_(tridentEntity.m_20182_());
         Vec3 toOwnerHorizontalNorm = owner.m_20182_().m_82546_(tridentEntity.m_20182_()).m_82492_(0.0, toOwner.f_82480_, 0.0).m_82541_();
         Vec3 toOwnerNorm = toOwner.m_82541_();
         Vec3 rotAxis = toOwnerHorizontalNorm.m_82537_(toOwnerNorm).m_82541_();
         float deg = (float)MathUtils.getAngleBetween(toOwnerNorm, toOwnerHorizontalNorm);
         poseStack.m_252781_(QuaternionUtils.rotationDegrees(new Vector3f((float)rotAxis.f_82479_, (float)rotAxis.f_82480_, (float)rotAxis.f_82481_), deg));
         poseStack.m_252781_(QuaternionUtils.XP.rotationDegrees(90.0F));
         poseStack.m_252781_(QuaternionUtils.ZP.rotationDegrees(Mth.m_14179_(partialTicks, tridentEntity.f_19860_, tridentEntity.m_146909_()) + 90.0F));
         poseStack.m_85837_(0.0, -0.8, -0.0);
         tridentPatch.renderXRot = tridentEntity.m_146909_();
         tridentPatch.renderXRotO = tridentEntity.f_19860_;
         tridentPatch.renderYRot = tridentEntity.m_146908_();
         tridentPatch.renderYRotO = tridentEntity.f_19859_;
         tridentEntity.f_19860_ = -90.0F;
         tridentEntity.f_19859_ = 90.0F;
         tridentEntity.m_146926_(-90.0F);
         tridentEntity.m_146922_(90.0F);
      }
   }

   @Inject(
      at = @At("TAIL"),
      method = "render(Lnet/minecraft/world/entity/projectile/ThrownTrident;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
      cancellable = true
   )
   private void epicfight_renderPost(
      ThrownTrident tridentEntity, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiSourceBuffer, int packedLight, CallbackInfo info
   ) {
      ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(tridentEntity, ThrownTridentPatch.class);
      if (tridentPatch != null && tridentPatch.isInnateActivated()) {
         tridentEntity.f_19860_ = tridentPatch.renderXRotO;
         tridentEntity.f_19859_ = tridentPatch.renderYRotO;
         tridentEntity.m_146926_(tridentPatch.renderXRot);
         tridentEntity.m_146922_(tridentPatch.renderYRot);
      }
   }
}
