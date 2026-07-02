package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class ZombifiedPiglinPatch extends HumanoidMobPatch<ZombifiedPiglin> {
   public ZombifiedPiglinPatch() {
      super(Factions.ZOMBIFIED_PIGLIN);
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.PIGLIN_ZOMBIFIED_IDLE);
      animator.addLivingAnimation(LivingMotions.WALK, Animations.PIGLIN_ZOMBIFIED_WALK);
      animator.addLivingAnimation(LivingMotions.CHASE, Animations.PIGLIN_ZOMBIFIED_CHASE);
      animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
      animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.PIGLIN_DEATH);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      super.commonAggressiveMobUpdateMotion(considerInaction);
   }

   @Override
   public void setAIAsInfantry(boolean holdingRanedWeapon) {
      CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();
      if (builder != null) {
         this.original.f_21345_.m_25352_(1, new AnimatedAttackGoal<>(this, builder.build(this)));
         this.original.f_21345_.m_25352_(1, new TargetChasingGoal(this, (PathfinderMob)this.getOriginal(), 1.2, true));
      }
   }

   @Override
   public AttackResult tryHurt(DamageSource damageSource, float amount) {
      return damageSource.m_7639_() instanceof ZombifiedPiglin ? AttackResult.blocked(amount) : super.tryHurt(damageSource, amount);
   }
}
