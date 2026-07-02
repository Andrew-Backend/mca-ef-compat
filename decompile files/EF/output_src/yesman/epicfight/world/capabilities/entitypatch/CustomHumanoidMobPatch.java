package yesman.epicfight.world.capabilities.entitypatch;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
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

public class CustomHumanoidMobPatch<T extends PathfinderMob> extends HumanoidMobPatch<T> {
   private final MobPatchReloadListener.CustomHumanoidMobPatchProvider provider;

   public CustomHumanoidMobPatch(Faction faction, MobPatchReloadListener.CustomHumanoidMobPatchProvider provider) {
      super(faction);
      this.provider = provider;
      this.weaponLivingMotions = this.provider.getHumanoidWeaponMotions();
      this.weaponAttackMotions = this.provider.getHumanoidCombatBehaviors();
   }

   @Override
   public void setAIAsInfantry(boolean holdingRanedWeapon) {
      boolean useBrain = !this.original.m_6274_().f_21845_.isEmpty();
      if (useBrain) {
         if (!holdingRanedWeapon) {
            CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();
            BrainRecomposer.recomposeBrainByType(
               this.original.m_6095_(),
               this.original.m_6274_(),
               builder != null ? new AnimatedCombatBehavior<>(this, builder.build(this)) : null,
               new MoveToTargetSinkStopInaction()
            );
         }
      } else if (!holdingRanedWeapon) {
         CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();
         if (builder != null) {
            this.original.f_21345_.m_25352_(0, new AnimatedAttackGoal<>(this, builder.build(this)));
            this.original.f_21345_.m_25352_(1, new TargetChasingGoal(this, this.getOriginal(), this.provider.getChasingSpeed(), true));
         }
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
      if (this.original.m_6117_()) {
         CapabilityItem activeItem = this.getHoldingItemCapability(this.original.m_7655_());
         UseAnim useAnim = this.original.m_21120_(this.original.m_7655_()).m_41780_();
         UseAnim secondUseAnim = activeItem.getUseAnimation(this);
         if (useAnim == UseAnim.BLOCK || secondUseAnim == UseAnim.BLOCK) {
            if (activeItem.getWeaponCategory() == CapabilityItem.WeaponCategories.SHIELD) {
               this.currentCompositeMotion = LivingMotions.BLOCK_SHIELD;
            } else {
               this.currentCompositeMotion = LivingMotions.BLOCK;
            }
         } else if (useAnim == UseAnim.BOW || useAnim == UseAnim.SPEAR) {
            this.currentCompositeMotion = LivingMotions.AIM;
         } else if (useAnim == UseAnim.CROSSBOW) {
            this.currentCompositeMotion = LivingMotions.RELOAD;
         } else {
            this.currentCompositeMotion = this.currentLivingMotion;
         }
      } else if (CrossbowItem.m_40932_(this.original.m_21205_())) {
         this.currentCompositeMotion = LivingMotions.AIM;
      } else if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation().get().isReboundAnimation()) {
         this.currentCompositeMotion = LivingMotions.NONE;
      } else if (this.original.f_20911_ && this.original.m_21257_().isEmpty()) {
         this.currentCompositeMotion = LivingMotions.DIGGING;
      } else {
         this.currentCompositeMotion = this.currentLivingMotion;
      }
   }

   @Override
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
