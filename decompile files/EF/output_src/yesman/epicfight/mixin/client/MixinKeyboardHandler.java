package yesman.epicfight.mixin.client;

import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.ClientEngine;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {
   @Shadow
   private long f_90870_ = -1L;

   @Inject(at = @At("HEAD"), method = "handleDebugKeys(I)Z", cancellable = true)
   private void epicfight_handleDebugKeys(int key, CallbackInfoReturnable<Boolean> info) {
      if (this.f_90870_ <= 0L || this.f_90870_ >= Util.m_137550_() - 100L) {
         switch (key) {
            case 89:
               boolean flag = ClientEngine.getInstance().switchVanillaModelDebuggingMode();
               this.m_90913_(flag ? "debug.vanilla_model_debugging.on" : "debug.vanilla_model_debugging.off");
               info.cancel();
               info.setReturnValue(true);
         }
      }
   }

   @Shadow
   private void m_90913_(String p_90914_, Object... p_90915_) {
   }
}
