package fabric.net.mca.fabric;

import fabric.net.mca.MCA;
import fabric.net.mca.ParticleTypesMCA;
import fabric.net.mca.SoundsMCA;
import fabric.net.mca.TradeOffersMCA;
import fabric.net.mca.advancement.criterion.CriterionMCA;
import fabric.net.mca.block.BlocksMCA;
import fabric.net.mca.entity.EntitiesMCA;
import fabric.net.mca.fabric.cobalt.network.NetworkHandlerImpl;
import fabric.net.mca.fabric.resources.ApiIdentifiableReloadListener;
import fabric.net.mca.fabric.resources.FabricBuildingTypes;
import fabric.net.mca.fabric.resources.FabricClothingList;
import fabric.net.mca.fabric.resources.FabricDialogues;
import fabric.net.mca.fabric.resources.FabricGiftLoader;
import fabric.net.mca.fabric.resources.FabricHairList;
import fabric.net.mca.fabric.resources.FabricNames;
import fabric.net.mca.fabric.resources.FabricTasks;
import fabric.net.mca.item.ItemsMCA;
import fabric.net.mca.network.MessagesMCA;
import fabric.net.mca.server.ServerInteractionManager;
import fabric.net.mca.server.command.AdminCommand;
import fabric.net.mca.server.command.Command;
import fabric.net.mca.server.world.data.VillageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndWorldTick;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.class_3264;

public final class MCAFabric implements ModInitializer {
   public void onInitialize() {
      new NetworkHandlerImpl();
      BlocksMCA.bootstrap();
      ItemsMCA.bootstrap();
      SoundsMCA.bootstrap();
      ParticleTypesMCA.bootstrap();
      EntitiesMCA.bootstrap();
      MessagesMCA.bootstrap();
      CriterionMCA.bootstrap();
      TradeOffersMCA.bootstrap();
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new ApiIdentifiableReloadListener());
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new FabricClothingList());
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new FabricHairList());
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new FabricGiftLoader());
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new FabricDialogues());
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new FabricTasks());
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new FabricNames());
      ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new FabricBuildingTypes());
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)w -> VillageManager.get(w).tick());
      ServerTickEvents.END_SERVER_TICK.register((EndTick)s -> ServerInteractionManager.getInstance().tick());
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> ServerInteractionManager.getInstance().onPlayerJoin(handler.field_14140));
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> {
         AdminCommand.register(dispatcher);
         Command.register(dispatcher);
      });
      ServerTickEvents.END_SERVER_TICK.register(MCA::setServer);
   }
}
