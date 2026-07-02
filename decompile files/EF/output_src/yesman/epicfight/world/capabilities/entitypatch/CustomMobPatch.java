package yesman.epicfight.world.capabilities.entitypatch;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.behavior.AnimatedCombatBehavior;
import yesman.epicfight.world.entity.ai.behavior.MoveToTargetSinkStopInaction;
import yesman.epicfight.world.entity.ai.brain.BrainRecomposer;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class CustomMobPatch<T extends PathfinderMob> extends MobPatch<T> {
   private final MobPatchReloadListener.CustomMobPatchProvider provider;

   public CustomMobPatch(Faction faction, MobPatchReloadListener.CustomMobPatchProvider provider) {
      super(faction);
      this.provider = provider;
   }

   @Override
   protected void initAI() {
      super.initAI();
      boolean useBrain = !this.original.m_6274_().f_21845_.isEmpty();
      CombatBehaviors<CustomMobPatch<T>> combatBehaviors = (CombatBehaviors<CustomMobPatch<T>>)((CombatBehaviors.Builder<CustomMobPatch<T>>)this.provider
         .getCombatBehaviorsBuilder())
         .build(this);
      if (useBrain) {
         BrainRecomposer.recomposeBrainByType(
            this.original.m_6095_(), this.original.m_6274_(), new AnimatedCombatBehavior<>(this, combatBehaviors), new MoveToTargetSinkStopInaction()
         );
      } else {
         this.original.f_21345_.m_25352_(0, new AnimatedAttackGoal<>(this, combatBehaviors));
         this.original.f_21345_.m_25352_(1, new TargetChasingGoal(this, this.getOriginal(), this.provider.getChasingSpeed(), true));
      }
   }

   @Override
   public void initAttributesFromCompound(CompoundTag compoundTag) {
      super.initAttributesFromCompound(compoundTag);
      this.original
         .m_21051_((Attribute)EpicFightAttributes.MAX_STRIKES.get())
         .m_22100_(this.provider.getAttributeValues().getDouble(EpicFightAttributes.MAX_STRIKES.get()));
      this.original
         .m_21051_((Attribute)EpicFightAttributes.ARMOR_NEGATION.get())
         .m_22100_(this.provider.getAttributeValues().getDouble(EpicFightAttributes.ARMOR_NEGATION.get()));
      this.original
         .m_21051_((Attribute)EpicFightAttributes.IMPACT.get())
         .m_22100_(this.provider.getAttributeValues().getDouble(EpicFightAttributes.IMPACT.get()));
      this.original
         .m_21051_((Attribute)EpicFightAttributes.STUN_ARMOR.get())
         .m_22100_(this.provider.getAttributeValues().getDouble(EpicFightAttributes.STUN_ARMOR.get()));
      if (this.provider.getAttributeValues().containsKey(Attributes.f_22281_)) {
         this.original.m_21051_(Attributes.f_22281_).m_22100_(this.provider.getAttributeValues().getDouble(Attributes.f_22281_));
      }
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);

      for (Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> pair : this.provider.getDefaultAnimations()) {
         animator.addLivingAnimation((LivingMotion)pair.getFirst(), (AssetAccessor<? extends StaticAnimation>)pair.getSecond());
      }
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      super.commonAggressiveMobUpdateMotion(considerInaction);
   }

   public AnimationManager.AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
      return this.provider.getStunAnimations().get(stunType);
   }

   @Override
   public SoundEvent getWeaponHitSound(InteractionHand hand) {
      CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
      return itemCap.isEmpty() ? this.provider.getHitSound() : itemCap.getHitSound();
   }

   @Override
   public SoundEvent getSwingSound(InteractionHand hand) {
      CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
      return itemCap.isEmpty() ? this.provider.getSwingSound() : itemCap.getSmashingSound();
   }

   @Override
   public HitParticleType getWeaponHitParticle(InteractionHand hand) {
      CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
      return itemCap.isEmpty() ? this.provider.getHitParticle() : itemCap.getHitParticle();
   }

   @Override
   public OpenMatrix4f getModelMatrix(float partialTicks) {
      float scale = this.provider.getScale();
      return super.getModelMatrix(partialTicks).scale(scale, scale, scale);
   }
}
