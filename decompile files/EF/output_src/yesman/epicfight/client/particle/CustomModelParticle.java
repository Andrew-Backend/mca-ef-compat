package yesman.epicfight.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.math.QuaternionUtils;

public abstract class CustomModelParticle<M extends Mesh> extends Particle {
   protected final AssetAccessor<M> particleMeshProvider;
   protected float pitch;
   protected float pitchO;
   protected float yaw;
   protected float yawO;
   protected float scale = 1.0F;
   protected float scaleO = 1.0F;

   public CustomModelParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, AssetAccessor<M> particleMesh) {
      super(level, x, y, z, xd, yd, zd);
      this.particleMeshProvider = particleMesh;
   }

   public void m_5744_(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
      PoseStack poseStack = new PoseStack();
      poseStack.m_85836_();
      this.setupPoseStack(poseStack, camera, partialTicks);
      this.prepareDraw(poseStack, partialTicks);
      this.particleMeshProvider
         .get()
         .draw(
            poseStack,
            vertexConsumer,
            Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP,
            this.m_6355_(partialTicks),
            this.f_107227_,
            this.f_107228_,
            this.f_107229_,
            this.f_107230_,
            OverlayTexture.f_118083_
         );
      this.revert(poseStack);
      poseStack.m_85849_();
   }

   public void m_5989_() {
      if (this.f_107224_++ >= this.f_107225_) {
         this.m_107274_();
      } else {
         this.pitchO = this.pitch;
         this.yawO = this.yaw;
         this.f_107204_ = this.f_107231_;
         this.scaleO = this.scale;
      }
   }

   public void prepareDraw(PoseStack poseStack, float partialTicks) {
   }

   protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTicks) {
      poseStack.m_85836_();
      Vec3 cameraPosition = camera.m_90583_();
      float x = (float)(Mth.m_14139_(partialTicks, this.f_107209_, this.f_107212_) - cameraPosition.m_7096_());
      float y = (float)(Mth.m_14139_(partialTicks, this.f_107210_, this.f_107213_) - cameraPosition.m_7098_());
      float z = (float)(Mth.m_14139_(partialTicks, this.f_107211_, this.f_107214_) - cameraPosition.m_7094_());
      poseStack.m_252880_(x, y, z);
      Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
      float roll = Mth.m_14179_(partialTicks, this.f_107204_, this.f_107231_);
      float pitch = Mth.m_14179_(partialTicks, this.pitchO, this.pitch);
      float yaw = Mth.m_14179_(partialTicks, this.yawO, this.yaw);
      rotation.mul(QuaternionUtils.YP.rotationDegrees(180.0F - yaw));
      rotation.mul(QuaternionUtils.XP.rotationDegrees(pitch));
      rotation.mul(QuaternionUtils.ZP.rotationDegrees(roll));
      poseStack.m_252781_(rotation);
      float scale = Mth.m_14179_(partialTicks, this.scaleO, this.scale);
      poseStack.m_85841_(scale, scale, scale);
   }

   protected void revert(PoseStack poseStack) {
      poseStack.m_85849_();
   }
}
