package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class WitchPatch extends HumanoidMobPatch<Witch> {
   public WitchPatch() {
      super(Factions.NEUTRAL);
   }

   @Override
   public void setAIAsInfantry(boolean holdingRanedWeapon) {
      this.original.f_21345_.m_25352_(0, new AnimatedAttackGoal<>(this, MobCombatBehaviors.WITCH.build(this)));
      this.original.f_21345_.m_25352_(1, new TargetChasingGoal(this, (PathfinderMob)this.getOriginal(), 1.0, true, 10.0));
   }

   @Override
   public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, InteractionHand hand) {
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.ILLAGER_IDLE);
      animator.addLivingAnimation(LivingMotions.WALK, Animations.ILLAGER_WALK);
      animator.addLivingAnimation(LivingMotions.DRINK, Animations.WITCH_DRINKING);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      super.commonMobUpdateMotion(considerInaction);
      if (this.original.m_34161_()) {
         this.currentCompositeMotion = LivingMotions.DRINK;
      }
   }
}
