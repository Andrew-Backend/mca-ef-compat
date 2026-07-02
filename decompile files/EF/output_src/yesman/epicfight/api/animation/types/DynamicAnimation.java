package yesman.epicfight.api.animation.types;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class DynamicAnimation {
   protected final boolean isRepeat;
   protected final float transitionTime;
   protected AnimationClip animationClip;

   public DynamicAnimation() {
      this(0.15F, false);
   }

   public DynamicAnimation(float transitionTime, boolean isRepeat) {
      this.isRepeat = isRepeat;
      this.transitionTime = transitionTime;
   }

   public final Pose getRawPose(float time) {
      return this.getAnimationClip().getPoseInTime(time);
   }

   public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
      Pose pose = this.getRawPose(time);
      this.modifyPose(this, pose, entitypatch, time, partialTicks);
      return pose;
   }

   public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
   }

   public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
      animationPlayer.setPlayAnimation(this.getAccessor());
      animationPlayer.tick(entitypatch);
      animationPlayer.begin(this.getAccessor(), entitypatch);
   }

   public void begin(LivingEntityPatch<?> entitypatch) {
   }

   public void tick(LivingEntityPatch<?> entitypatch) {
   }

   public void end(LivingEntityPatch<?> entitypatch, @Nullable AssetAccessor<? extends DynamicAnimation> nextAnimation, boolean isEnd) {
   }

   public void linkTick(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> linkAnimation) {
   }

   public boolean hasTransformFor(String joint) {
      return this.getTransfroms().containsKey(joint);
   }

   @OnlyIn(Dist.CLIENT)
   public Optional<JointMaskEntry> getJointMaskEntry(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion) {
      return Optional.empty();
   }

   public EntityState getState(LivingEntityPatch<?> entitypatch, float time) {
      return EntityState.DEFAULT_STATE;
   }

   public TypeFlexibleHashMap<EntityState.StateFactor<?>> getStatesMap(LivingEntityPatch<?> entitypatch, float time) {
      return new TypeFlexibleHashMap<>(false);
   }

   public <T> T getState(EntityState.StateFactor<T> stateFactor, LivingEntityPatch<?> entitypatch, float time) {
      return stateFactor.defaultValue();
   }

   public AnimationClip getAnimationClip() {
      return this.animationClip;
   }

   public Map<String, TransformSheet> getTransfroms() {
      return this.getAnimationClip().getJointTransforms();
   }

   public float getPlaySpeed(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
      return 1.0F;
   }

   public TransformSheet getCoord() {
      return this.getTransfroms().containsKey("Root") ? this.getTransfroms().get("Root") : TransformSheet.EMPTY_SHEET;
   }

   public void setTotalTime(float totalTime) {
      this.getAnimationClip().setClipTime(totalTime);
   }

   public float getTotalTime() {
      return this.getAnimationClip().getClipTime();
   }

   public float getTransitionTime() {
      return this.transitionTime;
   }

   public boolean isRepeat() {
      return this.isRepeat;
   }

   public boolean canBePlayedReverse() {
      return false;
   }

   public ResourceLocation getRegistryName() {
      return EpicFightMod.identifier("");
   }

   public int getId() {
      return -1;
   }

   public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
      return Optional.empty();
   }

   public boolean isBasicAttackAnimation() {
      return false;
   }

   public boolean isMainFrameAnimation() {
      return false;
   }

   public boolean isReboundAnimation() {
      return false;
   }

   public boolean isMetaAnimation() {
      return false;
   }

   public boolean isClientAnimation() {
      return false;
   }

   public boolean isStaticAnimation() {
      return false;
   }

   public abstract <A extends DynamicAnimation> AnimationManager.AnimationAccessor<? extends DynamicAnimation> getAccessor();

   public abstract AssetAccessor<? extends StaticAnimation> getRealAnimation();

   public boolean isLinkAnimation() {
      return false;
   }

   public boolean doesHeadRotFollowEntityHead() {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public void renderDebugging(PoseStack poseStack, MultiBufferSource buffer, LivingEntityPatch<?> entitypatch, float playTime, float partialTicks) {
   }
}
