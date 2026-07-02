package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.mesh.WitherMesh;
import yesman.epicfight.client.renderer.entity.WitherGhostRenderer;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherGhostPatch;
import yesman.epicfight.world.entity.WitherGhostClone;

public class WitherGhostCloneRenderer extends PatchedEntityRenderer<WitherGhostClone, WitherGhostPatch, WitherGhostRenderer, WitherMesh> {
   public void render(
      WitherGhostClone entity,
      WitherGhostPatch entitypatch,
      WitherGhostRenderer renderer,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      RenderType renderType = RenderType.m_110473_(PWitherRenderer.WITHER_INVULNERABLE_LOCATION);
      WitherMesh mesh = this.getMeshProvider(entitypatch).get();
      Armature armature = entitypatch.getArmature();
      float tranparency = entity.m_21525_() ? 0.6F : Mth.m_14031_((entity.f_19797_ + partialTicks) * 0.025F * (float) Math.PI) * 0.6F;
      poseStack.m_85836_();
      this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
      this.setArmaturePose(entitypatch, armature, partialTicks);
      mesh.draw(
         poseStack,
         buffer,
         renderType,
         packedLight,
         1.0F,
         1.0F,
         1.0F,
         tranparency,
         OverlayTexture.f_118083_,
         entitypatch.getArmature(),
         armature.getPoseMatrices()
      );
      if (Minecraft.m_91087_().m_91290_().m_114377_()) {
         for (Layer.Priority priority : Layer.Priority.values()) {
            AnimationPlayer animPlayer = entitypatch.getClientAnimator().getCompositeLayer(priority).animationPlayer;
            float playTime = animPlayer.getPrevElapsedTime() + (animPlayer.getElapsedTime() - animPlayer.getPrevElapsedTime()) * partialTicks;
            animPlayer.getAnimation().get().renderDebugging(poseStack, buffer, entitypatch, playTime, partialTicks);
         }
      }

      poseStack.m_85849_();
   }

   @Override
   public AssetAccessor<WitherMesh> getDefaultMesh() {
      return Meshes.WITHER;
   }
}
