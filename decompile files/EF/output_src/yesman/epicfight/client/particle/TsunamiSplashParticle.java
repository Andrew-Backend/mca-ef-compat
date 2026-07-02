package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class TsunamiSplashParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   TsunamiSplashParticle(
      ClientLevel p_108407_, double p_108408_, double p_108409_, double p_108410_, double p_108411_, double p_108412_, double p_108413_, SpriteSet p_108414_
   ) {
      super(p_108407_, p_108408_, p_108409_, p_108410_, 0.0, 0.0, 0.0);
      this.sprites = p_108414_;
      this.f_107225_ = 16;
      this.m_108337_(this.sprites.m_213979_(this.f_107223_));
      this.f_107219_ = true;
      this.f_107226_ = 1.0F;
      this.f_107215_ = p_108411_;
      this.f_107216_ = p_108412_;
      this.f_107217_ = p_108413_;
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107430_;
   }

   public void m_5989_() {
      super.m_5989_();
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet p_108429_) {
         this.sprites = p_108429_;
      }

      public Particle createParticle(
         SimpleParticleType p_108440_,
         ClientLevel p_108441_,
         double p_108442_,
         double p_108443_,
         double p_108444_,
         double p_108445_,
         double p_108446_,
         double p_108447_
      ) {
         return new TsunamiSplashParticle(p_108441_, p_108442_, p_108443_, p_108444_, p_108445_, p_108446_, p_108447_, this.sprites);
      }
   }
}
