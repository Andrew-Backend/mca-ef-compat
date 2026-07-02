package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

public class PatchedPhases {
   public static final EnderDragonPhase<DragonFlyingPhase> FLYING = EnderDragonPhase.m_31402_(DragonFlyingPhase.class, "Flying");
   public static final EnderDragonPhase<DragonGroundBattlePhase> GROUND_BATTLE = EnderDragonPhase.m_31402_(DragonGroundBattlePhase.class, "Ground Battle");
   public static final EnderDragonPhase<DragonLandingPhase> LANDING = EnderDragonPhase.m_31402_(DragonLandingPhase.class, "Landing");
   public static final EnderDragonPhase<DragonAirstrikePhase> AIRSTRIKE = EnderDragonPhase.m_31402_(DragonAirstrikePhase.class, "Airstrike");
   public static final EnderDragonPhase<DragonCrystalLinkPhase> CRYSTAL_LINK = EnderDragonPhase.m_31402_(DragonCrystalLinkPhase.class, "Crystal Link");
   public static final EnderDragonPhase<DragonNeutralizedPhase> NEUTRALIZED = EnderDragonPhase.m_31402_(DragonNeutralizedPhase.class, "Neutralized");
}
