package quilt.net.mca.quilt;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents.Join;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import quilt.net.mca.ClientProxyAbstractImpl;
import quilt.net.mca.Config;
import quilt.net.mca.KeyBindings;
import quilt.net.mca.MCAClient;
import quilt.net.mca.ModelPredicatesMCA;
import quilt.net.mca.ParticleTypesMCA;
import quilt.net.mca.block.BlockEntityTypesMCA;
import quilt.net.mca.block.BlocksMCA;
import quilt.net.mca.client.particle.InteractionParticle;
import quilt.net.mca.client.render.CribEntityRenderer;
import quilt.net.mca.client.render.GrimReaperRenderer;
import quilt.net.mca.client.render.TombstoneBlockEntityRenderer;
import quilt.net.mca.client.render.VillagerEntityMCARenderer;
import quilt.net.mca.client.render.ZombieVillagerEntityMCARenderer;
import quilt.net.mca.entity.EntitiesMCA;
import quilt.net.mca.quilt.client.gui.QuiltMCAScreens;
import quilt.net.mca.quilt.resources.ApiIdentifiableReloadListener;
import quilt.net.mca.quilt.resources.QuiltColorPaletteLoader;
import quilt.net.mca.quilt.resources.QuiltSupportersLoader;

public final class MCAQuiltClient extends ClientProxyAbstractImpl implements ClientModInitializer {
   public void onInitializeClient(ModContainer container) {
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
      ResourceLoader.get(class_3264.field_14188).registerReloader(new QuiltMCAScreens());
      ResourceLoader.get(class_3264.field_14188).registerReloader(new QuiltColorPaletteLoader());
      ResourceLoader.get(class_3264.field_14188).registerReloader(new QuiltSupportersLoader());
      ResourceLoader.get(class_3264.field_14188).registerReloader(new ApiIdentifiableReloadListener());
      ModelPredicatesMCA.setup(class_5272::method_27879);
      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> MCAClient.onLogin());
      BlockRenderLayerMap.put(class_1921.method_23581(), new class_2248[]{(class_2248)BlocksMCA.INFERNAL_FLAME.get()});
      ClientTickEvents.START.register(MCAClient::tickClient);
      KeyBindings.list.forEach(KeyBindingHelper::registerKeyBinding);
   }

   @Override
   public class_1657 getClientPlayer() {
      return class_310.method_1551().field_1724;
   }
}
