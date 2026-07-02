package yesman.epicfight.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EpicFightEntities {
   public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "epicfight");
   public static final RegistryObject<EntityType<AreaEffectBreath>> AREA_EFFECT_BREATH = ENTITIES.register(
      "area_effect_breath",
      () -> Builder.m_20704_(AreaEffectBreath::new, MobCategory.MISC)
         .m_20719_()
         .m_20699_(6.0F, 0.5F)
         .m_20702_(10)
         .m_20717_(Integer.MAX_VALUE)
         .m_20698_()
         .m_20712_("area_effect_breath")
   );
   public static final RegistryObject<EntityType<DroppedNetherStar>> DROPPED_NETHER_STAR = ENTITIES.register(
      "dropped_nether_star",
      () -> Builder.m_20704_(DroppedNetherStar::new, MobCategory.MISC)
         .m_20699_(0.25F, 0.25F)
         .m_20702_(6)
         .m_20717_(20)
         .m_20698_()
         .m_20712_("dropped_nether_star")
   );
   public static final RegistryObject<EntityType<WitherSkeletonMinion>> WITHER_SKELETON_MINION = ENTITIES.register(
      "wither_skeleton_minion",
      () -> Builder.m_20704_(WitherSkeletonMinion::new, MobCategory.MONSTER)
         .m_20719_()
         .m_20714_(new Block[]{Blocks.f_50070_})
         .m_20699_(0.7F, 2.4F)
         .m_20702_(8)
         .m_20712_("wither_skeleton_minion")
   );
   public static final RegistryObject<EntityType<WitherGhostClone>> WITHER_GHOST_CLONE = ENTITIES.register(
      "wither_ghost", () -> Builder.m_20704_(WitherGhostClone::new, MobCategory.MONSTER).m_20719_().m_20699_(0.9F, 3.5F).m_20702_(10).m_20712_("wither_ghost")
   );
   public static final RegistryObject<EntityType<DeathHarvestOrb>> DEATH_HARVEST_ORB = ENTITIES.register(
      "death_harvest_orb",
      () -> Builder.m_20704_(DeathHarvestOrb::new, MobCategory.MISC)
         .m_20699_(0.5F, 0.5F)
         .m_20702_(6)
         .m_20717_(1)
         .m_20698_()
         .m_20716_()
         .m_20712_("death_harvest_orb")
   );
   public static final RegistryObject<EntityType<DodgeLocationIndicator>> DODGE_LOCATION_INDICATOR = ENTITIES.register(
      "dodge_left",
      () -> Builder.m_20704_(DodgeLocationIndicator::new, MobCategory.MISC)
         .m_20699_(0.0F, 0.0F)
         .m_20702_(6)
         .m_20717_(1)
         .m_20698_()
         .m_20716_()
         .m_20712_("dodge_left")
   );

   public static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
      event.register((EntityType)WITHER_SKELETON_MINION.get(), Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Monster::m_219019_, Operation.OR);
   }
}
