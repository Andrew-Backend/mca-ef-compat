package yesman.epicfight.api.animation.types;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.DodgeLocationIndicator;

public class DodgeAnimation extends ActionAnimation {
   public static final Function<DamageSource, AttackResult.ResultType> DODGEABLE_SOURCE_VALIDATOR = damagesource -> damagesource.m_7639_() != null
         && !damagesource.m_269533_(DamageTypeTags.f_268415_)
         && !damagesource.m_276093_(DamageTypes.f_268515_)
         && !damagesource.m_269533_(DamageTypeTags.f_268490_)
         && !damagesource.m_269533_(DamageTypeTags.f_268738_)
         && !damagesource.m_269533_(EpicFightDamageTypeTags.BYPASS_DODGE)
      ? AttackResult.ResultType.MISSED
      : AttackResult.ResultType.SUCCESS;
   public static final Consumer<ProjectileImpactEvent> IGNORE_ALL_PROJECTILES = event -> event.setImpactResult(ImpactResult.SKIP_ENTITY);

   public DodgeAnimation(
      float transitionTime,
      AnimationManager.AnimationAccessor<? extends DodgeAnimation> accessor,
      float width,
      float height,
      AssetAccessor<? extends Armature> armature
   ) {
      this(transitionTime, 10.0F, accessor, width, height, armature);
   }

   public DodgeAnimation(
      float transitionTime,
      float delayTime,
      AnimationManager.AnimationAccessor<? extends DodgeAnimation> accessor,
      float width,
      float height,
      AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, delayTime, accessor, armature);
      this.stateSpectrumBlueprint
         .clear()
         .newTimePair(0.0F, delayTime)
         .addState(EntityState.TURNING_LOCKED, true)
         .addState(EntityState.MOVEMENT_LOCKED, true)
         .addState(EntityState.UPDATE_LIVING_MOTION, false)
         .addState(EntityState.CAN_BASIC_ATTACK, false)
         .addState(EntityState.CAN_SKILL_EXECUTION, false)
         .addState(EntityState.INACTION, true)
         .newTimePair(0.0F, Float.MAX_VALUE)
         .addState(EntityState.ATTACK_RESULT, DODGEABLE_SOURCE_VALIDATOR)
         .addState(EntityState.PROJECTILE_IMPACT_RESULT, IGNORE_ALL_PROJECTILES);
      this.addProperty(AnimationProperty.ActionAnimationProperty.AFFECT_SPEED, true);
      this.addEvents(
         AnimationProperty.StaticAnimationProperty.ON_END_EVENTS,
         AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
      );
      this.addEvents(
         AnimationProperty.StaticAnimationProperty.TICK_EVENTS,
         AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
            .params(EntityDimensions.m_20395_(width, height))
      );
   }

   @Override
   public void begin(LivingEntityPatch<?> entitypatch) {
      super.begin(entitypatch);
      if (!entitypatch.isLogicalClient() && entitypatch != null) {
         entitypatch.getOriginal().m_9236_().m_7967_(new DodgeLocationIndicator(entitypatch));
      }
   }
}
