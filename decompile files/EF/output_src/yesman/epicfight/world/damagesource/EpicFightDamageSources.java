package yesman.epicfight.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.WitherSkull;

public final class EpicFightDamageSources {
   private EpicFightDamageSources() {
   }

   public static EpicFightDamageSource fromVanillaDamageSource(DamageSource damageSource) {
      return new EpicFightDamageSource(damageSource);
   }

   public static EpicFightDamageSource shockwave(LivingEntity owner) {
      return new EpicFightDamageSource(getDamageTypeHolder(owner, EpicFightDamageTypes.SHOCKWAVE), owner, owner, null);
   }

   public static EpicFightDamageSource witherBeam(LivingEntity owner) {
      return new EpicFightDamageSource(getDamageTypeHolder(owner, EpicFightDamageTypes.WITHER_BEAM), owner, owner, null);
   }

   public static EpicFightDamageSource arrow(AbstractArrow arrow, @Nullable Entity shooter) {
      return new EpicFightDamageSource(getDamageTypeHolder(arrow, DamageTypes.f_268739_), arrow, shooter, null);
   }

   public static EpicFightDamageSource trident(Entity trident, Entity thrower) {
      return new EpicFightDamageSource(getDamageTypeHolder(trident, DamageTypes.f_268714_), trident, thrower, null);
   }

   public static EpicFightDamageSource witherSkull(WitherSkull witherSkull, Entity shooter) {
      return new EpicFightDamageSource(getDamageTypeHolder(witherSkull, DamageTypes.f_268641_), witherSkull, shooter, null);
   }

   public static EpicFightDamageSource mobAttack(LivingEntity owner) {
      return new EpicFightDamageSource(getDamageTypeHolder(owner, DamageTypes.f_268566_), owner, owner, null);
   }

   public static EpicFightDamageSource playerAttack(Player owner) {
      return new EpicFightDamageSource(getDamageTypeHolder(owner, DamageTypes.f_268464_), owner, owner, null);
   }

   public static EpicFightDamageSource enderDragonBreath(LivingEntity owner, Entity directEntity) {
      return fromVanillaDamageSource(owner.m_9236_().m_269111_().m_269104_(directEntity, owner));
   }

   private static Holder<DamageType> getDamageTypeHolder(Entity entity, ResourceKey<DamageType> damageTypeKey) {
      return entity.m_9236_().m_9598_().m_175515_(Registries.f_268580_).m_246971_(damageTypeKey);
   }
}
