package yesman.epicfight.skill.guard;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

public class ParryingSkill extends GuardSkill {
   private int PARRY_WINDOW;

   public static GuardSkill.Builder createActiveGuardBuilder() {
      return GuardSkill.createGuardBuilder()
         .addAdvancedGuardMotion(
            CapabilityItem.WeaponCategories.SWORD,
            (itemCap, playerpatch) -> itemCap.getStyle(playerpatch) == CapabilityItem.Styles.ONE_HAND
               ? List.of(Animations.SWORD_GUARD_ACTIVE_HIT1, Animations.SWORD_GUARD_ACTIVE_HIT2)
               : List.of(Animations.SWORD_GUARD_ACTIVE_HIT2, Animations.SWORD_GUARD_ACTIVE_HIT3)
         )
         .addAdvancedGuardMotion(
            CapabilityItem.WeaponCategories.LONGSWORD,
            (itemCap, playerpatch) -> List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2)
         )
         .addAdvancedGuardMotion(
            CapabilityItem.WeaponCategories.UCHIGATANA,
            (itemCap, playerpatch) -> List.of(Animations.SWORD_GUARD_ACTIVE_HIT1, Animations.SWORD_GUARD_ACTIVE_HIT2)
         )
         .addAdvancedGuardMotion(
            CapabilityItem.WeaponCategories.TACHI,
            (itemCap, playerpatch) -> List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2)
         );
   }

   public ParryingSkill(GuardSkill.Builder builder) {
      super(builder);
   }

   @Override
   public void startHolding(SkillContainer container) {
      super.startHolding(container);
      if (!container.getExecutor().isLogicalClient()) {
         int lastActive = container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.LAST_ACTIVE.get());
         if (container.getServerExecutor().getOriginal().f_19797_ - lastActive > this.PARRY_WINDOW * 2) {
            container.getDataManager()
               .setDataSync((SkillDataKey<Integer>)SkillDataKeys.LAST_ACTIVE.get(), container.getServerExecutor().getOriginal().f_19797_);
         }
      }
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
   }

   @Override
   public void guard(SkillContainer container, CapabilityItem itemCapability, TakeDamageEvent.Attack event, float knockback, float impact, boolean advanced) {
      if (this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapability, GuardSkill.BlockType.ADVANCED_GUARD)) {
         DamageSource damageSource = event.getDamageSource();
         Entity offender = getOffender(damageSource);
         if (offender != null && this.isBlockableSource(damageSource, true)) {
            ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
            boolean successParrying = serverPlayer.f_19797_
                  - container.getDataManager().<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.LAST_ACTIVE.get())
               < this.PARRY_WINDOW;
            float penalty = container.getDataManager().<Float>getDataValue((SkillDataKey<Float>)SkillDataKeys.PENALTY.get());
            event.getPlayerPatch().playSound((SoundEvent)EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
            ((HitParticleType)EpicFightParticles.HIT_BLUNT.get())
               .spawnParticleWithArgument(serverPlayer.m_284548_(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serverPlayer, offender);
            if (successParrying) {
               event.setParried(true);
               penalty = 0.1F;
               knockback *= 0.4F;
               container.getDataManager().setData((SkillDataKey<Integer>)SkillDataKeys.LAST_ACTIVE.get(), 0);
            } else {
               penalty += this.getPenalizer(itemCapability);
               container.getDataManager().setDataSync((SkillDataKey<Float>)SkillDataKeys.PENALTY.get(), penalty);
            }

            if (offender instanceof LivingEntity livingentity) {
               knockback += EnchantmentHelper.m_44894_(livingentity) * 0.1F;
            }

            assert offender != null;
            event.getPlayerPatch().knockBackEntity(offender.m_20182_(), knockback);
            float consumeAmount = penalty * impact;
            boolean canAfford = event.getPlayerPatch().consumeForSkill(this, Skill.Resource.STAMINA, consumeAmount);
            GuardSkill.BlockType blockType = successParrying
               ? GuardSkill.BlockType.ADVANCED_GUARD
               : (canAfford ? GuardSkill.BlockType.GUARD : GuardSkill.BlockType.GUARD_BREAK);
            AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = this.getGuardMotion(
               container, event.getPlayerPatch(), itemCapability, blockType
            );
            if (animation != null) {
               event.getPlayerPatch().playAnimationSynchronized(animation, 0.0F);
            }

            if (blockType == GuardSkill.BlockType.GUARD_BREAK) {
               event.getPlayerPatch().playSound((SoundEvent)EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
            }

            this.dealEvent(event.getPlayerPatch(), event, advanced);
            return;
         }
      }

      super.guard(container, itemCapability, event, knockback, impact, false);
   }

   @Override
   protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
      return damageSource.m_269533_(DamageTypeTags.f_268524_) && advanced || super.isBlockableSource(damageSource, false);
   }

   @Nullable
   @Override
   protected AnimationManager.AnimationAccessor<? extends StaticAnimation> getGuardMotion(
      SkillContainer container, PlayerPatch<?> playerpatch, CapabilityItem itemCapability, GuardSkill.BlockType blockType
   ) {
      AnimationManager.AnimationAccessor<? extends StaticAnimation> animation = itemCapability.getGuardMotion(this, blockType, playerpatch);
      if (animation != null) {
         return animation;
      }

      if (blockType == GuardSkill.BlockType.ADVANCED_GUARD) {
         List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> motions = (List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>)this.getGuardMotionMap(
               blockType
            )
            .getOrDefault(itemCapability.getWeaponCategory(), (a, b) -> null)
            .apply(itemCapability, playerpatch);
         if (motions != null) {
            SkillDataManager dataManager = container.getDataManager();
            int motionCounter = dataManager.<Integer>getDataValue((SkillDataKey<Integer>)SkillDataKeys.PARRY_MOTION_COUNTER.get());
            dataManager.setDataF((SkillDataKey<Integer>)SkillDataKeys.PARRY_MOTION_COUNTER.get(), v -> v + 1);
            motionCounter %= motions.size();
            return motions.get(motionCounter);
         }
      }

      return super.getGuardMotion(container, playerpatch, itemCapability, blockType);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.PARRY_WINDOW = parameters.m_128451_("parry_window");
      if (this.PARRY_WINDOW <= 0) {
         this.PARRY_WINDOW = 8;
      }
   }

   @Override
   public Skill getPriorSkill() {
      return EpicFightSkills.GUARD;
   }

   @Override
   protected boolean isAdvancedGuard() {
      return true;
   }

   @Override
   public Set<WeaponCategory> getAvailableWeaponCategories() {
      return this.advancedGuardMotions.keySet();
   }
}
