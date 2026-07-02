package yesman.epicfight.api.animation.types;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

public class KnockdownAnimation extends LongHitAnimation {
   public KnockdownAnimation(
      float transitionTime, AnimationManager.AnimationAccessor<? extends KnockdownAnimation> accessor, AssetAccessor<? extends Armature> armature
   ) {
      super(transitionTime, accessor, armature);
      this.stateSpectrumBlueprint
         .addState(EntityState.KNOCKDOWN, true)
         .addState(
            EntityState.ATTACK_RESULT,
            damagesource -> {
               if (damagesource.m_7639_() == null
                  || damagesource.m_269533_(DamageTypeTags.f_268415_)
                  || damagesource.m_276093_(DamageTypes.f_268515_)
                  || damagesource.m_269533_(DamageTypeTags.f_268738_)) {
                  return AttackResult.ResultType.SUCCESS;
               }

               if (damagesource instanceof EpicFightDamageSource epicfight$damagesource) {
                  if (epicfight$damagesource.m_269533_(EpicFightDamageTypeTags.FINISHER)) {
                     epicfight$damagesource.setStunType(StunType.NONE);
                     return AttackResult.ResultType.SUCCESS;
                  } else {
                     return AttackResult.ResultType.BLOCKED;
                  }
               } else {
                  return AttackResult.ResultType.BLOCKED;
               }
            }
         );
   }

   public KnockdownAnimation(float transitionTime, String path, AssetAccessor<? extends Armature> armature) {
      super(transitionTime, path, armature);
      this.stateSpectrumBlueprint
         .addState(EntityState.KNOCKDOWN, true)
         .addState(
            EntityState.ATTACK_RESULT,
            damagesource -> {
               if (damagesource.m_7639_() == null
                  || damagesource.m_269533_(DamageTypeTags.f_268415_)
                  || damagesource.m_276093_(DamageTypes.f_268515_)
                  || damagesource.m_269533_(DamageTypeTags.f_268738_)) {
                  return AttackResult.ResultType.SUCCESS;
               }

               if (damagesource instanceof EpicFightDamageSource epicfight$damagesource) {
                  if (epicfight$damagesource.m_269533_(EpicFightDamageTypeTags.FINISHER)) {
                     epicfight$damagesource.setStunType(StunType.NONE);
                     return AttackResult.ResultType.SUCCESS;
                  } else {
                     return AttackResult.ResultType.BLOCKED;
                  }
               } else {
                  return AttackResult.ResultType.BLOCKED;
               }
            }
         );
   }
}
