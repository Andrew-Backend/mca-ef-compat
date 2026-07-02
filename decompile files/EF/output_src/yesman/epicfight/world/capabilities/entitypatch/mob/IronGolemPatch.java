package yesman.epicfight.world.capabilities.entitypatch.mob;

import java.util.Set;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class IronGolemPatch extends MobPatch<IronGolem> {
   public IronGolemPatch() {
      super(Factions.VILLAGER);
   }

   @Override
   protected void initAI() {
      super.initAI();
      this.original.f_21345_.m_25352_(0, new AnimatedAttackGoal<>(this, MobCombatBehaviors.IRON_GOLEM.build(this)));
      this.original.f_21345_.m_25352_(1, new TargetChasingGoal(this, (PathfinderMob)this.original, 1.0, false));
   }

   @Override
   protected void selectGoalToRemove(Set<Goal> toRemove) {
      super.selectGoalToRemove(toRemove);

      for (WrappedGoal wrappedGoal : this.original.f_21345_.m_148105_()) {
         Goal goal = wrappedGoal.m_26015_();
         if (goal instanceof MoveTowardsTargetGoal) {
            toRemove.add(goal);
         }
      }
   }

   public static void initAttributes(EntityAttributeModificationEvent event) {
      event.add(EntityType.f_20460_, (Attribute)EpicFightAttributes.MAX_STRIKES.get(), 4.0);
      event.add(EntityType.f_20460_, (Attribute)EpicFightAttributes.IMPACT.get(), 6.0);
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.GOLEM_IDLE);
      animator.addLivingAnimation(LivingMotions.WALK, Animations.GOLEM_WALK);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.GOLEM_DEATH);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      super.commonMobUpdateMotion(considerInaction);
   }

   @Override
   public SoundEvent getWeaponHitSound(InteractionHand hand) {
      return (SoundEvent)EpicFightSounds.BLUNT_HIT_HARD.get();
   }

   @Override
   public SoundEvent getSwingSound(InteractionHand hand) {
      return (SoundEvent)EpicFightSounds.WHOOSH_BIG.get();
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      return null;
   }
}
