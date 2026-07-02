package yesman.epicfight.world.capabilities.entitypatch.mob;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.monster.Vex;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;

public class VexPatch extends MobPatch<Vex> {
   public VexPatch() {
      super(Factions.ILLAGER);
   }

   @Override
   protected void initAI() {
      super.initAI();
      this.original.f_21345_.m_25352_(0, new VexPatch.ChargeAttackGoal());
   }

   @Override
   protected void selectGoalToRemove(Set<Goal> toRemove) {
      super.selectGoalToRemove(toRemove);
      Iterator<WrappedGoal> iterator = this.original.f_21345_.m_148105_().iterator();

      for (int index = 0; iterator.hasNext(); index++) {
         WrappedGoal goal = iterator.next();
         Goal inner = goal.m_26015_();
         if (index == 1) {
            toRemove.add(inner);
            break;
         }
      }
   }

   @Override
   public void tick(LivingTickEvent event) {
      super.tick(event);
      if (!this.isLogicalClient()) {
         if (this.getEntityState().movementLocked()) {
            this.original.f_21345_.m_25355_(Flag.MOVE);
            this.original.f_21345_.m_25355_(Flag.JUMP);
         } else {
            this.original.f_21345_.m_25374_(Flag.MOVE);
            this.original.f_21345_.m_25374_(Flag.JUMP);
         }

         if (this.getEntityState().turningLocked()) {
            this.original.f_21345_.m_25355_(Flag.LOOK);
         } else {
            this.original.f_21345_.m_25374_(Flag.LOOK);
         }
      }
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.VEX_IDLE);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.VEX_DEATH);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.VEX_FLIPPING);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      if (this.original.m_21223_() <= 0.0F) {
         this.currentLivingMotion = LivingMotions.DEATH;
      } else if (this.state.inaction() && considerInaction) {
         this.currentLivingMotion = LivingMotions.INACTION;
      } else {
         this.currentLivingMotion = LivingMotions.IDLE;
         this.currentCompositeMotion = LivingMotions.IDLE;
      }
   }

   @Override
   public void onAttackBlocked(DamageSource damageSource, LivingEntityPatch<?> opponent) {
      this.original.m_146884_(opponent.getOriginal().m_146892_().m_82549_(opponent.getOriginal().m_20154_()));
      this.playAnimationSynchronized(Animations.VEX_NEUTRALIZED, 0.0F);
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      return Animations.VEX_HIT;
   }

   class ChargeAttackGoal extends Goal {
      private int chargingCounter;

      public ChargeAttackGoal() {
         this.m_7021_(EnumSet.of(Flag.MOVE));
      }

      public boolean m_8036_() {
         if (VexPatch.this.original.m_5448_() != null && !VexPatch.this.getEntityState().inaction() && VexPatch.this.original.m_217043_().m_188503_(10) == 0) {
            double distance = VexPatch.this.original.m_20280_(VexPatch.this.original.m_5448_());
            return distance < 49.0;
         } else {
            return false;
         }
      }

      public boolean m_8045_() {
         return this.chargingCounter > 0;
      }

      public void m_8056_() {
         VexPatch.this.original
            .m_21566_()
            .m_6849_(VexPatch.this.original.m_20185_(), VexPatch.this.original.m_20186_(), VexPatch.this.original.m_20189_(), 0.25);
         VexPatch.this.playAnimationSynchronized(Animations.VEX_CHARGE, 0.0F);
         VexPatch.this.original.m_5496_(SoundEvents.f_12500_, 1.0F, 1.0F);
         VexPatch.this.original.m_34042_(true);
         this.chargingCounter = 20;
      }

      public void m_8041_() {
         VexPatch.this.original.m_34042_(false);
      }

      public void m_8037_() {
         this.chargingCounter--;
      }
   }
}
