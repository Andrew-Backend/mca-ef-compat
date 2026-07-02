package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.mesh.WitherMesh;
import yesman.epicfight.client.renderer.patched.layer.PatchedWitherArmorLayer;
import yesman.epicfight.mixin.client.MixinLivingEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;

public class PWitherRenderer extends PatchedLivingEntityRenderer<WitherBoss, WitherPatch, WitherBossModel<WitherBoss>, WitherBossRenderer, WitherMesh> {
   public static final ResourceLocation WITHER_INVULNERABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_invulnerable.png");
   private static final ResourceLocation WITHER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither.png");

   public PWitherRenderer(Context context, EntityType<?> entityType) {
      super(context, entityType);
      this.addPatchedLayer(WitherArmorLayer.class, new PatchedWitherArmorLayer());
   }

   public void render(
      WitherBoss entity,
      WitherPatch entitypatch,
      WitherBossRenderer renderer,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      Minecraft mc = Minecraft.m_91087_();
      MixinLivingEntityRenderer livingEntityRendererAccessor = (MixinLivingEntityRenderer)renderer;
      boolean isVisible = this.isVisible(entity, entitypatch);
      boolean isVisibleToPlayer = !isVisible && !entity.m_20177_(mc.f_91074_);
      boolean isGlowing = mc.m_91314_(entity);
      RenderType renderType = livingEntityRendererAccessor.invokeGetRenderType(entity, isVisible, isVisibleToPlayer, isGlowing);
      WitherMesh mesh = this.getMeshProvider(entitypatch).get();
      Armature armature = entitypatch.getArmature();
      poseStack.m_85836_();
      this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
      this.setArmaturePose(entitypatch, armature, partialTicks);
      if (renderType != null) {
         int transparencyCount = entitypatch.getTransparency();
         if (transparencyCount == 0) {
            if (!entitypatch.isGhost()) {
               mesh.draw(
                  poseStack,
                  buffer,
                  renderType,
                  packedLight,
                  1.0F,
                  1.0F,
                  1.0F,
                  1.0F,
                  this.getOverlayCoord(entity, entitypatch, partialTicks),
                  entitypatch.getArmature(),
                  armature.getPoseMatrices()
               );
            }
         } else {
            float transparency = (Math.abs(transparencyCount) + partialTicks) / 41.0F;
            if (transparencyCount < 0) {
               transparency = 1.0F - transparency;
            }

            mesh.draw(
               poseStack,
               buffer,
               RenderType.m_110473_(WITHER_LOCATION),
               packedLight,
               1.0F,
               1.0F,
               1.0F,
               transparency,
               OverlayTexture.f_118083_,
               entitypatch.getArmature(),
               armature.getPoseMatrices()
            );
            mesh.draw(
               poseStack,
               buffer,
               RenderType.m_110473_(WITHER_INVULNERABLE_LOCATION),
               packedLight,
               1.0F,
               1.0F,
               1.0F,
               Mth.m_14031_(transparency * 3.1415F),
               OverlayTexture.f_118083_,
               entitypatch.getArmature(),
               armature.getPoseMatrices()
            );
         }

         this.renderLayer(renderer, entitypatch, entity, armature.getPoseMatrices(), buffer, poseStack, packedLight, partialTicks);
         if (Minecraft.m_91087_().m_91290_().m_114377_()) {
            entitypatch.getClientAnimator().renderDebuggingInfoForAllLayers(poseStack, buffer, partialTicks);
         }
      }

      poseStack.m_85849_();
   }

   protected boolean isVisible(WitherBoss witherboss, WitherPatch witherpatch) {
      return !witherpatch.isGhost() || witherpatch.getTransparency() != 0;
   }

   public void mulPoseStack(PoseStack poseStack, Armature armature, WitherBoss witherboss, WitherPatch entitypatch, float partialTicks) {
      super.mulPoseStack(poseStack, armature, witherboss, entitypatch, partialTicks);
      float f = 1.0F;
      int i = witherboss.m_31502_();
      if (i > 0) {
         f -= (i - partialTicks) / 440.0F;
      }

      poseStack.m_85841_(f, f, f);
   }

   @Override
   public AssetAccessor<WitherMesh> getDefaultMesh() {
      return Meshes.WITHER;
   }
}
