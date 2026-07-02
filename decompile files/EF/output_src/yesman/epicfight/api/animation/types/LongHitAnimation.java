package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;

public class LongHitAnimation extends ActionAnimation {
   public LongHitAnimation(
      float transitionTime, AnimationManager.AnimationAccessor<? extends LongHitAnimation> accessor, AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, accessor, armature);
      this.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true);
      this.addProperty(AnimationProperty.ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true);
      this.addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
      this.stateSpectrumBlueprint
         .clear()
         .newTimePair(0.0F, Float.MAX_VALUE)
         .addState(EntityState.TURNING_LOCKED, true)
         .addState(EntityState.MOVEMENT_LOCKED, true)
         .addState(EntityState.UPDATE_LIVING_MOTION, false)
         .addState(EntityState.CAN_BASIC_ATTACK, false)
         .addState(EntityState.CAN_SKILL_EXECUTION, false)
         .addState(EntityState.INACTION, true)
         .addState(EntityState.HURT_LEVEL, 2);
   }

   public LongHitAnimation(float transitionTime, String path, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, Float.MAX_VALUE, path, armature);
      this.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, true);
      this.addProperty(AnimationProperty.ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true);
      this.addProperty(AnimationProperty.StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
      this.stateSpectrumBlueprint
         .clear()
         .newTimePair(0.0F, Float.MAX_VALUE)
         .addState(EntityState.TURNING_LOCKED, true)
         .addState(EntityState.MOVEMENT_LOCKED, true)
         .addState(EntityState.UPDATE_LIVING_MOTION, false)
         .addState(EntityState.CAN_BASIC_ATTACK, false)
         .addState(EntityState.CAN_SKILL_EXECUTION, false)
         .addState(EntityState.INACTION, true)
         .addState(EntityState.HURT_LEVEL, 2);
   }
}
