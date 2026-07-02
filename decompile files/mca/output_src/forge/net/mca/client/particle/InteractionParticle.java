package forge.net.mca.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class InteractionParticle extends TextureSheetParticle {
   protected InteractionParticle(ClientLevel world, double x, double y, double z) {
      super(world, x, y, z);
      this.f_107215_ *= 0.01F;
      this.f_107216_ *= 0.01F;
      this.f_107217_ *= 0.01F;
      this.f_107216_ += 0.1;
      this.f_107663_ *= 1.5F;
      this.f_107225_ = 20;
      this.f_107219_ = false;
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107430_;
   }

   public float m_5902_(float tickDelta) {
      return 0.3F;
   }

   public void m_5989_() {
      this.f_107209_ = this.f_107212_;
      this.f_107210_ = this.f_107213_;
      this.f_107211_ = this.f_107214_;
      if (this.f_107224_++ >= this.f_107225_) {
         this.m_107274_();
      } else {
         if (this.f_107213_ == this.f_107210_) {
            this.f_107215_ *= 1.1;
            this.f_107217_ *= 1.1;
         }

         this.f_107215_ *= 0.86F;
         this.f_107216_ *= 0.86F;
         this.f_107217_ *= 0.86F;
         if (this.f_107218_) {
            this.f_107215_ *= 0.7F;
            this.f_107217_ *= 0.7F;
         }
      }
   }

   public static class Factory implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public Factory(SpriteSet sprite) {
         this.sprite = sprite;
      }

      public Particle createParticle(
         SimpleParticleType particleType, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ
      ) {
         InteractionParticle heartparticle = new InteractionParticle(world, x, y + 0.5, z);
         heartparticle.m_108335_(this.sprite);
         heartparticle.m_107253_(1.0F, 1.0F, 1.0F);
         return heartparticle;
      }
   }
}
