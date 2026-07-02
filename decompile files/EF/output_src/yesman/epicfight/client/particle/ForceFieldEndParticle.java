package yesman.epicfight.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.LightningRenderHelper;

public class ForceFieldEndParticle extends Particle {
   private boolean init;

   protected ForceFieldEndParticle(ClientLevel level, double x, double y, double z) {
      super(level, x, y, z);
      this.f_107225_ = 10;
      Minecraft mc = Minecraft.m_91087_();
      mc.f_91061_.m_107344_(new DustParticle.ExpansiveMetaParticle(level, x, y, z, 6.0, 80));
   }

   public ParticleRenderType m_7556_() {
      return EpicFightParticleRenderTypes.LIGHTNING;
   }

   public void m_5744_(VertexConsumer vertexBuilder, Camera camera, float parttialTick) {
      PoseStack poseStack = new PoseStack();
      Vec3 vec3 = camera.m_90583_();
      float f = (float)(Mth.m_14139_(parttialTick, this.f_107209_, this.f_107212_) - vec3.m_7096_());
      float f1 = (float)(Mth.m_14139_(parttialTick, this.f_107210_, this.f_107213_) - vec3.m_7098_());
      float f2 = (float)(Mth.m_14139_(parttialTick, this.f_107211_, this.f_107214_) - vec3.m_7094_());
      poseStack.m_252880_(f, f1, f2);
      if (this.f_107224_ > 0) {
         float progression = (this.f_107224_ + parttialTick) / this.f_107225_;
         LightningRenderHelper.renderFlashingLight(vertexBuilder, poseStack, 255, 0, 255, 15, 1.0F, progression);
      }

      if (!this.init) {
         ClientEngine.getInstance().renderEngine.getOverlayManager().flickering("flickering", 0.05F, 1.2F);
         this.init = true;
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new ForceFieldEndParticle(level, x, y, z);
      }
   }
}
