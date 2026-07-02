package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.online.EpicSkins;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.Armatures;

public class PatchedCapeLayer
   extends PatchedLayer<AbstractClientPlayer, AbstractClientPlayerPatch<AbstractClientPlayer>, PlayerModel<AbstractClientPlayer>, CapeLayer> {
   protected void renderLayer(
      AbstractClientPlayerPatch<AbstractClientPlayer> entitypatch,
      AbstractClientPlayer entityliving,
      CapeLayer vanillaLayer,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight,
      OpenMatrix4f[] poses,
      float bob,
      float yRot,
      float xRot,
      float partialTick
   ) {
      if (ClientConfig.enableCosmetics) {
         if (Minecraft.m_91087_().f_91080_ instanceof EffectRenderingInventoryScreen && entityliving == Minecraft.m_91087_().f_91074_ && partialTick == 1.0F) {
            return;
         }

         entitypatch.getClothSimulator()
            .getRunningObject(ClothSimulator.PLAYER_CLOAK)
            .ifPresent(
               clothObj -> {
                  ResourceLocation capeTexture = entitypatch.isEpicSkinsLoaded()
                     ? entitypatch.getEpicSkinsInformation().cloakTexture().get()
                     : entityliving.m_108561_();
                  if (capeTexture != null) {
                     Function<Float, OpenMatrix4f> partialColliderTransformProvider = partialFrame -> {
                        Vec3 pos = entitypatch.getOriginal().m_20318_(partialFrame);
                        float yRotLerp = Mth.m_14189_(partialFrame, entitypatch.getYRotO(), entitypatch.getYRot());
                        return OpenMatrix4f.createTranslation((float)pos.f_82479_, (float)pos.f_82480_, (float)pos.f_82481_)
                           .rotateDeg(180.0F - yRotLerp, Vec3f.Y_AXIS);
                     };
                     clothObj.tick(entitypatch, partialColliderTransformProvider, partialTick, entitypatch.getArmature(), poses);
                     double entityX = Mth.m_14139_(partialTick, entityliving.f_19790_, entityliving.m_20185_());
                     double entityY = Mth.m_14139_(partialTick, entityliving.f_19791_, entityliving.m_20186_());
                     double entityZ = Mth.m_14139_(partialTick, entityliving.f_19792_, entityliving.m_20189_());
                     PoseStack posestack$2 = new PoseStack();
                     PatchedEntityRenderer renderer = ClientEngine.getInstance().renderEngine.getEntityRenderer(EntityType.f_20532_);
                     renderer.mulPoseStack(posestack$2, entitypatch.getArmature(), (LivingEntity)entitypatch.getOriginal(), entitypatch, partialTick);
                     Matrix4f renderLocalPose = posestack$2.m_85850_().m_252922_();
                     float bodyYRot = Mth.m_14189_(partialTick, entitypatch.getYRotO(), entitypatch.getYRot());
                     if (entitypatch.isEpicSkinsLoaded()) {
                        EpicSkins epicskinsInfo = entitypatch.getEpicSkinsInformation();
                        renderSimulatingCape(
                           poseStack,
                           buffer,
                           RenderType.m_110458_(capeTexture),
                           Mesh.DrawingFunction.NEW_ENTITY,
                           clothObj,
                           entityX,
                           entityY,
                           entityZ,
                           epicskinsInfo.r(),
                           epicskinsInfo.g(),
                           epicskinsInfo.b(),
                           1.0F,
                           entitypatch,
                           poses,
                           packedLight,
                           renderLocalPose,
                           bodyYRot
                        );
                     } else {
                        renderSimulatingCape(
                           poseStack,
                           buffer,
                           RenderType.m_110458_(capeTexture),
                           Mesh.DrawingFunction.NEW_ENTITY,
                           clothObj,
                           entityX,
                           entityY,
                           entityZ,
                           1.0F,
                           1.0F,
                           1.0F,
                           1.0F,
                           entitypatch,
                           poses,
                           packedLight,
                           renderLocalPose,
                           bodyYRot
                        );
                     }
                  }
               }
            );
      } else if (entityliving.m_108555_() && !entityliving.m_20145_() && entityliving.m_36170_(PlayerModelPart.CAPE) && entityliving.m_108561_() != null) {
         ItemStack itemstack = entityliving.m_6844_(EquipmentSlot.CHEST);
         if (itemstack.m_41720_() != Items.f_42741_) {
            OpenMatrix4f modelMatrix = new OpenMatrix4f();
            modelMatrix.scale(new Vec3f(-1.0F, -1.0F, 1.0F)).mulFront(poses[8]);
            poseStack.m_85836_();
            MathUtils.mulStack(poseStack, modelMatrix);
            poseStack.m_85837_(0.0, -0.4, -0.025);
            vanillaLayer.m_6494_(
               poseStack,
               buffer,
               packedLight,
               entityliving,
               entityliving.f_267362_.m_267756_(),
               entityliving.f_267362_.m_267731_(),
               partialTick,
               entityliving.f_19797_,
               yRot,
               xRot
            );
            poseStack.m_85849_();
         }
      }
   }

   public static void renderSimulatingCape(
      PoseStack poseStack,
      MultiBufferSource buffers,
      RenderType rendertype,
      Mesh.DrawingFunction drawFunction,
      ClothSimulator.ClothObject clothObj,
      double x,
      double y,
      double z,
      float r,
      float g,
      float b,
      float a,
      AbstractClientPlayerPatch<?> entitypatch,
      OpenMatrix4f[] poses,
      int packedLight,
      Matrix4f renderLocalMatrix,
      float yBodyRot
   ) {
      poseStack.m_85836_();
      float scaler = entitypatch.getScale();
      poseStack.m_85841_(scaler, scaler, scaler);
      if (entitypatch.getOriginal().m_21033_(EquipmentSlot.CHEST)) {
         OpenMatrix4f poseMat = poses[Armatures.BIPED.get().chest.getId()];
         poseStack.m_252880_(poseMat.m30, poseMat.m31, poseMat.m32);
         poseStack.m_85841_(1.17F, 1.17F, 1.17F);
         poseStack.m_252880_(-poseMat.m30, -poseMat.m31, -poseMat.m32);
      }

      clothObj.scaleFromPose(poseStack, poses);
      poseStack.m_85836_();
      renderLocalMatrix = renderLocalMatrix.invert(new Matrix4f());
      poseStack.m_252931_(renderLocalMatrix);
      poseStack.m_85837_(-renderLocalMatrix.m30() - x, -renderLocalMatrix.m31() - y, -renderLocalMatrix.m32() - z);
      poseStack.m_85850_().m_252943_().rotate(QuaternionUtils.YP.rotationDegrees(yBodyRot));
      clothObj.drawPosed(
         poseStack,
         buffers.m_6299_(EpicFightRenderTypes.getTriangulated(rendertype)),
         drawFunction,
         packedLight,
         r,
         g,
         b,
         a,
         OverlayTexture.f_118083_,
         entitypatch.getArmature(),
         poses
      );
      poseStack.m_85849_();
      poseStack.m_85849_();
   }
}
