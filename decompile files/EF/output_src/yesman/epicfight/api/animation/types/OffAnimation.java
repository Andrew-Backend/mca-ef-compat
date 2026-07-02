package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class OffAnimation extends StaticAnimation {
   public OffAnimation(AnimationManager.AnimationAccessor<? extends OffAnimation> accessor) {
      super(0.0F, false, accessor, null);
   }

   @Override
   public void begin(LivingEntityPatch<?> entitypatch) {
      if (entitypatch.isLogicalClient()) {
         AnimationPlayer player = entitypatch.getClientAnimator().getCompositeLayer(this.getPriority()).animationPlayer;
         if (!player.isEmpty() && !(player.getAnimation() instanceof OffAnimation)) {
            entitypatch.getClientAnimator().getCompositeLayer(this.getPriority()).off(entitypatch);
         }
      } else {
         entitypatch.<Animator>getAnimator().playAnimation(Animations.EMPTY_ANIMATION, 0.0F);
      }
   }

   @Override
   public boolean isMetaAnimation() {
      return true;
   }
}
