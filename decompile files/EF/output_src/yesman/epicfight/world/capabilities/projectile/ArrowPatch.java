package yesman.epicfight.world.capabilities.projectile;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.AbstractArrow;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;

public class ArrowPatch extends ProjectilePatch<AbstractArrow> {
   protected void setMaxStrikes(AbstractArrow projectileEntity, int maxStrikes) {
      projectileEntity.m_36767_((byte)(maxStrikes - 1));
   }

   @Override
   public EpicFightDamageSource createEpicFightDamageSource() {
      return EpicFightDamageSources.arrow(this.original, this.original.m_19749_())
         .setStunType(StunType.SHORT)
         .addRuntimeTag(DamageTypeTags.f_268524_)
         .setBaseArmorNegation(this.armorNegation)
         .setBaseImpact(this.impact)
         .setInitialPosition(this.initialFirePosition);
   }
}
