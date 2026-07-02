package yesman.epicfight.world.capabilities.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class TridentCapability extends RangedWeaponCapability {
   private List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> attackMotion = List.of(
      Animations.TRIDENT_AUTO1, Animations.TRIDENT_AUTO2, Animations.TRIDENT_AUTO3, Animations.SPEAR_DASH, Animations.SPEAR_ONEHAND_AIR_SLASH
   );
   private List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> mountAttackMotion = List.of(Animations.SPEAR_MOUNT_ATTACK);

   public TridentCapability(CapabilityItem.Builder builder) {
      super(builder);
   }

   @Override
   public Style getStyle(LivingEntityPatch<?> entitypatch) {
      return CapabilityItem.Styles.ONE_HAND;
   }

   @Override
   public SoundEvent getHitSound() {
      return (SoundEvent)EpicFightSounds.BLADE_HIT.get();
   }

   @Override
   public HitParticleType getHitParticle() {
      return (HitParticleType)EpicFightParticles.HIT_BLADE.get();
   }

   @Override
   public List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> getAutoAttackMotion(PlayerPatch<?> playerpatch) {
      return this.attackMotion;
   }

   @Override
   public List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> getMountAttackMotion() {
      return this.mountAttackMotion;
   }

   @Override
   public LivingMotion getLivingMotion(LivingEntityPatch<?> entitypatch, InteractionHand hand) {
      return entitypatch.getOriginal().m_6117_() && entitypatch.getOriginal().m_21211_().m_41780_() == UseAnim.SPEAR ? LivingMotions.AIM : null;
   }

   @Nullable
   @Override
   public Skill getInnateSkill(PlayerPatch<?> playerpatch, ItemStack itemstack) {
      if (EnchantmentHelper.m_44932_(itemstack) > 0) {
         return EpicFightSkills.TSUNAMI;
      } else if (EnchantmentHelper.m_44936_(itemstack)) {
         return EpicFightSkills.WRATHFUL_LIGHTING;
      } else {
         return EnchantmentHelper.m_44928_(itemstack) > 0 ? EpicFightSkills.EVERLASTING_ALLEGIANCE : null;
      }
   }
}
