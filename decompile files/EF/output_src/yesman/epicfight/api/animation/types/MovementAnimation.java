package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class MovementAnimation extends StaticAnimation {
   public MovementAnimation(
      boolean isRepeat, AnimationManager.AnimationAccessor<? extends MovementAnimation> accessor, AssetAccessor<? extends Armature> armature
   ) {
      super(0.15F, isRepeat, accessor, armature);
   }

   public MovementAnimation(
      float transitionTime,
      boolean isRepeat,
      AnimationManager.AnimationAccessor<? extends MovementAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, isRepeat, accessor, armature);
   }

   public MovementAnimation(float transitionTime, boolean isRepeat, String path, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, isRepeat, path, armature);
   }

   @Override
   public float getPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
      if (animation.isLinkAnimation()) {
         return 1.0F;
      }

      float movementSpeed = 1.0F;
      if (Math.abs(entitypatch.getOriginal().f_267362_.m_267731_() - entitypatch.getOriginal().f_267362_.m_267711_(1.0F)) < 0.007F) {
         movementSpeed *= entitypatch.getOriginal().f_267362_.m_267731_() * 1.16F;
      }

      return movementSpeed;
   }

   @Override
   public boolean canBePlayedReverse() {
      return true;
   }
}
