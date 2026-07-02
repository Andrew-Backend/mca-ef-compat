package yesman.epicfight.world.entity.eventlistener;

import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Stop;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ItemUseEndEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
   private final Stop forgeEvent;

   public ItemUseEndEvent(ServerPlayerPatch playerpatch, Stop forgeEvent) {
      super(playerpatch, true);
      this.forgeEvent = forgeEvent;
   }

   public Stop getForgeEvent() {
      return this.forgeEvent;
   }
}
