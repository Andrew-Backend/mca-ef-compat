package yesman.epicfight.compat;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraftforge.eventbus.api.IEventBus;

public class IceAndFireCompat implements ICompatModule {
   @Override
   public void onModEventBus(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBus(IEventBus eventBus) {
      eventBus.addListener(event -> {
         if (event.getPlayerPatch().getOriginal().m_20202_() instanceof EntityDragonBase) {
            event.setCanceled(true);
         }
      });
   }

   @Override
   public void onModEventBusClient(IEventBus eventBus) {
   }

   @Override
   public void onForgeEventBusClient(IEventBus eventBus) {
   }
}
