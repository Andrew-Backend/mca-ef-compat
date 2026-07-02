package yesman.epicfight.world.capabilities.entitypatch.boss;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.WitherGhostClone;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class WitherGhostPatch extends MobPatch<WitherGhostClone> {
   public void onJoinWorld(WitherGhostClone original, EntityJoinLevelEvent event) {
      super.onJoinWorld(original, event);
      if (!this.original.m_21525_()) {
         this.playAnimation(Animations.WITHER_CHARGE, 0.0F);
         if (this.isLogicalClient()) {
            this.playSound(SoundEvents.f_12554_, -0.1F, 0.1F);
         }
      }
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.IDLE, Animations.WITHER_IDLE);
      animator.addLivingAnimation(LivingMotions.DEATH, Animations.WITHER_IDLE);
   }

   public static void initAttributes(EntityAttributeModificationEvent event) {
      event.add((EntityType)EpicFightEntities.WITHER_GHOST_CLONE.get(), (Attribute)EpicFightAttributes.IMPACT.get(), 3.0);
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      this.currentLivingMotion = LivingMotions.IDLE;
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      return null;
   }
}
