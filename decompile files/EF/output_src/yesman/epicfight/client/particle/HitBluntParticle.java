package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import yesman.epicfight.particle.EpicFightParticles;

public class HitBluntParticle extends HitParticle {
   public HitBluntParticle(ClientLevel world, double x, double y, double z, double argX, double argY, double argZ, SpriteSet animatedSprite) {
      super(world, x, y, z, animatedSprite);
      this.f_107227_ = 1.0F;
      this.f_107228_ = 1.0F;
      this.f_107229_ = 1.0F;
      this.f_107663_ = 1.0F;
      this.f_107225_ = 2;
      double d = 1.0;

      for (int i = 0; i < 7; i++) {
         double particleMotionX = this.f_107223_.m_188500_() * d;
         d *= this.f_107223_.m_188499_() ? 1.0 : -1.0;
         double particleMotionZ = this.f_107223_.m_188500_() * d;
         d *= this.f_107223_.m_188499_() ? 1.0 : -1.0;
         this.f_107208_
            .m_7106_(
               (ParticleOptions)EpicFightParticles.DUST_EXPANSIVE.get(),
               this.f_107212_,
               this.f_107213_,
               this.f_107214_,
               particleMotionX,
               this.f_107223_.m_188500_() * 0.5,
               particleMotionZ
            );
      }
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new HitBluntParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
      }
   }
}
