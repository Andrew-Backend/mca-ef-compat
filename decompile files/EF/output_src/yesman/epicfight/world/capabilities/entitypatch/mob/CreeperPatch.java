package yesman.epicfight.world.capabilities.entitypatch.mob;

import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.CreeperSwellStoppableGoal;

public class CreeperPatch extends MobPatch<Creeper> {
   public CreeperPatch() {
      super(Factions.NEUTRAL);
   }

   public static void initAttributes(EntityAttributeModificationEvent event) {
      event.add(EntityType.f_20558_, (Attribute)EpicFightAttributes.STUN_ARMOR.get(), 1.0);
   }

   @Override
   protected void selectGoalToRemove(Set<Goal> toRemove) {
      for (WrappedGoal wrappedGoal : this.original.f_21345_.m_148105_()) {
         Goal goal = wrappedGoal.m_26015_();
         if (goal instanceof SwellGoal) {
            toRemove.add(goal);
         }
      }
   }

   @Override
   protected void initAI() {
      super.initAI();
      this.original.f_21345_.m_25352_(2, new CreeperSwellStoppableGoal(this, this.original));
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.CREEPER_IDLE);
      animator.addLivingAnimation(LivingMotions.WALK, Animations.CREEPER_WALK);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.CREEPER_DEATH);
   }

   @Override
   public void serverTick(LivingTickEvent event) {
      super.serverTick(event);
      if (this.getEntityState().inaction()) {
         for (WrappedGoal goal : this.original.f_21345_.m_148105_()) {
            if (goal.m_26015_() instanceof CreeperSwellStoppableGoal && goal.m_7620_()) {
               goal.m_8041_();
            }
         }
      }
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      super.commonMobUpdateMotion(considerInaction);
   }

   @Override
   public OpenMatrix4f getModelMatrix(float partialTicks) {
      OpenMatrix4f mat = super.getModelMatrix(partialTicks);
      if (this.isLogicalClient()) {
         float f = this.original.m_32320_(partialTicks);
         float f1 = 1.0F + Mth.m_14031_(f * 100.0F) * f * 0.01F;
         f = Mth.m_14036_(f, 0.0F, 1.0F);
         f *= f;
         f *= f;
         float f2 = (1.0F + f * 0.4F) * f1;
         float f3 = (1.0F + f * 0.1F) / f1;
         OpenMatrix4f.scale(new Vec3f(f2, f3, f2), mat, mat);
      }

      return mat;
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      return stunType == StunType.LONG ? Animations.CREEPER_HIT_LONG : Animations.CREEPER_HIT_SHORT;
   }
}
