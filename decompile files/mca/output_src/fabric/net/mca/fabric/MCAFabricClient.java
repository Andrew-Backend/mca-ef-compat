package fabric.net.mca.fabric;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import fabric.net.mca.ClientProxyAbstractImpl;
import fabric.net.mca.Config;
import fabric.net.mca.KeyBindings;
import fabric.net.mca.MCAClient;
import fabric.net.mca.ModelPredicatesMCA;
import fabric.net.mca.ParticleTypesMCA;
import fabric.net.mca.block.BlockEntityTypesMCA;
import fabric.net.mca.block.BlocksMCA;
import fabric.net.mca.client.particle.InteractionParticle;
import fabric.net.mca.client.render.CribEntityRenderer;
import fabric.net.mca.client.render.GrimReaperRenderer;
import fabric.net.mca.client.render.TombstoneBlockEntityRenderer;
import fabric.net.mca.client.render.VillagerEntityMCARenderer;
import fabric.net.mca.client.render.ZombieVillagerEntityMCARenderer;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.fabric.client.gui.FabricMCAScreens;
import fabric.net.mca.fabric.resources.ApiIdentifiableReloadListener;
import fabric.net.mca.fabric.resources.FabricColorPaletteLoader;
import fabric.net.mca.fabric.resources.FabricSupportersLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_2248;
import net.minecraft.class_2396;
import net.minecraft.class_2591;
import net.minecraft.class_310;
import net.minecraft.class_3264;
import net.minecraft.class_5272;
import net.minecraft.class_5616;
import net.minecraft.class_963;
import net.minecraft.class_971;

public final class MCAFabricClient extends ClientProxyAbstractImpl implements ClientModInitializer {
   public void onInitializeClient() {
      if (Config.getInstance().useSquidwardModels) {
         EntityRendererRegistry.register(EntitiesMCA.MALE_VILLAGER, class_963::new);
         EntityRendererRegistry.register(EntitiesMCA.FEMALE_VILLAGER, class_963::new);
         EntityRendererRegistry.register(EntitiesMCA.MALE_ZOMBIE_VILLAGER, class_971::new);
         EntityRendererRegistry.register(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER, class_971::new);
      } else {
         EntityRendererRegistry.register(EntitiesMCA.MALE_VILLAGER, VillagerEntityMCARenderer::new);
         EntityRendererRegistry.register(EntitiesMCA.FEMALE_VILLAGER, VillagerEntityMCARenderer::new);
         EntityRendererRegistry.register(EntitiesMCA.MALE_ZOMBIE_VILLAGER, ZombieVillagerEntityMCARenderer::new);
         EntityRendererRegistry.register(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER, ZombieVillagerEntityMCARenderer::new);
      }

      EntityRendererRegistry.register(EntitiesMCA.GRIM_REAPER, GrimReaperRenderer::new);
      EntityRendererRegistry.register(EntitiesMCA.CRIB, CribEntityRenderer::new);
      ParticleProviderRegistry.register((class_2396)ParticleTypesMCA.NEG_INTERACTION.get(), InteractionParticle.Factory::new);
      ParticleProviderRegistry.register((class_2396)ParticleTypesMCA.POS_INTERACTION.get(), InteractionParticle.Factory::new);
      class_5616.method_32144((class_2591)BlockEntityTypesMCA.TOMBSTONE.get(), TombstoneBlockEntityRenderer::new);
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new FabricMCAScreens());
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new FabricColorPaletteLoader());
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new FabricSupportersLoader());
      ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new ApiIdentifiableReloadListener());
      ModelPredicatesMCA.setup(class_5272::method_27879);
      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> MCAClient.onLogin());
      BlockRenderLayerMap.INSTANCE.putBlock((class_2248)BlocksMCA.INFERNAL_FLAME.get(), class_1921.method_23581());
      ClientTickEvents.START_CLIENT_TICK.register(MCAClient::tickClient);
      KeyBindings.list.forEach(KeyBindingHelper::registerKeyBinding);
   }

   @Override
   public class_1657 getClientPlayer() {
      return class_310.method_1551().field_1724;
   }
}
