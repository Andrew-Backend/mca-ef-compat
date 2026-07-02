package yesman.epicfight.api.animation.types;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AirSlashAnimation extends AttackAnimation {
   public AirSlashAnimation(
      float transitionTime,
      float antic,
      float contact,
      float recovery,
      @Nullable Collider collider,
      Joint colliderJoint,
      AnimationManager.AnimationAccessor<? extends AirSlashAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      this(transitionTime, antic, antic, contact, recovery, true, collider, colliderJoint, accessor, armature);
   }

   public AirSlashAnimation(
      float transitionTime,
      float antic,
      float preDelay,
      float contact,
      float recovery,
      boolean directional,
      @Nullable Collider collider,
      Joint colliderJoint,
      AnimationManager.AnimationAccessor<? extends AirSlashAnimation> accessor,
      AssetAccessor<? extends Armature> armature
   ) {
      this(transitionTime, accessor, armature, new AttackAnimation.Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, colliderJoint, collider));
      if (directional) {
         this.addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
      }
   }

   public AirSlashAnimation(
      float transitionTime,
      AnimationManager.AnimationAccessor<? extends AirSlashAnimation> accessor,
      AssetAccessor<? extends Armature> armature,
      AttackAnimation.Phase... phases
   ) {
      super(transitionTime, accessor, armature, phases);
      this.addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F));
      this.addProperty(AnimationProperty.AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.5F);
      this.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, false);
      this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true);
   }

   public AirSlashAnimation(float transitionTime, String path, AssetAccessor<? extends Armature> armature, AttackAnimation.Phase... phases) {
      super(transitionTime, path, armature, phases);
      this.addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.5F));
      this.addProperty(AnimationProperty.AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.5F);
      this.addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, false);
      this.addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true);
   }

   @Override
   protected void spawnHitParticle(ServerLevel world, LivingEntityPatch<?> attackerpatch, Entity hit, AttackAnimation.Phase phase) {
      super.spawnHitParticle(world, attackerpatch, hit, phase);
      world.m_8767_(ParticleTypes.f_123797_, hit.m_20185_(), hit.m_20186_(), hit.m_20189_(), 15, 0.0, 0.0, 0.0, 1.0);
   }

   @Override
   public boolean isBasicAttackAnimation() {
      return true;
   }
}
