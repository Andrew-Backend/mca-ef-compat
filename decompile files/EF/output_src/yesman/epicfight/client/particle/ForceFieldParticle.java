package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.ClassicMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class ForceFieldParticle extends TexturedCustomModelParticle {
   private LivingEntityPatch<?> caster;

   public ForceFieldParticle(
      ClientLevel level, double x, double y, double z, double xd, double yd, double zd, AssetAccessor<ClassicMesh> particleMesh, ResourceLocation texture
   ) {
      super(level, x, y, z, xd, yd, zd, particleMesh, texture);
      this.f_107225_ = 158;
      this.f_107219_ = false;
      this.f_107231_ = (float)xd;
      this.pitch = (float)zd;
      Entity entity = level.m_6815_((int)Double.doubleToLongBits(yd));
      if (entity != null) {
         this.caster = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
      }
   }

   public ParticleRenderType m_7556_() {
      return EpicFightParticleRenderTypes.PARTICLE_MODEL_NO_NORMAL;
   }

   @Override
   public void m_5989_() {
      super.m_5989_();
      this.yaw += 36.0F;
      this.scale = this.scale + Math.max(30 - this.f_107224_, 0) / 140.0F;
      if (this.caster != null && this.caster.getStunShield() <= 0.0F) {
         this.m_107274_();
      }

      for (int x = -1; x <= 1; x += 2) {
         for (int z = -1; z <= 1; z += 2) {
            Vec3 rand = new Vec3(Math.random() * x, Math.random(), Math.random() * z).m_82541_().m_82490_(10.0);
            this.f_107208_
               .m_7106_(
                  (ParticleOptions)EpicFightParticles.DUST_CONTRACTIVE.get(),
                  this.f_107212_ + rand.f_82479_,
                  this.f_107213_ + rand.f_82480_ - 1.0,
                  this.f_107214_ + rand.f_82481_,
                  -rand.f_82479_,
                  -rand.f_82480_,
                  -rand.f_82481_
               );
         }
      }
   }

   public int m_6355_(float p_107086_) {
      int i = super.m_6355_(p_107086_);
      int k = i >> 16 & 0xFF;
      return 240 | k << 16;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         return new ForceFieldParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, Meshes.FORCE_FIELD, EnderDragonRenderer.f_114174_);
      }
   }
}
