package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class AttackPhaseEndEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
   private final AnimationManager.AnimationAccessor<? extends AttackAnimation> animation;
   private final AttackAnimation.Phase phase;
   private final int phaseOrder;

   public AttackPhaseEndEvent(
      ServerPlayerPatch playerpatch, AnimationManager.AnimationAccessor<? extends AttackAnimation> animation, AttackAnimation.Phase phase, int phaseOrder
   ) {
      super(playerpatch, false);
      this.animation = animation;
      this.phase = phase;
      this.phaseOrder = phaseOrder;
   }

   public AnimationManager.AnimationAccessor<? extends AttackAnimation> getAnimation() {
      return this.animation;
   }

   public AttackAnimation.Phase getPhase() {
      return this.phase;
   }

   public int getPhaseOrder() {
      return this.phaseOrder;
   }
}
