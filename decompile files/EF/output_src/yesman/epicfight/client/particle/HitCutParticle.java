package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import yesman.epicfight.particle.EpicFightParticles;

public class HitCutParticle extends NoRenderParticle {
   public HitCutParticle(ClientLevel world, double x, double y, double z, double width, double height, double _null) {
      super(world, x, y, z);
      this.f_107212_ = x + (this.f_107223_.m_188500_() - 0.5) * width;
      this.f_107213_ = y + (this.f_107223_.m_188500_() + height) * 0.5;
      this.f_107214_ = z + (this.f_107223_.m_188500_() - 0.5) * width;
      this.f_107208_.m_7106_((ParticleOptions)EpicFightParticles.CUT.get(), this.f_107212_, this.f_107213_, this.f_107214_, 0.0, 0.0, 0.0);
      double d = 0.2F;

      for (int i = 0; i < 6; i++) {
         double particleMotionX = this.f_107223_.m_188500_() * d;
         d *= this.f_107223_.m_188499_() ? 1.0 : -1.0;
         double particleMotionZ = this.f_107223_.m_188500_() * d;
         d *= this.f_107223_.m_188499_() ? 1.0 : -1.0;
         this.f_107208_
            .m_7106_((ParticleOptions)EpicFightParticles.BLOOD.get(), this.f_107212_, this.f_107213_, this.f_107214_, particleMotionX, 0.0, particleMotionZ);
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new HitCutParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
      }
   }
}
