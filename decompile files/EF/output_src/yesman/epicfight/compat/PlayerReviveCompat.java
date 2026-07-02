package yesman.epicfight.compat;

import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import team.creative.playerrevive.server.PlayerReviveServer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class PlayerReviveCompat implements ICompatModule {
   @Override
   public void onModEventBus(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBus(IEventBus eventBus) {
      eventBus.addListener(
         event -> {
            if (event.getEntity() instanceof Player player) {
               EpicFightCapabilities.getPlayerPatchAsOptional(event.getEntity())
                  .ifPresent(
                     playerPatch -> playerPatch.getEventListener()
                        .addEventListener(PlayerEventListener.EventType.SKILL_CAST_EVENT, UUID.randomUUID(), skillCastEvent -> {
                           if (PlayerReviveServer.isBleeding(player)) {
                              skillCastEvent.setCanceled(true);
                           }
                        })
                  );
            }
         }
      );
   }

   @Override
   public void onModEventBusClient(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBusClient(IEventBus eventBus) {
   }
}
