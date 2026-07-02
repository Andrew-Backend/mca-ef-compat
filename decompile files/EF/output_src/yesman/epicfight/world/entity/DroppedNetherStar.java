package yesman.epicfight.world.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;

public class DroppedNetherStar extends ItemEntity {
   public DroppedNetherStar(EntityType<? extends DroppedNetherStar> entityType, Level level) {
      super(entityType, level);
   }

   public DroppedNetherStar(Level level, double x, double y, double z, ItemStack itemstack, double dx, double dy, double dz) {
      this((EntityType<? extends DroppedNetherStar>)EpicFightEntities.DROPPED_NETHER_STAR.get(), level);
      this.m_6034_(x, y, z);
      this.m_20334_(dx, dy, dz);
      this.m_32045_(itemstack);
      this.lifespan = itemstack.m_41720_() == null ? 6000 : itemstack.getEntityLifespan(level);
      this.f_19794_ = true;
      this.m_32010_(30);
      this.m_20242_(true);
   }

   public DroppedNetherStar(Level level, Vec3 position, Vec3 deltaMovement) {
      this(
         level,
         position.f_82479_,
         position.f_82480_,
         position.f_82481_,
         new ItemStack(Items.f_42686_),
         deltaMovement.f_82479_,
         deltaMovement.f_82480_,
         deltaMovement.f_82481_
      );
   }

   public void m_8119_() {
      super.m_8119_();
      if (this.f_19797_ % 70 == 0) {
         this.m_9236_()
            .m_7785_(
               this.m_20185_(), this.m_20186_(), this.m_20189_(), (SoundEvent)EpicFightSounds.NETHER_STAR_GLITTER.get(), this.m_5720_(), 1.0F, 1.0F, false
            );
      }

      Vec3 deltaMove = this.m_20184_();
      if (this.m_9236_().m_5776_()) {
         Vec3 particleDeltaMove = new Vec3(-deltaMove.f_82479_, -1.0, -deltaMove.f_82481_)
            .m_82541_()
            .m_82520_((this.f_19796_.m_188501_() - 0.5F) * 0.1F, 0.0, (this.f_19796_.m_188501_() - 0.5F) * 0.1F);
         this.m_9236_()
            .m_7106_(
               (ParticleOptions)EpicFightParticles.NORMAL_DUST.get(),
               this.m_20185_() + (this.f_19796_.m_188501_() - 0.5F) * this.m_20205_(),
               this.m_20186_() + this.m_20206_() * 2.5,
               this.m_20189_() + (this.f_19796_.m_188501_() - 0.5F) * this.m_20205_(),
               particleDeltaMove.f_82479_,
               0.0,
               particleDeltaMove.f_82481_
            );
      }

      this.m_20256_(deltaMove.m_82542_(0.68, 0.68, 0.68));
   }

   public boolean m_6060_() {
      return true;
   }

   public boolean m_6051_() {
      return false;
   }
}
