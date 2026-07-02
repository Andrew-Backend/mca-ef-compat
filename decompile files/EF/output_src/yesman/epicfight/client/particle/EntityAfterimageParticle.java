package yesman.epicfight.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EntityAfterimageParticle extends CustomModelParticle<SkinnedMesh> {
   protected final EntitySnapshot<?> entitySnapshot;
   protected final Consumer<EntityAfterimageParticle> ticktask;
   protected float alphaO;

   public EntityAfterimageParticle(
      ClientLevel level,
      double x,
      double y,
      double z,
      double xd,
      double yd,
      double zd,
      EntitySnapshot<?> entitySnapshot,
      Consumer<EntityAfterimageParticle> ticktask
   ) {
      super(level, x, y, z, xd, yd, zd, (AssetAccessor<SkinnedMesh>)null);
      this.entitySnapshot = entitySnapshot;
      this.ticktask = ticktask;
      this.f_107227_ = 1.0F;
      this.f_107228_ = 1.0F;
      this.f_107229_ = 1.0F;
      this.alphaO = 1.0F;
      this.f_107230_ = 1.0F;
      this.yawO = entitySnapshot.getYRot();
      this.yaw = entitySnapshot.getYRot();
   }

   @Override
   public void m_5989_() {
      super.m_5989_();
      this.alphaO = this.f_107230_;
      this.ticktask.accept(this);
   }

   @Override
   public void m_5744_(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
      float alpha = Mth.m_14179_(partialTicks, this.alphaO, this.f_107230_);
      int lightColor = this.m_6355_(partialTicks);
      PoseStack poseStack = new PoseStack();
      this.setupPoseStack(poseStack, camera, partialTicks);
      BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
      this.entitySnapshot
         .renderTextured(poseStack, buffers, EpicFightRenderTypes::entityAfterimageStencil, Mesh.DrawingFunction.POSITION_TEX, 0, 0.0F, 0.0F, 0.0F, 1.0F);
      this.entitySnapshot.renderItems(poseStack, buffers, EpicFightRenderTypes.itemAfterimageStencil(), Mesh.DrawingFunction.POSITION_TEX, lightColor, 1.0F);
      buffers.m_173043_();
      this.entitySnapshot
         .renderTextured(
            poseStack,
            buffers,
            EpicFightRenderTypes::entityAfterimageTranslucent,
            Mesh.DrawingFunction.NEW_ENTITY,
            lightColor,
            this.f_107227_,
            this.f_107228_,
            this.f_107229_,
            alpha
         );
      this.entitySnapshot.renderItems(poseStack, buffers, EpicFightRenderTypes.itemAfterimageTranslucent(), Mesh.DrawingFunction.NEW_ENTITY, lightColor, alpha);
      buffers.m_173043_();
      this.revert(poseStack);
   }

   public ParticleRenderType m_7556_() {
      return EpicFightParticleRenderTypes.ENTITY_PARTICLE;
   }

   @Override
   protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTick) {
      poseStack.m_85836_();
      poseStack.m_252931_(RenderSystem.getModelViewStack().m_85850_().m_252922_());
      RenderSystem.getModelViewStack().m_85836_();
      RenderSystem.getModelViewStack().m_166856_();
      RenderSystem.applyModelViewMatrix();
      Vec3 cameraPosition = camera.m_90583_();
      float x = (float)(Mth.m_14139_(partialTick, this.f_107209_, this.f_107212_) - cameraPosition.m_7096_());
      float y = (float)(Mth.m_14139_(partialTick, this.f_107210_, this.f_107213_) - cameraPosition.m_7098_());
      float z = (float)(Mth.m_14139_(partialTick, this.f_107211_, this.f_107214_) - cameraPosition.m_7094_());
      poseStack.m_252880_(x, y, z);
      Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
      rotation.mul(QuaternionUtils.YP.rotationDegrees(180.0F));
      poseStack.m_252781_(rotation);
      poseStack.m_252931_(OpenMatrix4f.exportToMojangMatrix(this.entitySnapshot.getModelMatrix()));
      float scale = Mth.m_14179_(partialTick, this.scaleO, this.scale);
      poseStack.m_252880_(0.0F, this.entitySnapshot.getHeightHalf(), 0.0F);
      poseStack.m_85841_(scale, scale, scale);
      poseStack.m_252880_(0.0F, -this.entitySnapshot.getHeightHalf(), 0.0F);
   }

   @Override
   protected void revert(PoseStack poseStack) {
      poseStack.m_85849_();
      RenderSystem.getModelViewStack().m_85849_();
      RenderSystem.applyModelViewMatrix();
   }

   public static class AdrenalineParticleProvider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Entity entity = level.m_6815_((int)Double.doubleToLongBits(xSpeed));
         LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
         if (entitypatch != null) {
            EntitySnapshot<?> entitySnapshot = entitypatch.captureEntitySnapshot();
            if (entitySnapshot != null) {
               EntityAfterimageParticle adrenalineparticle = new EntityAfterimageParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, entitySnapshot, particle -> {
                  particle.f_107230_ -= 0.025F;
                  particle.scale = particle.scale + (-0.0025F * particle.f_107224_ * particle.f_107224_ + 1.0F) * 0.1F;
               });
               adrenalineparticle.m_107257_(20);
               adrenalineparticle.m_107271_(0.6F);
               return adrenalineparticle;
            }
         }

         return null;
      }
   }

   public static class WhiteAfterimageParticle extends EntityAfterimageParticle {
      public WhiteAfterimageParticle(
         ClientLevel level,
         double x,
         double y,
         double z,
         double xd,
         double yd,
         double zd,
         EntitySnapshot<?> entitySnapshot,
         Consumer<EntityAfterimageParticle> ticktask
      ) {
         super(level, x, y, z, xd, yd, zd, entitySnapshot, ticktask);
      }

      @Override
      public void m_5744_(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
         float alpha = Mth.m_14179_(partialTicks, this.alphaO, this.f_107230_);
         int lightColor = this.m_6355_(partialTicks);
         PoseStack poseStack = new PoseStack();
         this.setupPoseStack(poseStack, camera, partialTicks);
         BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
         this.entitySnapshot
            .renderTextured(poseStack, buffers, EpicFightRenderTypes::entityAfterimageStencil, Mesh.DrawingFunction.POSITION_TEX, 0, 0.0F, 0.0F, 0.0F, 1.0F);
         this.entitySnapshot.renderItems(poseStack, buffers, EpicFightRenderTypes.itemAfterimageStencil(), Mesh.DrawingFunction.POSITION_TEX, lightColor, 1.0F);
         buffers.m_173043_();
         this.entitySnapshot
            .render(
               poseStack,
               buffers,
               EpicFightRenderTypes.entityAfterimageWhite(),
               Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP,
               lightColor,
               this.f_107227_,
               this.f_107228_,
               this.f_107229_,
               alpha
            );
         this.entitySnapshot
            .renderItems(poseStack, buffers, EpicFightRenderTypes.itemAfterimageWhite(), Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP, lightColor, alpha);
         buffers.m_173043_();
         this.revert(poseStack);
      }
   }

   public static class WhiteAfterimageProvider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Entity entity = level.m_6815_((int)Double.doubleToLongBits(xSpeed));
         LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
         if (entitypatch != null) {
            EntitySnapshot<?> entitySnapshot = entitypatch.captureEntitySnapshot();
            if (entitySnapshot != null) {
               EntityAfterimageParticle.WhiteAfterimageParticle afterimage = new EntityAfterimageParticle.WhiteAfterimageParticle(
                  level,
                  x,
                  y,
                  z,
                  xSpeed,
                  ySpeed,
                  zSpeed,
                  entitySnapshot,
                  particle -> particle.f_107230_ = (float)(particle.f_107225_ - particle.f_107224_) / particle.f_107225_
               );
               afterimage.m_107257_(20);
               return afterimage;
            }
         }

         return null;
      }
   }
}
