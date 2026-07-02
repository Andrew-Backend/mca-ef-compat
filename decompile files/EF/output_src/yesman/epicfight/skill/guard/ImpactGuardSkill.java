package yesman.epicfight.skill.guard;

import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

public class ImpactGuardSkill extends GuardSkill {
   protected float superiorPenalizer;
   protected float damageReducer;

   public static GuardSkill.Builder createEnergizingGuardBuilder() {
      return GuardSkill.createGuardBuilder()
         .addAdvancedGuardMotion(CapabilityItem.WeaponCategories.LONGSWORD, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
         .addAdvancedGuardMotion(
            CapabilityItem.WeaponCategories.SPEAR,
            (item, player) -> item.getStyle(player) == CapabilityItem.Styles.TWO_HAND ? Animations.SPEAR_GUARD_HIT : null
         )
         .addAdvancedGuardMotion(CapabilityItem.WeaponCategories.TACHI, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
         .addAdvancedGuardMotion(CapabilityItem.WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_HIT);
   }

   public ImpactGuardSkill(GuardSkill.Builder builder) {
      super(builder);
   }

   @Override
   public void setParams(CompoundTag parameters) {
      super.setParams(parameters);
      this.superiorPenalizer = parameters.m_128457_("superior_penalizer");
      this.damageReducer = parameters.m_128457_("damage_reducer");
   }

   @Override
   public void guard(SkillContainer container, CapabilityItem itemCapapbility, TakeDamageEvent.Attack event, float knockback, float impact, boolean advanced) {
      boolean canUse = this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapapbility, GuardSkill.BlockType.ADVANCED_GUARD);
      if (event.getDamageSource().m_269533_(DamageTypeTags.f_268415_)) {
         impact = event.getDamage();
      }

      super.guard(container, itemCapapbility, event, knockback, impact, canUse);
   }

   @Override
   public void dealEvent(PlayerPatch<?> playerpatch, TakeDamageEvent.Attack event, boolean advanced) {
      boolean isSpecialSource = isAdvancedBlockableDamageSource(event.getDamageSource());
      if (isSpecialSource) {
         EpicFightDamageSource efsource;
         if (event.getDamageSource() instanceof EpicFightDamageSource epicfightDamagesource) {
            efsource = epicfightDamagesource;
         } else {
            efsource = EpicFightDamageSources.fromVanillaDamageSource(event.getDamageSource());
         }

         efsource.addRuntimeTag(EpicFightDamageTypeTags.UNBLOCKALBE);
         efsource.addRuntimeTag(EpicFightDamageTypeTags.NO_STUN);
         playerpatch.getOriginal().m_6469_(efsource, event.getDamage() * this.damageReducer);
      }

      event.setResult(isSpecialSource ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.BLOCKED);
      playerpatch.countHurtTime(event.getDamage());
      EpicFightCapabilities.getUnparameterizedEntityPatch(event.getDamageSource().m_7639_(), LivingEntityPatch.class)
         .ifPresent(attackerpatch -> attackerpatch.setLastAttackEntity(playerpatch.getOriginal()));
      if (event.getDamageSource() instanceof EpicFightDamageSource epicfightDamageSource) {
         epicfightDamageSource.setStunType(StunType.NONE);
      }

      event.setCanceled(true);
      Entity directEntity = event.getDamageSource().m_7640_();
      if (advanced) {
         LivingEntity original = (LivingEntity)playerpatch.getOriginal();
         ((HitParticleType)EpicFightParticles.AIR_BURST.get()).spawnParticleWithArgument((ServerLevel)original.m_9236_(), original, directEntity);
      }

      EpicFightCapabilities.getParameterizedEntityPatch(directEntity, LivingEntity.class, LivingEntityPatch.class)
         .ifPresent(entitypatch -> entitypatch.onAttackBlocked(event.getDamageSource(), playerpatch));
   }

   @Override
   protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
      return !damageSource.m_269533_(DamageTypeTags.f_268738_)
         && !damageSource.m_269533_(EpicFightDamageTypeTags.UNBLOCKALBE)
         && (
            !damageSource.m_269533_(DamageTypeTags.f_268490_)
                  && !damageSource.m_269533_(DamageTypeTags.f_268524_)
                  && !damageSource.m_269533_(DamageTypeTags.f_268415_)
                  && !damageSource.m_276093_(DamageTypes.f_268515_)
                  && !damageSource.m_269533_(DamageTypeTags.f_268745_)
               || advanced
         );
   }

   @Override
   public float getPenalizer(CapabilityItem itemCap) {
      return this.advancedGuardMotions.containsKey(itemCap.getWeaponCategory()) ? this.superiorPenalizer : this.penalizer;
   }

   private static boolean isAdvancedBlockableDamageSource(DamageSource damageSource) {
      return damageSource.m_269533_(DamageTypeTags.f_268415_)
         || damageSource.m_276093_(DamageTypes.f_268515_)
         || damageSource.m_269533_(DamageTypeTags.f_268745_)
         || damageSource.m_269533_(DamageTypeTags.f_268524_)
         || damageSource.m_269533_(DamageTypeTags.f_268490_);
   }

   @Override
   public Skill getPriorSkill() {
      return EpicFightSkills.GUARD;
   }

   @Override
   protected boolean isAdvancedGuard() {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Object> getTooltipArgsOfScreen(List<Object> list) {
      list.add(String.format("%.1f", this.damageReducer * 100.0));
      return list;
   }

   @Override
   public Set<WeaponCategory> getAvailableWeaponCategories() {
      return this.advancedGuardMotions.keySet();
   }
}
