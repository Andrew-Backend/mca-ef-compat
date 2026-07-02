package yesman.epicfight.world.damagesource;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightDamageTypes {
   public static final ResourceKey<DamageType> SHOCKWAVE = ResourceKey.m_135785_(Registries.f_268580_, EpicFightMod.identifier("shockwave"));
   public static final ResourceKey<DamageType> WITHER_BEAM = ResourceKey.m_135785_(Registries.f_268580_, EpicFightMod.identifier("wither_beam"));

   private EpicFightDamageTypes() {
   }
}
