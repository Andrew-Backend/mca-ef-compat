package yesman.epicfight.api.animation.types;

import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class BasicAttackAnimation extends AttackAnimation {
   public BasicAttackAnimation(
      float transitionTime,
      float antic,
      float contact,
      float recovery,
      @Nullable Collider collider,
      Joint colliderJoint,
      AnimationManager.AnimationAccessor<? extends BasicAttackAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      this(transitionTime, antic, antic, contact, recovery, collider, colliderJoint, accessor, armature);
   }

   public BasicAttackAnimation(
      float transitionTime,
      float antic,
      float preDelay,
      float contact,
      float recovery,
      @Nullable Collider collider,
      Joint colliderJoint,
      AnimationManager.AnimationAccessor<? extends BasicAttackAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor, armature);
      this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, true);
      this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false);
      this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
   }

   public BasicAttackAnimation(
      float transitionTime,
      float antic,
      float contact,
      float recovery,
      InteractionHand hand,
      @Nullable Collider collider,
      Joint colliderJoint,
      AnimationManager.AnimationAccessor<? extends BasicAttackAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, antic, antic, contact, recovery, hand, collider, colliderJoint, accessor, armature);
      this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, true);
      this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false);
      this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
   }

   public BasicAttackAnimation(
      float transitionTime,
      AnimationManager.AnimationAccessor<? extends BasicAttackAnimation> accessor,
      AssetAccessor<? extends Armature> armature,
      AttackAnimation.Phase... phases
   ) {
      super(transitionTime, accessor, armature, phases);
      this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, true);
      this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false);
      this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
   }

   public BasicAttackAnimation(float transitionTime, String path, AssetAccessor<? extends Armature> armature, AttackAnimation.Phase... phases) {
      super(transitionTime, path, armature, phases);
      this.addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, true);
      this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, false);
      this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
   }

   @Override
   protected void bindPhaseState(AttackAnimation.Phase phase) {
      float preDelay = phase.preDelay;
      this.stateSpectrumBlueprint
         .newTimePair(phase.start, preDelay)
         .addState(EntityState.PHASE_LEVEL, 1)
         .newTimePair(phase.start, phase.contact)
         .addState(EntityState.CAN_SKILL_EXECUTION, false)
         .newTimePair(phase.start, phase.recovery)
         .addState(EntityState.MOVEMENT_LOCKED, true)
         .addState(EntityState.UPDATE_LIVING_MOTION, false)
         .addState(EntityState.CAN_BASIC_ATTACK, false)
         .newTimePair(phase.start, phase.end)
         .addState(EntityState.INACTION, true)
         .newTimePair(preDelay, phase.contact)
         .addState(EntityState.ATTACKING, true)
         .addState(EntityState.PHASE_LEVEL, 2)
         .newTimePair(phase.contact, phase.end)
         .addState(EntityState.PHASE_LEVEL, 3)
         .addState(EntityState.TURNING_LOCKED, true);
   }

   @Override
   public void loadAnimation() {
      super.loadAnimation();
      if (!this.properties.containsKey(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED)) {
         float basisSpeed = Float.parseFloat(String.format(Locale.US, "%.2f", 1.0F / this.getTotalTime()));
         this.addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, basisSpeed);
      }
   }

   @Override
   public TypeFlexibleHashMap<EntityState.StateFactor<?>> getStatesMap(LivingEntityPatch<?> entitypatch, float time) {
      TypeFlexibleHashMap<EntityState.StateFactor<?>> stateMap = super.getStatesMap(entitypatch, time);
      if (!EpicFightGameRules.STIFF_COMBO_ATTACKS.getRuleValue(entitypatch.getOriginal().m_9236_())) {
         stateMap.put(EntityState.MOVEMENT_LOCKED, false);
         stateMap.put(EntityState.UPDATE_LIVING_MOTION, true);
      }

      return stateMap;
   }

   @Override
   protected Vec3 getCoordVector(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> dynamicAnimation) {
      Vec3 vec3 = super.getCoordVector(entitypatch, dynamicAnimation);
      if (entitypatch.shouldBlockMoving() && this.getProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE).orElse(false)) {
         vec3 = vec3.m_82490_(0.0);
      }

      return vec3;
   }

   @Override
   public Optional<JointMaskEntry> getJointMaskEntry(LivingEntityPatch<?> entitypatch, boolean useCurrentMotion) {
      return entitypatch.isLogicalClient() && entitypatch.getClientAnimator().getPriorityFor(this.getAccessor()) == Layer.Priority.HIGHEST
         ? Optional.of(JointMaskEntry.BASIC_ATTACK_MASK)
         : super.getJointMaskEntry(entitypatch, useCurrentMotion);
   }

   @Override
   public boolean isBasicAttackAnimation() {
      return true;
   }

   @Override
   public boolean shouldPlayerMove(LocalPlayerPatch playerpatch) {
      return playerpatch.isLogicalClient() ? EpicFightGameRules.STIFF_COMBO_ATTACKS.getRuleValue(playerpatch.getOriginal().m_9236_()) : true;
   }

   @OnlyIn(Dist.CLIENT)
   private static boolean isPlayerMoving(LocalPlayerPatch localPlayerPatch) {
      PlayerInputState inputState = InputManager.getInputState(localPlayerPatch.getOriginal());
      return inputState.forwardImpulse() != 0.0F || inputState.leftImpulse() != 0.0F;
   }
}
