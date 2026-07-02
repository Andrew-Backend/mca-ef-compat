package yesman.epicfight.world.capabilities.entitypatch.mob;

import java.util.Optional;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Zoglin;
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
import yesman.epicfight.world.entity.ai.behavior.AnimatedCombatBehavior;
import yesman.epicfight.world.entity.ai.behavior.MoveToTargetSinkStopInaction;
import yesman.epicfight.world.entity.ai.brain.BrainRecomposer;

public class ZoglinPatch extends MobPatch<Zoglin> {
   public ZoglinPatch() {
      super(Factions.ZOMBIFIED_PIGLIN);
   }

   @Override
   public void initAI() {
      super.initAI();
      BrainRecomposer.recomposeZoglinBrain(
         this.original.m_6274_(), new AnimatedCombatBehavior<>(this, MobCombatBehaviors.HOGLIN.build(this)), new MoveToTargetSinkStopInaction()
      );
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.HOGLIN_IDLE);
      animator.addLivingAnimation(LivingMotions.WALK, Animations.HOGLIN_WALK);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.HOGLIN_DEATH);
   }

   public static void initAttributes(EntityAttributeModificationEvent event) {
      event.add(EntityType.f_20500_, (Attribute)EpicFightAttributes.MAX_STRIKES.get(), 4.0);
      event.add(EntityType.f_20500_, (Attribute)EpicFightAttributes.IMPACT.get(), 5.0);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      super.commonMobUpdateMotion(considerInaction);
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

   @Override
   public LivingEntity getTarget() {
      Optional<LivingEntity> opt = this.original.m_6274_().m_21952_(MemoryModuleType.f_26372_);
      return opt.orElse(null);
   }
}
