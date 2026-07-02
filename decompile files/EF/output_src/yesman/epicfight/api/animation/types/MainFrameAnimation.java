package yesman.epicfight.api.animation.types;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.ActionEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class MainFrameAnimation extends StaticAnimation {
   public MainFrameAnimation(
      float convertTime, AnimationManager.AnimationAccessor<? extends MainFrameAnimation> accessor, AssetAccessor<? extends Armature> armature
   ) {
      super(convertTime, false, accessor, armature);
   }

   public MainFrameAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature) {
      super(convertTime, false, path, armature);
   }

   @Override
   public void begin(LivingEntityPatch<?> entitypatch) {
      if (entitypatch.<Animator>getAnimator().getPlayerFor(null).getAnimation().get() == this) {
         TypeFlexibleHashMap<EntityState.StateFactor<?>> stateMap = this.stateSpectrum.getStateMap(entitypatch, 0.0F);
         TypeFlexibleHashMap<EntityState.StateFactor<?>> modifiedStateMap = new TypeFlexibleHashMap<>(false);
         stateMap.forEach(
            (k, v) -> modifiedStateMap.put((EntityState.StateFactor<?>)k, this.getModifiedLinkState((EntityState.StateFactor<?>)k, v, entitypatch, 0.0F))
         );
         entitypatch.updateEntityState(new EntityState(modifiedStateMap));
      }

      if (entitypatch.isLogicalClient()) {
         entitypatch.updateMotion(false);
         this.getProperty(AnimationProperty.StaticAnimationProperty.RESET_LIVING_MOTION)
            .ifPresentOrElse(livingMotion -> entitypatch.getClientAnimator().forceResetBeforeAction(livingMotion, livingMotion), () -> {
               entitypatch.getClientAnimator().resetMotion(true);
               entitypatch.getClientAnimator().resetCompositeMotion();
            });
         entitypatch.getClientAnimator().getPlayerFor(this.getAccessor()).setReversed(false);
      }

      super.begin(entitypatch);
      if (entitypatch instanceof PlayerPatch<?> playerpatch) {
         if (playerpatch.isLogicalClient()) {
            if (playerpatch.getOriginal().m_7578_()) {
               playerpatch.getEventListener()
                  .triggerEvents(PlayerEventListener.EventType.ACTION_EVENT_CLIENT, new ActionEvent<>(playerpatch, this.getAccessor()));
            }
         } else {
            ActionEvent<ServerPlayerPatch> actionEvent = new ActionEvent<>(playerpatch, this.getAccessor());
            playerpatch.getEventListener().triggerEvents(PlayerEventListener.EventType.ACTION_EVENT_SERVER, actionEvent);
            if (actionEvent.shouldResetActionTick()) {
               playerpatch.resetActionTick();
            }
         }
      }
   }

   @Override
   public void tick(LivingEntityPatch<?> entitypatch) {
      super.tick(entitypatch);
      if (entitypatch.getEntityState().movementLocked()) {
         entitypatch.getOriginal().f_267362_.m_267771_(0.0F);
      }
   }

   @Override
   public boolean isMainFrameAnimation() {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Layer.Priority getPriority() {
      return this.getProperty(ClientAnimationProperties.PRIORITY).orElse(Layer.Priority.HIGHEST);
   }
}
