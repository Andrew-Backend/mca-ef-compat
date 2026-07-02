package yesman.epicfight.client.gui.screen.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import yesman.epicfight.client.ClientEngine;

public class FlickeringOverlay extends OverlayManager.Overlay {
   private float time = (float) -Math.PI;
   private final float deltaTime;
   private final float strength;
   private final double initialGamma;

   public FlickeringOverlay(float deltaTime, float strength) {
      this.deltaTime = deltaTime;
      this.strength = strength;
      Minecraft minecraft = Minecraft.m_91087_();
      this.initialGamma = (Double)minecraft.f_91066_.m_231927_().m_231551_();
   }

   @Override
   public boolean render(int xResolution, int yResolution) {
      this.time = this.time + this.deltaTime;
      float darkenAmount = Mth.m_14036_((float)Math.sin(this.time), -1.0F, 0.0F);
      OverlayManager overlayManager = ClientEngine.getInstance().renderEngine.getOverlayManager();
      float gamma = (float)Math.max(this.initialGamma + darkenAmount * this.strength, 0.0);
      overlayManager.setModifiedGamma(gamma);
      return this.time >= 0.0F;
   }
}
