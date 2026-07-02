package yesman.epicfight.api.client.animation;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.ConcurrentLinkAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.LayerOffAnimation;
import yesman.epicfight.api.animation.types.LinkAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class Layer {
   protected AssetAccessor<? extends StaticAnimation> nextAnimation;
   protected final LinkAnimation linkAnimation;
   protected final ConcurrentLinkAnimation concurrentLinkAnimation;
   protected final LayerOffAnimation layerOffAnimation;
   protected final Layer.Priority priority;
   protected boolean disabled;
   protected boolean paused;
   public final AnimationPlayer animationPlayer;

   public Layer(Layer.Priority priority) {
      this(priority, AnimationPlayer::new);
   }

   public Layer(Layer.Priority priority, Supplier<AnimationPlayer> animationPlayerProvider) {
      this.animationPlayer = animationPlayerProvider.get();
      this.linkAnimation = new LinkAnimation();
      this.concurrentLinkAnimation = new ConcurrentLinkAnimation();
      this.layerOffAnimation = new LayerOffAnimation(priority);
      this.priority = priority;
      this.disabled = true;
   }

   public void playAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, float transitionTimeModifier) {
      Pose lastPose = this.getCurrentPose(entitypatch);
      if (!this.animationPlayer.isEnd()) {
         this.animationPlayer.getAnimation().get().end(entitypatch, nextAnimation, false);
      }

      this.resume();
      nextAnimation.get().begin(entitypatch);
      if (!nextAnimation.get().isMetaAnimation()) {
         this.setLinkAnimation(nextAnimation, entitypatch, lastPose, transitionTimeModifier);
         this.linkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
         entitypatch.updateEntityState();
         this.nextAnimation = nextAnimation;
      }
   }

   public void playAnimationInstantly(AssetAccessor<? extends DynamicAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
      if (!this.animationPlayer.isEnd()) {
         this.animationPlayer.getAnimation().get().end(entitypatch, nextAnimation, false);
      }

      this.resume();
      nextAnimation.get().begin(entitypatch);
      nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
      entitypatch.updateEntityState();
      this.nextAnimation = null;
   }

   protected void playLivingAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
      if (!this.animationPlayer.isEnd()) {
         this.animationPlayer.getAnimation().get().end(entitypatch, nextAnimation, false);
      }

      this.resume();
      nextAnimation.get().begin(entitypatch);
      if (!nextAnimation.get().isMetaAnimation()) {
         this.concurrentLinkAnimation.acceptFrom(this.animationPlayer.getRealAnimation(), nextAnimation, this.animationPlayer.getElapsedTime());
         this.concurrentLinkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
         entitypatch.updateEntityState();
         this.nextAnimation = nextAnimation;
      }
   }

   protected Pose getCurrentPose(LivingEntityPatch<?> entitypatch) {
      return entitypatch.getClientAnimator().getPose(0.0F, false);
   }

   protected void setLinkAnimation(
      AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, Pose lastPose, float transitionTimeModifier
   ) {
      AssetAccessor<? extends DynamicAnimation> fromAnimation = this.animationPlayer.isEmpty()
         ? entitypatch.getClientAnimator().baseLayer.animationPlayer.getAnimation()
         : this.animationPlayer.getAnimation();
      if (fromAnimation.get() instanceof LinkAnimation linkAnimation) {
         fromAnimation = linkAnimation.getFromAnimation();
      }

      nextAnimation.get().setLinkAnimation(fromAnimation, lastPose, !this.animationPlayer.isEmpty(), transitionTimeModifier, entitypatch, this.linkAnimation);
      this.linkAnimation.getAnimationClip().setBaked();
   }

   public void update(LivingEntityPatch<?> entitypatch) {
      if (this.paused) {
         this.animationPlayer.setElapsedTime(this.animationPlayer.getElapsedTime());
      } else {
         this.animationPlayer.tick(entitypatch);
      }

      if (!this.animationPlayer.isEnd()) {
         this.animationPlayer.getAnimation().get().tick(entitypatch);
      } else if (!this.paused) {
         if (this.nextAnimation != null) {
            if (!this.animationPlayer.getAnimation().get().isLinkAnimation() && !this.nextAnimation.get().isLinkAnimation()) {
               this.nextAnimation.get().begin(entitypatch);
            }

            this.nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
            this.nextAnimation = null;
         } else if (this.animationPlayer.getAnimation() instanceof LayerOffAnimation) {
            this.animationPlayer.getAnimation().get().end(entitypatch, Animations.EMPTY_ANIMATION, true);
         } else {
            this.off(entitypatch);
         }
      }

      if (this.isBaseLayer()) {
         entitypatch.updateEntityState();
         entitypatch.updateMotion(true);
      }
   }

   public void pause() {
      this.paused = true;
   }

   public void resume() {
      this.paused = false;
      this.disabled = false;
   }

   protected boolean isDisabled() {
      return this.disabled;
   }

   public boolean isOff() {
      return this.isDisabled() || this.animationPlayer.isEmpty();
   }

   protected boolean isBaseLayer() {
      return false;
   }

   public void copyLayerTo(Layer layer, float playbackTime) {
      AssetAccessor<? extends DynamicAnimation> animation;
      if (this.animationPlayer.getAnimation() == this.linkAnimation) {
         this.linkAnimation.copyTo(layer.linkAnimation);
         animation = layer.linkAnimation;
      } else {
         animation = this.animationPlayer.getAnimation();
      }

      layer.animationPlayer.setPlayAnimation(animation);
      layer.animationPlayer.setElapsedTime(this.animationPlayer.getPrevElapsedTime() + playbackTime, this.animationPlayer.getElapsedTime() + playbackTime);
      layer.nextAnimation = this.nextAnimation;
      layer.resume();
   }

   public LivingMotion getLivingMotion(LivingEntityPatch<?> entitypatch, boolean current) {
      return current ? entitypatch.currentLivingMotion : entitypatch.getClientAnimator().currentMotion();
   }

   public Pose getEnabledPose(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion, float partialTick) {
      Pose pose = this.animationPlayer.getCurrentPose(entitypatch, partialTick);
      this.animationPlayer
         .getAnimation()
         .get()
         .getJointMaskEntry(entitypatch, useCurrentMotion)
         .ifPresent(jointEntry -> pose.disableJoint(entry -> jointEntry.isMasked(this.getLivingMotion(entitypatch, useCurrentMotion), (String)entry.getKey())));
      return pose;
   }

   public void off(LivingEntityPatch<?> entitypatch) {
      if (!this.isDisabled() && !(this.animationPlayer.getAnimation() instanceof LayerOffAnimation)) {
         if (this.priority == null) {
            this.disableLayer();
         } else {
            float transitionTimeModifier = entitypatch.getClientAnimator().baseLayer.animationPlayer.getAnimation().get().getTransitionTime();
            setLayerOffAnimation(
               this.animationPlayer.getAnimation(), this.getEnabledPose(entitypatch, false, 1.0F), this.layerOffAnimation, transitionTimeModifier
            );
            this.playAnimationInstantly(this.layerOffAnimation, entitypatch);
         }
      }
   }

   public void disableLayer() {
      this.disabled = true;
      this.animationPlayer.setPlayAnimation(Animations.EMPTY_ANIMATION);
   }

   public static void setLayerOffAnimation(
      AssetAccessor<? extends DynamicAnimation> currentAnimation, Pose currentPose, LayerOffAnimation offAnimation, float transitionTimeModifier
   ) {
      offAnimation.setLastAnimation(currentAnimation.get().getRealAnimation());
      offAnimation.setLastPose(currentPose);
      offAnimation.setTotalTime(transitionTimeModifier);
   }

   public AssetAccessor<? extends DynamicAnimation> getNextAnimation() {
      return this.nextAnimation;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.isBaseLayer() ? "Base Layer(" + ((Layer.BaseLayer)this).baseLayerPriority + ") : " : " Composite Layer(" + this.priority + ") : ");
      sb.append(this.animationPlayer.getAnimation() + " ");
      sb.append(", prev elapsed time: " + this.animationPlayer.getPrevElapsedTime() + " ");
      sb.append(", elapsed time: " + this.animationPlayer.getElapsedTime() + " ");
      sb.append(", total time: " + this.animationPlayer.getAnimation().get().getTotalTime() + " ");
      return sb.toString();
   }

   public static class BaseLayer extends Layer {
      protected Map<Layer.Priority, Layer> compositeLayers = Maps.newLinkedHashMap();
      protected Layer.Priority baseLayerPriority;

      public BaseLayer() {
         this(AnimationPlayer::new);
      }

      public BaseLayer(Supplier<AnimationPlayer> animationPlayerProvider) {
         super(null, animationPlayerProvider);

         for (Layer.Priority priority : Layer.Priority.values()) {
            this.compositeLayers.computeIfAbsent(priority, Layer::new);
         }

         this.baseLayerPriority = Layer.Priority.LOWEST;
      }

      @Override
      public void playAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, float transitionTimeModifier) {
         this.offCompositeLayersLowerThan(entitypatch, nextAnimation);
         super.playAnimation(nextAnimation, entitypatch, transitionTimeModifier);
         this.baseLayerPriority = nextAnimation.get().getPriority();
      }

      @Override
      protected void playLivingAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
         if (!this.animationPlayer.isEnd()) {
            this.animationPlayer.getAnimation().get().end(entitypatch, nextAnimation, false);
         }

         this.resume();
         nextAnimation.get().begin(entitypatch);
         if (!nextAnimation.get().isMetaAnimation()) {
            this.concurrentLinkAnimation.acceptFrom(this.animationPlayer.getRealAnimation(), nextAnimation, this.animationPlayer.getElapsedTime());
            this.concurrentLinkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
            entitypatch.updateEntityState();
            this.nextAnimation = nextAnimation;
         }
      }

      @Override
      public void update(LivingEntityPatch<?> entitypatch) {
         super.update(entitypatch);

         for (Layer layer : this.compositeLayers.values()) {
            layer.update(entitypatch);
         }
      }

      public void offCompositeLayersLowerThan(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends StaticAnimation> nextAnimation) {
         Layer.Priority[] layersToOff = nextAnimation.get().isMainFrameAnimation()
            ? nextAnimation.get().getPriority().lowersAndEqual()
            : nextAnimation.get().getPriority().lowers();

         for (Layer.Priority p : layersToOff) {
            this.compositeLayers.get(p).off(entitypatch);
         }
      }

      public void disableLayer(Layer.Priority priority) {
         this.compositeLayers.get(priority).disableLayer();
      }

      public Layer getLayer(Layer.Priority priority) {
         return this.compositeLayers.get(priority);
      }

      public Layer.Priority getBaseLayerPriority() {
         return this.baseLayerPriority;
      }

      @Override
      public void off(LivingEntityPatch<?> entitypatch) {
      }

      @Override
      protected boolean isDisabled() {
         return false;
      }

      @Override
      protected boolean isBaseLayer() {
         return true;
      }
   }

   public enum LayerType {
      BASE_LAYER,
      COMPOSITE_LAYER;
   }

   public enum Priority {
      LOWEST,
      LOW,
      MIDDLE,
      HIGH,
      HIGHEST;

      public Layer.Priority[] lowers() {
         return Arrays.copyOfRange(values(), 0, this.ordinal());
      }

      public Layer.Priority[] lowersAndEqual() {
         return Arrays.copyOfRange(values(), 0, this.ordinal() + 1);
      }

      public Layer.Priority[] highers() {
         return Arrays.copyOfRange(values(), this.ordinal(), values().length);
      }

      public boolean isHigherThan(Layer.Priority priority) {
         return this.ordinal() > priority.ordinal();
      }

      public boolean isHigherOrEqual(Layer.Priority priority) {
         return this.ordinal() >= priority.ordinal();
      }
   }
}
