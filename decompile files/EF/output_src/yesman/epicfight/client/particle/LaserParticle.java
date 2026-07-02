package yesman.epicfight.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.client.model.ClassicMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.QuaternionUtils;

public class LaserParticle extends CustomModelParticle<ClassicMesh> {
   private final float length;
   private final float xRot;
   private final float yRot;

   public LaserParticle(ClientLevel level, double x, double y, double z, double toX, double toY, double toZ) {
      super(level, x, y, z, 0.0, 0.0, 0.0, Meshes.LASER);
      this.f_107225_ = 5;
      Vec3 direction = new Vec3(toX - x, toY - y, toZ - z);
      Vec3 start = new Vec3(x, y, z);
      Vec3 destination = start.m_82549_(direction.m_82541_().m_82490_(200.0));
      BlockHitResult hitResult = level.m_45547_(new ClipContext(start, destination, Block.COLLIDER, Fluid.NONE, null));
      double xLength = hitResult.m_82450_().f_82479_ - x;
      double yLength = hitResult.m_82450_().f_82480_ - y;
      double zLength = hitResult.m_82450_().f_82481_ - z;
      double horizontalDistance = (float)Math.sqrt(xLength * xLength + zLength * zLength);
      this.length = (float)Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
      this.yRot = (float)(Math.atan2(zLength, xLength) * (180.0 / Math.PI)) + 90.0F + 180.0F;
      this.xRot = (float)(Math.atan2(yLength, horizontalDistance) * (180.0 / Math.PI));
      int smokeCount = (int)this.length * 4;

      for (int i = 0; i < smokeCount; i++) {
         level.m_7106_(ParticleTypes.f_123762_, x + xLength / smokeCount * i, y + yLength / smokeCount * i, z + zLength / smokeCount * i, 0.0, 0.0, 0.0);
      }

      this.m_107259_(new AABB(x, y, z, toX, toY, toZ));
   }

   @Override
   protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTick) {
      poseStack.m_85836_();
      Vec3 cameraPosition = camera.m_90583_();
      float x = (float)(Mth.m_14139_(partialTick, this.f_107209_, this.f_107212_) - cameraPosition.m_7096_());
      float y = (float)(Mth.m_14139_(partialTick, this.f_107210_, this.f_107213_) - cameraPosition.m_7098_());
      float z = (float)(Mth.m_14139_(partialTick, this.f_107211_, this.f_107214_) - cameraPosition.m_7094_());
      poseStack.m_252880_(x, y, z);
      poseStack.m_252781_(QuaternionUtils.YP.rotationDegrees(180.0F - this.yRot));
      poseStack.m_252781_(QuaternionUtils.XP.rotationDegrees(this.xRot));
      float progression = (this.f_107224_ + partialTick) / (this.f_107225_ + 1);
      float scale = Mth.m_14031_(progression * (float) Math.PI);
      float zScale = progression > 0.5F ? 1.0F : Mth.m_14031_(progression * (float) Math.PI);
      poseStack.m_85841_(scale, scale, zScale * this.length);
   }

   public ParticleRenderType m_7556_() {
      return EpicFightParticleRenderTypes.TRANSLUCENT_GLOWING;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(
         SimpleParticleType typeIn, ClientLevel level, double startX, double startY, double startZ, double endX, double endY, double endZ
      ) {
         return new LaserParticle(level, startX, startY, startZ, endX, endY, endZ);
      }
   }
}
