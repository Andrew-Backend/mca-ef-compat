package yesman.epicfight.network.common;

import javax.annotation.Nullable;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.SynchedAnimationVariableKey;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class AnimationVariablePacket<T> {
   protected AssetAccessor<? extends StaticAnimation> animation;
   protected SynchedAnimationVariableKey<T> animationVariableKey;
   protected T value;
   protected AnimationVariablePacket.Action action;

   public AnimationVariablePacket(
      SynchedAnimationVariableKey<T> animationVariableKey,
      @Nullable AssetAccessor<? extends StaticAnimation> animation,
      T value,
      AnimationVariablePacket.Action action
   ) {
      this.animationVariableKey = animationVariableKey;
      this.value = value;
      this.action = action;
      this.animation = animation;
   }

   public void process(LivingEntityPatch<?> entitypatch) {
      switch (this.action) {
         case PUT:
            if (this.animationVariableKey.isSharedKey()) {
               entitypatch.<Animator>getAnimator()
                  .getVariables()
                  .putSharedVariable((AnimationVariables.SharedAnimationVariableKey<T>)this.animationVariableKey, this.value, false);
            } else {
               entitypatch.<Animator>getAnimator()
                  .getVariables()
                  .put((AnimationVariables.IndependentAnimationVariableKey<T>)this.animationVariableKey, this.animation, this.value, false);
            }
            break;
         case REMOVE:
            if (this.animationVariableKey.isSharedKey()) {
               entitypatch.<Animator>getAnimator()
                  .getVariables()
                  .removeSharedVariable((AnimationVariables.SharedAnimationVariableKey<T>)this.animationVariableKey, false);
            } else {
               entitypatch.<Animator>getAnimator()
                  .getVariables()
                  .remove((AnimationVariables.IndependentAnimationVariableKey<?>)this.animationVariableKey, this.animation, false);
            }
      }
   }

   public enum Action {
      PUT,
      REMOVE;
   }
}
