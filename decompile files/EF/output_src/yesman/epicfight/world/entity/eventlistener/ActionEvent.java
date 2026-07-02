package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class ActionEvent<T extends PlayerPatch<?>> extends AbstractPlayerEvent<T> {
   private final AnimationManager.AnimationAccessor<? extends MainFrameAnimation> actionAnimation;
   private boolean resetActionTick;

   public ActionEvent(PlayerPatch<?> playerdata, AnimationManager.AnimationAccessor<? extends MainFrameAnimation> actionAnimation) {
      super((T)playerdata, false);
      this.actionAnimation = actionAnimation;
      this.resetActionTick = true;
   }

   public AnimationManager.AnimationAccessor<? extends MainFrameAnimation> getAnimation() {
      return this.actionAnimation;
   }

   public void resetActionTick(boolean flag) {
      this.resetActionTick = flag;
   }

   public boolean shouldResetActionTick() {
      return this.resetActionTick;
   }
}
