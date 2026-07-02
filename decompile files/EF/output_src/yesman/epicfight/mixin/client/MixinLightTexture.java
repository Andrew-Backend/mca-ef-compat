package yesman.epicfight.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;

@Mixin(LightTexture.class)
public abstract class MixinLightTexture {
   @Inject(at = @At("HEAD"), method = "updateLightTexture(F)V", cancellable = true)
   private void epicfight_head_updateLightTexture(CallbackInfo info) {
      OverlayManager overlayManager = ClientEngine.getInstance().renderEngine.getOverlayManager();
      if (overlayManager.isGammaChanged()) {
         Minecraft minecraft = Minecraft.m_91087_();
         minecraft.f_91066_.m_231927_().m_231514_(overlayManager.getModifiedGamma((Double)minecraft.f_91066_.m_231927_().m_231551_()));
      }
   }

   @Inject(at = @At("TAIL"), method = "updateLightTexture(F)V", cancellable = true)
   private void epicfight_tail_updateLightTexture(CallbackInfo info) {
      OverlayManager overlayManager = ClientEngine.getInstance().renderEngine.getOverlayManager();
      if (overlayManager.isGammaChanged()) {
         Minecraft minecraft = Minecraft.m_91087_();
         minecraft.f_91066_.m_231927_().m_231514_(overlayManager.getOriginalGamma());
      }
   }
}
