package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class RavagerPatch extends MobPatch<Ravager> {
   public RavagerPatch() {
      super(Factions.ILLAGER);
   }

   public static void initAttributes(EntityAttributeModificationEvent event) {
      event.add(EntityType.f_20518_, (Attribute)EpicFightAttributes.MAX_STRIKES.get(), 8.0);
      event.add(EntityType.f_20518_, (Attribute)EpicFightAttributes.IMPACT.get(), 6.0);
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.RAVAGER_IDLE);
      animator.addLivingAnimation(LivingMotions.WALK, Animations.RAVAGER_WALK);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.RAVAGER_DEATH);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      super.commonMobUpdateMotion(considerInaction);
   }

   @Override
   protected void initAI() {
      super.initAI();
      this.original.f_21345_.m_25352_(0, new AnimatedAttackGoal<>(this, MobCombatBehaviors.RAVAGER.build(this)));
      this.original.f_21345_.m_25352_(1, new TargetChasingGoal(this, (PathfinderMob)this.original, 1.0, false));
   }

   @Override
   public void onAttackBlocked(DamageSource damageSource, LivingEntityPatch<?> opponent) {
      if (this.original.m_33364_() > 0) {
         this.playAnimationSynchronized(Animations.RAVAGER_STUN, 0.0F);
      }
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      return null;
   }

   @Override
   public SoundEvent getWeaponHitSound(InteractionHand hand) {
      return (SoundEvent)EpicFightSounds.BLUNT_HIT_HARD.get();
   }

   @Override
   public SoundEvent getSwingSound(InteractionHand hand) {
      return (SoundEvent)EpicFightSounds.WHOOSH_BIG.get();
   }
}
