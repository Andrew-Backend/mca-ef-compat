package yesman.epicfight.client.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemOverrides.BakedOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides.PropertyMatcher;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.ModelEvent.ModifyBakingResult;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.event.RenderLevelStageEvent.RegisterStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.particle.AirBurstParticle;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.particle.AshDirectionalParticle;
import yesman.epicfight.client.particle.BladeRushParticle;
import yesman.epicfight.client.particle.BloodParticle;
import yesman.epicfight.client.particle.CatharsisParticle;
import yesman.epicfight.client.particle.CutParticle;
import yesman.epicfight.client.particle.DustParticle;
import yesman.epicfight.client.particle.EnderParticle;
import yesman.epicfight.client.particle.EntityAfterimageParticle;
import yesman.epicfight.client.particle.EviscerateParticle;
import yesman.epicfight.client.particle.FeatherParticle;
import yesman.epicfight.client.particle.ForceFieldEndParticle;
import yesman.epicfight.client.particle.ForceFieldParticle;
import yesman.epicfight.client.particle.GroundSlamParticle;
import yesman.epicfight.client.particle.HitBluntParticle;
import yesman.epicfight.client.particle.HitCutParticle;
import yesman.epicfight.client.particle.LaserParticle;
import yesman.epicfight.client.particle.ProjectileTrailParticle;
import yesman.epicfight.client.particle.TsunamiSplashParticle;
import yesman.epicfight.client.renderer.blockentity.FractureBlockRenderer;
import yesman.epicfight.client.renderer.entity.DroppedNetherStarRenderer;
import yesman.epicfight.client.renderer.entity.WitherGhostRenderer;
import yesman.epicfight.client.renderer.entity.WitherSkeletonMinionRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.level.block.entity.EpicFightBlockEntities;

