package yesman.epicfight.compat.betterthirdperson;

import net.minecraftforge.eventbus.api.IEventBus;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisableState;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisabledReason;
import yesman.epicfight.compat.ICompatModule;

public final class BetterThirdPersonCompat implements ICompatModule {
   @Override
   public void onModEventBus(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBus(IEventBus eventBus) {
   }

   @Override
   public void onModEventBusClient(IEventBus eventBus) {
      EpicFightTpsCameraDisableState.disable(EpicFightTpsCameraDisabledReason.BetterThirdPerson);
   }

   @Override
   public void onForgeEventBusClient(IEventBus eventBus) {
   }
}
