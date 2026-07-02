package yesman.epicfight.compat;

import dev.tr7zw.firstperson.api.ActivationHandler;
import dev.tr7zw.firstperson.api.FirstPersonAPI;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class FirstPersonCompat implements ICompatModule {
   @OnlyIn(Dist.CLIENT)
   @Override
   public void onModEventBusClient(IEventBus eventBus) {
      eventBus.addListener(event -> event.enqueueWork(() -> FirstPersonAPI.getActivationHandlers().add(new ActivationHandler() {
         public boolean preventFirstperson() {
            PlayerPatch<?> playerpatch = ClientEngine.getInstance().getPlayerPatch();
            return playerpatch != null && playerpatch.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT && ClientConfig.enableAnimatedFirstPersonModel;
         }
      })));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onForgeEventBusClient(IEventBus eventBus) {
   }

   @Override
   public void onModEventBus(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBus(IEventBus eventBus) {
   }
}
