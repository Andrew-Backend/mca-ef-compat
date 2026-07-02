package quilt.net.mca.quilt;

import net.minecraft.class_3264;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents.End;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents.Join;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import quilt.net.mca.MCA;
import quilt.net.mca.ParticleTypesMCA;
import quilt.net.mca.SoundsMCA;
import quilt.net.mca.TradeOffersMCA;
import quilt.net.mca.advancement.criterion.CriterionMCA;
import quilt.net.mca.block.BlocksMCA;
import quilt.net.mca.entity.EntitiesMCA;
import quilt.net.mca.item.ItemsMCA;
import quilt.net.mca.network.MessagesMCA;
import quilt.net.mca.quilt.cobalt.network.NetworkHandlerImpl;
import quilt.net.mca.quilt.resources.ApiIdentifiableReloadListener;
import quilt.net.mca.quilt.resources.QuiltBuildingTypes;
import quilt.net.mca.quilt.resources.QuiltClothingList;
import quilt.net.mca.quilt.resources.QuiltDialogues;
import quilt.net.mca.quilt.resources.QuiltGiftLoader;
import quilt.net.mca.quilt.resources.QuiltHairList;
import quilt.net.mca.quilt.resources.QuiltNames;
import quilt.net.mca.quilt.resources.QuiltTasks;
import quilt.net.mca.server.ServerInteractionManager;
import quilt.net.mca.server.command.AdminCommand;
import quilt.net.mca.server.command.Command;
import quilt.net.mca.server.world.data.VillageManager;

public final class MCAQuilt implements ModInitializer {
   public void onInitialize(ModContainer container) {
      new NetworkHandlerImpl();
      BlocksMCA.bootstrap();
      ItemsMCA.bootstrap();
      SoundsMCA.bootstrap();
      ParticleTypesMCA.bootstrap();
      EntitiesMCA.bootstrap();
      MessagesMCA.bootstrap();
      CriterionMCA.bootstrap();
      TradeOffersMCA.bootstrap();
      ResourceLoader.get(class_3264.field_14190).registerReloader(new ApiIdentifiableReloadListener());
      ResourceLoader.get(class_3264.field_14190).registerReloader(new QuiltClothingList());
      ResourceLoader.get(class_3264.field_14190).registerReloader(new QuiltHairList());
      ResourceLoader.get(class_3264.field_14190).registerReloader(new QuiltGiftLoader());
      ResourceLoader.get(class_3264.field_14190).registerReloader(new QuiltDialogues());
      ResourceLoader.get(class_3264.field_14190).registerReloader(new QuiltTasks());
      ResourceLoader.get(class_3264.field_14190).registerReloader(new QuiltNames());
      ResourceLoader.get(class_3264.field_14190).registerReloader(new QuiltBuildingTypes());
      ServerWorldTickEvents.END.register((End)(s, w) -> VillageManager.get(w).tick());
      ServerTickEvents.END.register((org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents.End)s -> ServerInteractionManager.getInstance().tick());
      ServerTickEvents.END.register(MCA::setServer);
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> ServerInteractionManager.getInstance().onPlayerJoin(handler.field_14140));
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, integrated, dedicated) -> {
         AdminCommand.register(dispatcher);
         Command.register(dispatcher);
      });
   }
}
