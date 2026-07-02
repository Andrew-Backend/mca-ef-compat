package yesman.epicfight.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.client.world.util.FakeLevel;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {
   @Redirect(
      method = "<init>",
      at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z")
   )
   private boolean epicfight$init(IEventBus instance, Event e) {
      return (ClientLevel)this instanceof FakeLevel ? false : instance.post(e);
   }
}
