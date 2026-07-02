package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public abstract class HitParticle extends TextureSheetParticle {
   protected final SpriteSet animatedSprite;

   public HitParticle(ClientLevel world, double x, double y, double z, SpriteSet animatedSprite) {
      super(world, x, y, z);
      this.f_107227_ = 1.0F;
      this.f_107228_ = 1.0F;
      this.f_107229_ = 1.0F;
      this.animatedSprite = animatedSprite;
      this.m_108339_(animatedSprite);
   }

   public void m_5989_() {
      this.f_107209_ = this.f_107212_;
      this.f_107210_ = this.f_107213_;
      this.f_107211_ = this.f_107214_;
      if (this.f_107224_++ >= this.f_107225_) {
         this.m_107274_();
      } else {
         this.m_108339_(this.animatedSprite);
      }
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107431_;
   }

   public int m_6355_(float partialTick) {
      return 15728880;
   }
}
