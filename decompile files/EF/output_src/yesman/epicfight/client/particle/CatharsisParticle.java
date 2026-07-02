package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class CatharsisParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected CatharsisParticle(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet sprites) {
      super(pLevel, pX, pY, pZ);
      this.f_107216_ = 0.1;
      this.f_107663_ = 0.75F;
      this.sprites = sprites;
      this.m_108339_(sprites);
   }

   public void m_5989_() {
      super.m_5989_();
      this.m_108339_(this.sprites);
      this.f_107230_ -= 0.05F;
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107431_;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(
         SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed
      ) {
         CatharsisParticle catharsisparticle = new CatharsisParticle(pLevel, pX, pY, pZ, this.sprites);
         catharsisparticle.m_107271_(0.8F);
         catharsisparticle.m_107257_(12);
         return catharsisparticle;
      }
   }
}
