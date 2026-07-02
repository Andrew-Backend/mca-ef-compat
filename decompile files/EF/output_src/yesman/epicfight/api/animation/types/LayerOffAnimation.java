package yesman.epicfight.api.animation.types;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class LayerOffAnimation extends DynamicAnimation implements AnimationManager.AnimationAccessor<LayerOffAnimation> {
   private AssetAccessor<? extends DynamicAnimation> lastAnimation;
   private Pose lastPose;
   private final Layer.Priority layerPriority;

   public LayerOffAnimation(Layer.Priority layerPriority) {
      this.layerPriority = layerPriority;
      this.animationClip = new AnimationClip();
   }

   public void setLastPose(Pose pose) {
      this.lastPose = pose;
   }

   @Override
   public void end(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
      if (entitypatch.isLogicalClient() && isEnd) {
         entitypatch.getClientAnimator().baseLayer.disableLayer(this.layerPriority);
      }
   }

   @Override
   public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      Pose lowerLayerPose = entitypatch.getClientAnimator().getComposedLayerPoseBelow(this.layerPriority, Minecraft.m_91087_().m_91296_());
      Pose interpolatedPose = Pose.interpolatePose(this.lastPose, lowerLayerPose, time / this.getTotalTime());
      interpolatedPose.disableJoint(joint -> !this.lastPose.hasTransform((String)joint.getKey()));
      return interpolatedPose;
   }

   @Override
   public Optional<JointMaskEntry> getJointMaskEntry(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion) {
      return this.lastAnimation.get().getJointMaskEntry(entitypatch, useCurrentMotion);
   }

   @Override
   public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
      return this.lastAnimation.get().getProperty(propertyType);
   }

   public void setLastAnimation(AssetAccessor<? extends DynamicAnimation> animation) {
      this.lastAnimation = animation;
   }

   @Override
   public boolean doesHeadRotFollowEntityHead() {
      return this.lastAnimation.get().doesHeadRotFollowEntityHead();
   }

   @Override
   public AssetAccessor<? extends StaticAnimation> getRealAnimation() {
      return Animations.EMPTY_ANIMATION;
   }

   @Override
   public AnimationClip getAnimationClip() {
      return this.animationClip;
   }

   @Override
   public boolean hasTransformFor(String joint) {
      return this.lastPose.hasTransform(joint);
   }

   @Override
   public boolean isLinkAnimation() {
      return true;
   }

   public LayerOffAnimation get() {
      return this;
   }

   @Override
   public ResourceLocation registryName() {
      return null;
   }

   @Override
   public boolean isPresent() {
      return true;
   }

   @Override
   public int id() {
      return -1;
   }

   @Override
   public AnimationManager.AnimationAccessor<? extends LayerOffAnimation> getAccessor() {
      return this;
   }

   @Override
   public boolean inRegistry() {
      return false;
   }
}
