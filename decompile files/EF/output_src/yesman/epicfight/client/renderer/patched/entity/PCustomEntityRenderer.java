package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.forgeevent.PrepareModelEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PCustomEntityRenderer extends PatchedEntityRenderer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityRenderer<LivingEntity>, SkinnedMesh> {
   private final AssetAccessor<SkinnedMesh> mesh;

   public PCustomEntityRenderer(AssetAccessor<SkinnedMesh> mesh, Context context) {
      this.mesh = mesh;
   }

   @Override
   public void render(
      LivingEntity entity,
      LivingEntityPatch<LivingEntity> entitypatch,
      EntityRenderer<LivingEntity> renderer,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      super.render(entity, entitypatch, renderer, buffer, poseStack, packedLight, partialTicks);
      Minecraft mc = Minecraft.m_91087_();
      boolean isGlowing = mc.m_91314_(entity);
      ResourceLocation textureLocation = renderer.m_5478_(entity);
      RenderType renderType = isGlowing ? RenderType.m_110491_(textureLocation) : RenderType.m_110458_(textureLocation);
      Armature armature = entitypatch.getArmature();
      poseStack.m_85836_();
      this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
      this.setArmaturePose(entitypatch, armature, partialTicks);
      if (renderType != null) {
         SkinnedMesh mesh = this.mesh.get();
         PrepareModelEvent prepareModelEvent = new PrepareModelEvent(this, mesh, entitypatch, buffer, poseStack, packedLight, partialTicks);
         if (!MinecraftForge.EVENT_BUS.post(prepareModelEvent)) {
            mesh.draw(
               poseStack,
               buffer,
               renderType,
               packedLight,
               1.0F,
               1.0F,
               1.0F,
               !entity.m_20177_(mc.f_91074_) ? 0.15F : 1.0F,
               this.getOverlayCoord(entity, entitypatch, partialTicks),
               armature,
               armature.getPoseMatrices()
            );
         }
      }

      if (Minecraft.m_91087_().m_91290_().m_114377_()) {
         entitypatch.getClientAnimator().renderDebuggingInfoForAllLayers(poseStack, buffer, partialTicks);
      }

      poseStack.m_85849_();
   }

   protected int getOverlayCoord(LivingEntity entity, LivingEntityPatch<LivingEntity> entitypatch, float partialTicks) {
      return OverlayTexture.m_118093_(0, OverlayTexture.m_118096_(entity.f_20916_ > 5));
   }

   @Override
   public AssetAccessor<SkinnedMesh> getDefaultMesh() {
      return this.mesh;
   }
}
