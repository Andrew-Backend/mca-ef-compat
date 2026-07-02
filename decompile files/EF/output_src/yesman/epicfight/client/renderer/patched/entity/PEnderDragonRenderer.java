package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.mesh.DragonMesh;
import yesman.epicfight.client.renderer.LightningRenderHelper;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.DragonCrystalLinkPhase;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.PatchedPhases;

public class PEnderDragonRenderer extends PatchedEntityRenderer<EnderDragon, EnderDragonPatch, EnderDragonRenderer, DragonMesh> {
   private static final ResourceLocation DRAGON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
   private static final ResourceLocation DRAGON_EXPLODING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");

   public void render(
      EnderDragon entityIn,
      EnderDragonPatch entitypatch,
      EnderDragonRenderer renderer,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      DragonMesh mesh = this.getMeshProvider(entitypatch).get();
      Armature armature = entitypatch.getArmature();
      poseStack.m_85836_();
      this.mulPoseStack(poseStack, armature, entityIn, entitypatch, partialTicks);
      this.setArmaturePose(entitypatch, armature, partialTicks);
      armature.getPoseMatrices()[0] = OpenMatrix4f.rotate(-90.0F, Vec3f.X_AXIS, armature.getPoseMatrices()[0], null);
      if (entityIn.f_31084_ > 0) {
         poseStack.m_85837_(entityIn.m_217043_().m_188583_() * 0.08, 0.0, entityIn.m_217043_().m_188583_() * 0.08);
         float deathTimeProgression = (entityIn.f_31084_ + partialTicks) / 200.0F;
         mesh.draw(
            poseStack,
            buffer,
            RenderType.m_173235_(DRAGON_EXPLODING_LOCATION),
            packedLight,
            1.0F,
            1.0F,
            1.0F,
            deathTimeProgression,
            OverlayTexture.f_118083_,
            entitypatch.getArmature(),
            armature.getPoseMatrices()
         );
         mesh.draw(
            poseStack,
            buffer,
            RenderType.m_110479_(DRAGON_LOCATION),
            packedLight,
            1.0F,
            1.0F,
            1.0F,
            1.0F,
            this.getOverlayCoord(entityIn, entitypatch, partialTicks),
            entitypatch.getArmature(),
            armature.getPoseMatrices()
         );
      } else {
         mesh.draw(
            poseStack,
            buffer,
            RenderType.m_110458_(DRAGON_LOCATION),
            packedLight,
            1.0F,
            1.0F,
            1.0F,
            1.0F,
            this.getOverlayCoord(entityIn, entitypatch, partialTicks),
            entitypatch.getArmature(),
            armature.getPoseMatrices()
         );
      }

      if (Minecraft.m_91087_().m_91290_().m_114377_()) {
         entitypatch.getClientAnimator().renderDebuggingInfoForAllLayers(poseStack, buffer, partialTicks);
      }

      poseStack.m_85849_();
      if (entityIn.f_31086_ != null) {
         float x = (float)(entityIn.f_31086_.m_20185_() - Mth.m_14139_(partialTicks, entityIn.f_19854_, entityIn.m_20185_()));
         float y = (float)(entityIn.f_31086_.m_20186_() - Mth.m_14139_(partialTicks, entityIn.f_19855_, entityIn.m_20186_()));
         float z = (float)(entityIn.f_31086_.m_20189_() - Mth.m_14139_(partialTicks, entityIn.f_19856_, entityIn.m_20189_()));
         poseStack.m_85836_();
         EnderDragonRenderer.m_114187_(
            x, y + EndCrystalRenderer.m_114158_(entityIn.f_31086_, partialTicks), z, partialTicks, entityIn.f_19797_, poseStack, buffer, packedLight
         );
         poseStack.m_85849_();
      }

      if (entityIn.f_31084_ > 0) {
         float deathTimeProgression = (entityIn.f_31084_ + partialTicks) / 200.0F;
         VertexConsumer lightningBuffer = buffer.m_6299_(RenderType.m_110502_());
         int density = (int)((deathTimeProgression + deathTimeProgression * deathTimeProgression) / 2.0F * 60.0F);
         float f7 = Math.min(deathTimeProgression > 0.8F ? (deathTimeProgression - 0.8F) / 0.2F : 0.0F, 1.0F);
         poseStack.m_85836_();
         LightningRenderHelper.renderCyclingLight(lightningBuffer, poseStack, 255, 0, 255, density, 1.0F, deathTimeProgression, f7);
         poseStack.m_85849_();
      }
   }

   public void mulPoseStack(PoseStack matStack, Armature armature, EnderDragon entityIn, EnderDragonPatch entitypatch, float partialTicks) {
      OpenMatrix4f modelMatrix;
      if (entitypatch.isGroundPhase() && entitypatch.getOriginal().f_31084_ <= 0) {
         modelMatrix = entitypatch.getModelMatrix(partialTicks).scale(-1.0F, 1.0F, -1.0F);
      } else {
         float f = (float)entityIn.m_31101_(7, partialTicks)[0];
         float f1 = (float)(entityIn.m_31101_(5, partialTicks)[1] - entityIn.m_31101_(10, partialTicks)[1]);
         float f2 = entitypatch.getOriginal().f_31084_ > 0
            ? 0.0F
            : MathUtils.rotWrap(entityIn.m_31101_(5, partialTicks)[0] - entityIn.m_31101_(10, partialTicks)[0]);
         modelMatrix = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f1, f1, f, f, partialTicks, 1.0F, 1.0F, 1.0F)
            .rotateDeg(-f2 * 1.5F, Vec3f.Z_AXIS);
      }

      MathUtils.mulStack(matStack, modelMatrix);
   }

   protected int getOverlayCoord(EnderDragon entity, EnderDragonPatch entitypatch, float partialTicks) {
      DragonPhaseInstance currentPhase = entity.m_31157_().m_31415_();
      float chargingTick = 158.0F;
      float progression = currentPhase.m_7309_() == PatchedPhases.CRYSTAL_LINK
         ? (chargingTick - ((DragonCrystalLinkPhase)currentPhase).getChargingCount()) / chargingTick
         : 0.0F;
      return OverlayTexture.m_118093_(OverlayTexture.m_118088_(progression), OverlayTexture.m_118096_(entity.f_20916_ > 5 || entity.f_20919_ > 0));
   }

   @Override
   public AssetAccessor<DragonMesh> getDefaultMesh() {
      return Meshes.DRAGON;
   }
}