@EventBusSubscriber(modid = "epicfight", value = Dist.CLIENT, bus = Bus.MOD)
public class ClientModBusEvent {
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onParticleRegistry(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)EpicFightParticles.ENDERMAN_DEATH_EMIT.get(), EnderParticle.EndermanDeathEmitProvider::new);
      event.registerSpriteSet((ParticleType)EpicFightParticles.HIT_BLUNT.get(), HitBluntParticle.Provider::new);
      event.registerSpecial((ParticleType)EpicFightParticles.HIT_BLADE.get(), new HitCutParticle.Provider());
      event.registerSpriteSet((ParticleType)EpicFightParticles.CUT.get(), CutParticle.Provider::new);
      event.registerSpriteSet((ParticleType)EpicFightParticles.NORMAL_DUST.get(), DustParticle.NormalDustProvider::new);
      event.registerSpriteSet((ParticleType)EpicFightParticles.DUST_EXPANSIVE.get(), DustParticle.ExpansiveDustProvider::new);
      event.registerSpriteSet((ParticleType)EpicFightParticles.DUST_CONTRACTIVE.get(), DustParticle.ContractiveDustProvider::new);
      event.registerSpecial((ParticleType)EpicFightParticles.EVISCERATE.get(), new EviscerateParticle.Provider());
      event.registerSpriteSet((ParticleType)EpicFightParticles.BLOOD.get(), BloodParticle.Provider::new);
      event.registerSpriteSet((ParticleType)EpicFightParticles.BLADE_RUSH_SKILL.get(), BladeRushParticle.Provider::new);
      event.registerSpecial((ParticleType)EpicFightParticles.GROUND_SLAM.get(), new GroundSlamParticle.Provider());
      event.registerSpriteSet((ParticleType)EpicFightParticles.BREATH_FLAME.get(), EnderParticle.BreathFlameProvider::new);
      event.registerSpecial((ParticleType)EpicFightParticles.FORCE_FIELD.get(), new ForceFieldParticle.Provider());
      event.registerSpecial((ParticleType)EpicFightParticles.FORCE_FIELD_END.get(), new ForceFieldEndParticle.Provider());
      event.registerSpecial((ParticleType)EpicFightParticles.ADRENALINE_PLAYER_BEATING.get(), new EntityAfterimageParticle.AdrenalineParticleProvider());
      event.registerSpecial((ParticleType)EpicFightParticles.WHITE_AFTERIMAGE.get(), new EntityAfterimageParticle.WhiteAfterimageProvider());
      event.registerSpecial((ParticleType)EpicFightParticles.LASER.get(), new LaserParticle.Provider());
      event.registerSpecial((ParticleType)EpicFightParticles.NEUTRALIZE.get(), new DustParticle.ExpansiveMetaParticle.Provider());
      event.registerSpecial((ParticleType)EpicFightParticles.BOSS_CASTING.get(), new DustParticle.ContractiveMetaParticle.Provider());
      event.registerSpriteSet((ParticleType)EpicFightParticles.TSUNAMI_SPLASH.get(), TsunamiSplashParticle.Provider::new);
      event.registerSpecial((ParticleType)EpicFightParticles.SWING_TRAIL.get(), new AnimationTrailParticle.Provider());
      event.registerSpecial((ParticleType)EpicFightParticles.PROJECTILE_TRAIL.get(), new ProjectileTrailParticle.Provider());
      event.registerSpriteSet((ParticleType)EpicFightParticles.FEATHER.get(), FeatherParticle.Provider::new);
      event.registerSpecial((ParticleType)EpicFightParticles.AIR_BURST.get(), new AirBurstParticle.Provider());
      event.registerSpriteSet((ParticleType)EpicFightParticles.ASH_DIRECTIONAL.get(), AshDirectionalParticle.Provider::new);
      event.registerSpriteSet((ParticleType)EpicFightParticles.CATHARSIS.get(), CatharsisParticle.Provider::new);
   }

   @SubscribeEvent
   public static void registerRenderersEvent(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)EpicFightEntities.AREA_EFFECT_BREATH.get(), NoopRenderer::new);
      event.registerEntityRenderer((EntityType)EpicFightEntities.DROPPED_NETHER_STAR.get(), DroppedNetherStarRenderer::new);
      event.registerEntityRenderer((EntityType)EpicFightEntities.DEATH_HARVEST_ORB.get(), NoopRenderer::new);
      event.registerEntityRenderer((EntityType)EpicFightEntities.DODGE_LOCATION_INDICATOR.get(), NoopRenderer::new);
      event.registerEntityRenderer((EntityType)EpicFightEntities.WITHER_GHOST_CLONE.get(), WitherGhostRenderer::new);
      event.registerEntityRenderer((EntityType)EpicFightEntities.WITHER_SKELETON_MINION.get(), WitherSkeletonMinionRenderer::new);
      event.registerBlockEntityRenderer((BlockEntityType)EpicFightBlockEntities.FRACTURE.get(), FractureBlockRenderer::new);
   }

   @SubscribeEvent
   public static void registerStageEvent(RegisterStageEvent event) {
      RenderItemBase.initItemRenderers(Minecraft.m_91087_());
   }

   @SubscribeEvent
   public static void addLayersEvent(AddLayers event) {
      WearableItemLayer.clearModels();
      ClientEngine.getInstance().renderEngine.reloadEntityRenderers(event.getContext());
      SoftBodyTranslatable.TRACKING_SIMULATION_SUBJECTS.removeIf(ClothSimulatable::invalid);

      for (ClothSimulatable simOwner : SoftBodyTranslatable.TRACKING_SIMULATION_SUBJECTS) {
         simOwner.getClothSimulator().getAllRunningObjects().forEach(entry -> simOwner.getClothSimulator().restart((ResourceLocation)entry.getKey()));
      }
   }

   @SubscribeEvent
   public static void registerGuiOverlaysEvent(RegisterGuiOverlaysEvent event) {
      event.registerAboveAll("stamina_bar", ClientEngine.getInstance().renderEngine.battleModeUI::renderStaminaBar);
      event.registerAboveAll("skills", ClientEngine.getInstance().renderEngine.battleModeUI::renderNormalSkills);
      event.registerAboveAll("weapon_innate", ClientEngine.getInstance().renderEngine.battleModeUI::renderWeaponInnateSkill);
      event.registerAboveAll("charging_bar", ClientEngine.getInstance().renderEngine.battleModeUI::renderCharingBar);
   }

   @SubscribeEvent
   public static void registerAdditionalEvent(RegisterAdditional event) {
      SkillCategory.ENUM_MANAGER
         .universalValues()
         .stream()
         .filter(skillCategory -> !skillCategory.bookIcon().equals(SkillCategory.DEFAULT_BOOK_ICON))
         .forEach(skillCategory -> event.register(new ModelResourceLocation(skillCategory.bookIcon(), "inventory")));
   }

   @SubscribeEvent
   public static void modifyBakingResultEvent(ModifyBakingResult event) {
      ModelResourceLocation skillbookLocation = new ModelResourceLocation(SkillCategory.DEFAULT_BOOK_ICON, "inventory");
      if (event.getModels().containsKey(skillbookLocation)) {
         List<BakedOverride> skillCategoryOverrides = new ArrayList<>();
         SkillCategory.ENUM_MANAGER
            .universalValues()
            .stream()
            .filter(skillCategory -> !skillCategory.bookIcon().equals(SkillCategory.DEFAULT_BOOK_ICON))
            .sorted((c1, c2) -> Integer.compare(c2.universalOrdinal(), c1.universalOrdinal()))
            .forEach(skillCategory -> {
               ModelResourceLocation model = new ModelResourceLocation(skillCategory.bookIcon(), "inventory");
               PropertyMatcher[] propertyMatchers = new PropertyMatcher[]{new PropertyMatcher(0, skillCategory.universalOrdinal())};
               BakedModel bakedModel = (BakedModel)event.getModelBakery().m_119251_().get(model);
               skillCategoryOverrides.add(new BakedOverride(propertyMatchers, bakedModel));
            });
         ItemOverrides overrides = ((BakedModel)event.getModels().get(skillbookLocation)).m_7343_();
         overrides.f_111735_ = skillCategoryOverrides.toArray(BakedOverride[]::new);
         overrides.f_173461_ = new ResourceLocation[]{EpicFightMod.identifier("skill")};
      }
   }
}
