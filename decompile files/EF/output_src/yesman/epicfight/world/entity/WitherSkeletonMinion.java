package yesman.epicfight.world.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;

public class WitherSkeletonMinion extends WitherSkeleton {
   private WitherBoss summoner;

   public WitherSkeletonMinion(EntityType<? extends WitherSkeletonMinion> p_34166_, Level p_34167_) {
      super(p_34166_, p_34167_);
   }

   public WitherSkeletonMinion(Level level, WitherBoss summoner, double x, double y, double z) {
      super((EntityType)EpicFightEntities.WITHER_SKELETON_MINION.get(), level);
      this.m_20343_(x, y, z);
      this.summoner = summoner;
      if (this.summoner != null && this.summoner.m_6084_()) {
         this.m_6710_((LivingEntity)this.summoner.m_9236_().m_6815_(this.summoner.m_31512_(0)));
      }
   }

   public boolean m_7301_(MobEffectInstance p_70687_1_) {
      return p_70687_1_.m_19544_() != MobEffects.f_19615_ && super.m_7301_(p_70687_1_);
   }

   protected void m_8099_() {
      super.m_8099_();
      this.f_21346_
         .m_25352_(
            3,
            new NearestAttackableTargetGoal(
               this, LivingEntity.class, 10, true, false, livingentity -> livingentity.m_6336_() != MobType.f_21641_ && livingentity.m_5789_()
            )
         );
   }

   public boolean m_6469_(DamageSource source, float amount) {
      return this.summoner != null && source.m_7639_() == this.summoner ? false : super.m_6469_(source, amount);
   }

   public void m_8119_() {
      super.m_8119_();
      if (this.m_9236_().m_5776_()) {
         this.m_9236_()
            .m_7106_(
               ParticleTypes.f_123762_,
               this.m_20185_() + this.f_19796_.m_188583_() * 0.3F,
               this.m_20188_() + this.f_19796_.m_188583_() * 0.3F,
               this.m_20189_() + this.f_19796_.m_188583_() * 0.3F,
               0.0,
               0.0,
               0.0
            );
      } else {
         if (this.f_19797_ > 200 && this.f_19797_ % 30 == 0) {
            this.m_6469_(this.m_9236_().m_269111_().m_269251_(), 1.0F);
         }

         if (this.summoner != null && !this.summoner.m_6084_()) {
            this.m_21153_(0.0F);
         }
      }
   }
}
