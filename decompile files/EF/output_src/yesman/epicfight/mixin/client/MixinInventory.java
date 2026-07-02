package yesman.epicfight.mixin.client;

import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.events.engine.ControlEngine;

@Mixin(Inventory.class)
public class MixinInventory {
   @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
   private void onCycleHotbarSlot(double direction, CallbackInfo ci) {
      if (ControlEngine.isHotbarCyclingDisabled()) {
         ci.cancel();
      }
   }
}
