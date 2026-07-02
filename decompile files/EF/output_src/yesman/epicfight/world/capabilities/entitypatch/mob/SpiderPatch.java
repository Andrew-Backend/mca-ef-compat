package yesman.epicfight.world.capabilities.entitypatch.mob;

import java.util.Set;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class SpiderPatch<T extends PathfinderMob> extends MobPatch<T> {
   public SpiderPatch() {
      super(Factions.NEUTRAL);
   }

   @Override
   protected void initAI() {
      super.initAI();
      this.original.f_21345_.m_25352_(0, new AnimatedAttackGoal<>(this, (CombatBehaviors<SpiderPatch<T>>)MobCombatBehaviors.SPIDER.build(this)));
      this.original.f_21345_.m_25352_(1, new TargetChasingGoal(this, this.original, 1.0, false));
   }

   @Override
   protected void selectGoalToRemove(Set<Goal> toRemove) {
      super.selectGoalToRemove(toRemove);

      for (WrappedGoal wrappedGoal : this.original.f_21345_.m_148105_()) {
         Goal goal = wrappedGoal.m_26015_();
         if (goal instanceof LeapAtTargetGoal) {
            toRemove.add(goal);
         }
      }
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.SPIDER_DEATH);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.SPIDER_IDLE);
      animator.addLivingAnimation(LivingMotions.WALK, Animations.SPIDER_CRAWL);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      super.commonMobUpdateMotion(considerInaction);
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      switch (stunType) {
         case SHORT:
            return Animations.SPIDER_HIT;
         case LONG:
            return Animations.SPIDER_HIT;
         case HOLD:
            return Animations.SPIDER_HIT;
         case KNOCKDOWN:
            return Animations.SPIDER_NEUTRALIZED;
         case NEUTRALIZE:
            return Animations.SPIDER_NEUTRALIZED;
         default:
            return null;
      }
   }

   @Override
   public SoundEvent getSwingSound(InteractionHand hand) {
      return SoundEvents.f_12434_;
   }
}
