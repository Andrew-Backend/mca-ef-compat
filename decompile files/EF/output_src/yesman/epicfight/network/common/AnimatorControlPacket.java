package yesman.epicfight.network.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AnimatorControlPacket {
   protected AnimatorControlPacket.Action action;
   protected int animationId;
   protected float transitionTimeModifier;
   protected boolean pause;

   public AnimatorControlPacket(AnimatorControlPacket.Action action, int animationId, float transitionTimeModifier, boolean pause) {
      this.action = action;
      this.animationId = animationId;
      this.transitionTimeModifier = transitionTimeModifier;
      this.pause = pause;
   }

   public <T extends SPAnimatorControl> void process(LivingEntityPatch<?> entitypatch) {
      try {
         switch (this.action) {
            case PLAY:
               entitypatch.<Animator>getAnimator().playAnimation(this.animationId, this.transitionTimeModifier);
               break;
            case PLAY_INSTANTLY:
               entitypatch.<Animator>getAnimator().playAnimationInstantly(this.animationId);
               break;
            case RESERVE:
               entitypatch.<Animator>getAnimator().reserveAnimation(this.animationId);
               break;
            case STOP:
               entitypatch.<Animator>getAnimator().stopPlaying(this.animationId);
               break;
            case SHOT:
               entitypatch.<Animator>getAnimator().playShootingAnimation();
               break;
            case SOFT_PAUSE:
               entitypatch.<Animator>getAnimator().setSoftPause(this.pause);
               break;
            case HARD_PAUSE:
               entitypatch.<Animator>getAnimator().setHardPause(this.pause);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static yesman.epicfight.api.client.animation.Layer.Priority getPriority(AnimatorControlPacket.Priority priority) {
      switch (priority) {
         case LOWEST:
            return yesman.epicfight.api.client.animation.Layer.Priority.LOWEST;
         case LOW:
            return yesman.epicfight.api.client.animation.Layer.Priority.LOW;
         case MIDDLE:
            return yesman.epicfight.api.client.animation.Layer.Priority.MIDDLE;
         case HIGH:
            return yesman.epicfight.api.client.animation.Layer.Priority.HIGH;
         case HIGHEST:
            return yesman.epicfight.api.client.animation.Layer.Priority.HIGHEST;
         default:
            return null;
      }
   }

   public enum Action {
      PLAY,
      PLAY_CLIENT,
      PLAY_INSTANTLY,
      RESERVE,
      STOP,
      SHOT,
      SOFT_PAUSE,
      HARD_PAUSE;
   }

   public enum Layer {
      ANIMATION,
      BASE_LAYER,
      COMPOSITE_LAYER;
   }

   public enum Priority {
      ANIMATION,
      LOWEST,
      LOW,
      MIDDLE,
      HIGH,
      HIGHEST;
   }
}
