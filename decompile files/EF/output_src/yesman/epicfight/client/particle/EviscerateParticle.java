package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.particle.EpicFightParticles;

public class EviscerateParticle extends NoRenderParticle {
   protected EviscerateParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
      super(world, x, y, z, motionX, motionY, motionZ);

      for (int i = 0; i < 50; i++) {
         Vec3 rot = MathUtils.getVectorForRotation(0.0F, (float)motionY);
         double particleMotionX = rot.f_82479_ * this.f_107223_.m_188501_() * -0.5;
         double particleMotionZ = rot.f_82481_ * this.f_107223_.m_188501_() * -0.5;
         this.f_107208_
            .m_7106_((ParticleOptions)EpicFightParticles.BLOOD.get(), this.f_107212_, this.f_107213_, this.f_107214_, particleMotionX, 0.0, particleMotionZ);
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new EviscerateParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
      }
   }
}
