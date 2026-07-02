package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class AttackEndEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
   private AnimationManager.AnimationAccessor<? extends AttackAnimation> animation;

   public AttackEndEvent(ServerPlayerPatch playerpatch, AnimationManager.AnimationAccessor<? extends AttackAnimation> animation) {
      super(playerpatch, false);
      this.animation = animation;
   }

   public AnimationManager.AnimationAccessor<? extends AttackAnimation> getAnimation() {
      return this.animation;
   }
}
