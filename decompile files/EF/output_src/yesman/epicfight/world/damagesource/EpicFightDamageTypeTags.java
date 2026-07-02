package yesman.epicfight.world.damagesource;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import yesman.epicfight.main.EpicFightMod;

public interface EpicFightDamageTypeTags {
   TagKey<DamageType> FINISHER = create("finisher");
   TagKey<DamageType> COUNTER = create("counter");
   TagKey<DamageType> EXECUTION = create("execution");
   TagKey<DamageType> WEAPON_INNATE = create("weapon_innate");
   TagKey<DamageType> GUARD_PUNCTURE = create("guard_puncture");
   TagKey<DamageType> UNBLOCKALBE = create("unblockable");
   TagKey<DamageType> NO_STUN = create("no_stun");
   TagKey<DamageType> BYPASS_DODGE = create("bypass_dodge");
   TagKey<DamageType> NONE = create("none");
   TagKey<DamageType> IS_MELEE = create("is_melee");
   TagKey<DamageType> IS_MAGIC = create("is_magic");

   private static TagKey<DamageType> create(String tagName) {
      return TagKey.m_203882_(Registries.f_268580_, EpicFightMod.identifier(tagName));
   }
}
