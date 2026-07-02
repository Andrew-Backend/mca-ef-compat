package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import org.joml.Matrix4f;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.mixin.client.MixinLivingEntityRenderer;

public class FirstPersonRenderer
   extends PatchedLivingEntityRenderer<LocalPlayer, LocalPlayerPatch, PlayerModel<LocalPlayer>, LivingEntityRenderer<LocalPlayer, PlayerModel<LocalPlayer>>, HumanoidMesh> {
   public FirstPersonRenderer(Context context, EntityType<?> entityType) {
      super(context, entityType);
      this.addPatchedLayer(ElytraLayer.class, new EmptyLayer());
      this.addPatchedLayer(PlayerItemInHandLayer.class, new PatchedItemInHandLayer());
      this.addPatchedLayer(HumanoidArmorLayer.class, new WearableItemLayer<>(Meshes.BIPED, true, context.m_266367_()));
      this.addPatchedLayer(CustomHeadLayer.class, new EmptyLayer());
      this.addPatchedLayer(ArrowLayer.class, new EmptyLayer());
      this.addPatchedLayer(BeeStingerLayer.class, new EmptyLayer());
      this.addPatchedLayer(SpinAttackEffectLayer.class, new EmptyLayer());
      this.addPatchedLayer(CapeLayer.class, new EmptyLayer());
   }

   public void render(
      LocalPlayer entity,
      LocalPlayerPatch localPlayerPatch,
      LivingEntityRenderer<LocalPlayer, PlayerModel<LocalPlayer>> renderer,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      if (localPlayerPatch.getPovSettings() != null) {
         Pose pose = localPlayerPatch.getFirstPersonLayer().getEnabledPose(localPlayerPatch, true, partialTicks);
         OpenMatrix4f[] poses = localPlayerPatch.getArmature().getPoseAsTransformMatrix(pose, false);
         poseStack.m_85836_();
         Matrix4f lastPose = new Matrix4f(poseStack.m_85850_().m_252922_());
         float standingEyeHeight = entity.m_6431_(net.minecraft.world.entity.Pose.STANDING, entity.m_6972_(net.minecraft.world.entity.Pose.STANDING));
         poseStack.m_166856_();
         if (localPlayerPatch.hasCameraAnimation()) {
            float time = Mth.m_14179_(
               partialTicks,
               localPlayerPatch.getFirstPersonLayer().animationPlayer.getPrevElapsedTime(),
               localPlayerPatch.getFirstPersonLayer().animationPlayer.getElapsedTime()
            );
            JointTransform cameraTransform;
            if (!localPlayerPatch.getFirstPersonLayer().animationPlayer.getAnimation().get().isLinkAnimation() && localPlayerPatch.getPovSettings() != null) {
               cameraTransform = localPlayerPatch.getPovSettings().cameraTransform().getInterpolatedTransform(time);
            } else {
               cameraTransform = localPlayerPatch.getFirstPersonLayer().getLinkCameraTransform().getInterpolatedTransform(time);
            }

            MathUtils.mulStack(poseStack, cameraTransform.toMatrix().invert());
         }

         switch (localPlayerPatch.getPovSettings().rootTransformation()) {
            case CAMERA:
               poseStack.m_252880_(0.0F, -standingEyeHeight, 0.0F);
               poseStack.m_252931_(lastPose);
               break;
            case WORLD:
               float yRotModel = 180.0F - Mth.m_14189_(partialTicks, entity.f_19859_, entity.m_146908_());
               float yRotWorld = 180.0F - Mth.m_14189_(partialTicks, localPlayerPatch.getYRotO(), localPlayerPatch.getYRot());
               float yRot = yRotWorld - yRotModel;
               float xRot = Mth.m_14189_(partialTicks, entity.f_19860_, entity.m_146909_());
               poseStack.m_252781_(Axis.f_252529_.m_252977_(xRot));
               poseStack.m_252781_(Axis.f_252436_.m_252977_(yRot));
               poseStack.m_252880_(0.0F, -standingEyeHeight, 0.0F);
         }

         HumanoidMesh mesh = this.getMeshProvider(localPlayerPatch).get();
         this.prepareModel(mesh, entity, localPlayerPatch, renderer);
         if (!localPlayerPatch.getOriginal().m_20145_()) {
            Map<String, Boolean> visibilities = localPlayerPatch.getPovSettings().visibilities();
            boolean defaultVisibility = localPlayerPatch.getPovSettings().visibilityOthers();

            for (Entry<String, SkinnedMesh.SkinnedMeshPart> entry : mesh.getPartEntry()) {
               if (visibilities.containsKey(entry.getKey())) {
                  entry.getValue().setHidden(!visibilities.get(entry.getKey()));
               } else {
                  entry.getValue().setHidden(!defaultVisibility);
               }
            }

            RenderType renderType = RenderType.m_110458_(entity.m_108560_());
            mesh.draw(poseStack, buffer, renderType, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.f_118083_, localPlayerPatch.getArmature(), poses);
         }

         if (!entity.m_5833_()) {
            this.renderLayer(renderer, localPlayerPatch, entity, poses, buffer, poseStack, packedLight, partialTicks);
         }

         poseStack.m_85849_();
      } else {
         Pose pose = localPlayerPatch.<Animator>getAnimator().getPose(partialTicks);
         OpenMatrix4f[] poses = localPlayerPatch.getArmature().getPoseAsTransformMatrix(pose, false);
         poseStack.m_85836_();
         Matrix4f lastPose = new Matrix4f(poseStack.m_85850_().m_252922_());
         poseStack.m_166856_();
         float correction = 0.0F;
         if (entity.m_6067_()) {
            correction = 0.25F;
         } else if (entity.m_21255_()) {
            correction = 100.0F;
         }

         float standingEyeHeight = entity.m_6431_(net.minecraft.world.entity.Pose.STANDING, entity.m_6972_(net.minecraft.world.entity.Pose.STANDING));
         poseStack.m_252880_(0.0F, -standingEyeHeight - 0.05F, correction);
         poseStack.m_252931_(lastPose);
         HumanoidMesh mesh = this.getMeshProvider(localPlayerPatch).get();
         this.prepareModel(mesh, entity, localPlayerPatch, renderer);
         if (!localPlayerPatch.getOriginal().m_20145_()) {
            for (SkinnedMesh.SkinnedMeshPart p : mesh.getAllParts()) {
               p.setHidden(true);
            }

            mesh.leftArm.setHidden(false);
            mesh.rightArm.setHidden(false);
            mesh.leftSleeve.setHidden(false);
            mesh.rightSleeve.setHidden(false);
            RenderType renderType = RenderType.m_110458_(entity.m_108560_());
            mesh.draw(poseStack, buffer, renderType, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.f_118083_, localPlayerPatch.getArmature(), poses);
         }

         if (!entity.m_5833_()) {
            this.renderLayer(renderer, localPlayerPatch, entity, poses, buffer, poseStack, packedLight, partialTicks);
         }

         poseStack.m_85849_();
      }
   }

   protected void renderLayer(
      LivingEntityRenderer<LocalPlayer, PlayerModel<LocalPlayer>> renderer,
      LocalPlayerPatch entitypatch,
      LocalPlayer entity,
      OpenMatrix4f[] poses,
      MultiBufferSource buffer,
      PoseStack poseStack,
      int packedLight,
      float partialTicks
   ) {
      Iterator<RenderLayer<LocalPlayer, PlayerModel<LocalPlayer>>> iter = renderer.f_115291_.iterator();
      float f = MathUtils.lerpBetween(entity.f_20884_, entity.f_20883_, partialTicks);
      float f1 = MathUtils.lerpBetween(entity.f_20886_, entity.f_20885_, partialTicks);
      float f2 = f1 - f;
      float f7 = entity.m_5686_(partialTicks);
      float bob = ((MixinLivingEntityRenderer)renderer).invokeGetBob(entity, partialTicks);

      while (iter.hasNext()) {
         RenderLayer<LocalPlayer, PlayerModel<LocalPlayer>> layer = iter.next();
         Class<?> rendererClass = layer.getClass();
         if (rendererClass.isAnonymousClass()) {
            rendererClass = rendererClass.getSuperclass();
         }

         if (this.patchedLayers.containsKey(rendererClass)) {
            this.patchedLayers.get(rendererClass).renderLayer(entity, entitypatch, layer, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
         }
      }
   }

   public AssetAccessor<HumanoidMesh> getMeshProvider(LocalPlayerPatch entitypatch) {
      return entitypatch.getOriginal().m_108564_().equals("slim") ? Meshes.ALEX : Meshes.BIPED;
   }

   @Override
   public AssetAccessor<HumanoidMesh> getDefaultMesh() {
      return Meshes.BIPED;
   }

   protected void prepareModel(
      HumanoidMesh mesh, LocalPlayer entity, LocalPlayerPatch entitypatch, LivingEntityRenderer<LocalPlayer, PlayerModel<LocalPlayer>> renderer
   ) {
      mesh.initialize();
      mesh.head.setHidden(true);
      mesh.hat.setHidden(true);
   }
}
