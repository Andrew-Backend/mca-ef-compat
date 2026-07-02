package forge.net.mca.forge;

import forge.net.mca.Config;
import forge.net.mca.KeyBindings;
import forge.net.mca.ModelPredicatesMCA;
import forge.net.mca.ParticleTypesMCA;
import forge.net.mca.block.BlockEntityTypesMCA;
import forge.net.mca.block.BlocksMCA;
import forge.net.mca.client.gui.MCAScreens;
import forge.net.mca.client.particle.InteractionParticle;
import forge.net.mca.client.render.CribEntityRenderer;
import forge.net.mca.client.render.GrimReaperRenderer;
import forge.net.mca.client.render.TombstoneBlockEntityRenderer;
import forge.net.mca.client.render.VillagerEntityMCARenderer;
import forge.net.mca.client.render.ZombieVillagerEntityMCARenderer;
import forge.net.mca.client.resources.ColorPaletteLoader;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.resources.ApiReloadListener;
import forge.net.mca.resources.Supporters;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = "mca", value = Dist.CLIENT, bus = Bus.MOD)
public final class MCAForgeClient {
   @SubscribeEvent
   public static void data(RegisterClientReloadListenersEvent event) {
      new ClientProxyImpl();
      event.registerReloadListener(new MCAScreens());
      event.registerReloadListener(new ColorPaletteLoader());
      event.registerReloadListener(new Supporters());
      event.registerReloadListener(new ApiReloadListener());
   }

   @SubscribeEvent
   public static void setup(FMLClientSetupEvent event) {
      if (Config.getInstance().useSquidwardModels) {
         EntityRenderers.m_174036_((EntityType)EntitiesMCA.MALE_VILLAGER.get(), VillagerRenderer::new);
         EntityRenderers.m_174036_((EntityType)EntitiesMCA.FEMALE_VILLAGER.get(), VillagerRenderer::new);
         EntityRenderers.m_174036_((EntityType)EntitiesMCA.MALE_ZOMBIE_VILLAGER.get(), ZombieVillagerRenderer::new);
         EntityRenderers.m_174036_((EntityType)EntitiesMCA.FEMALE_ZOMBIE_VILLAGER.get(), ZombieVillagerRenderer::new);
      } else {
         EntityRenderers.m_174036_((EntityType)EntitiesMCA.MALE_VILLAGER.get(), VillagerEntityMCARenderer::new);
         EntityRenderers.m_174036_((EntityType)EntitiesMCA.FEMALE_VILLAGER.get(), VillagerEntityMCARenderer::new);
         EntityRenderers.m_174036_((EntityType)EntitiesMCA.MALE_ZOMBIE_VILLAGER.get(), ZombieVillagerEntityMCARenderer::new);
         EntityRenderers.m_174036_((EntityType)EntitiesMCA.FEMALE_ZOMBIE_VILLAGER.get(), ZombieVillagerEntityMCARenderer::new);
      }

      EntityRenderers.m_174036_((EntityType)EntitiesMCA.GRIM_REAPER.get(), GrimReaperRenderer::new);
      EntityRenderers.m_174036_((EntityType)EntitiesMCA.CRIB.get(), CribEntityRenderer::new);
      BlockEntityRenderers.m_173590_((BlockEntityType)BlockEntityTypesMCA.TOMBSTONE.get(), TombstoneBlockEntityRenderer::new);
      ModelPredicatesMCA.setup(ItemProperties::register);
      ItemBlockRenderTypes.setRenderLayer((Block)BlocksMCA.INFERNAL_FLAME.get(), RenderType.m_110463_());
   }

   @SubscribeEvent
   public static void onKeyRegister(RegisterKeyMappingsEvent event) {
      KeyBindings.list.forEach(event::register);
   }

   @SubscribeEvent
   public static void onParticleFactoryRegistration(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)ParticleTypesMCA.NEG_INTERACTION.get(), InteractionParticle.Factory::new);
      event.registerSpriteSet((ParticleType)ParticleTypesMCA.POS_INTERACTION.get(), InteractionParticle.Factory::new);
   }
}
