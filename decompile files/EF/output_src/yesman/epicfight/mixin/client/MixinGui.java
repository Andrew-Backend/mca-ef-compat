package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.HitResult.Type;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(Gui.class)
public abstract class MixinGui {
   @Shadow
   private static ResourceLocation f_279580_;
   @Shadow
   private Minecraft f_92986_;

   @Inject(at = @At("TAIL"), method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V")
   private void renderCrosshairINJECT(GuiGraphics guiGraphics, CallbackInfo callback) {
      if (EpicFightCameraAPI.getInstance().isTPSMode()) {
         this.epicfight$renderCrosshair(guiGraphics, true);
      }
   }

   @Redirect(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", ordinal = 0),
      method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V"
   )
   private void renderCrosshairREDIRECT(
      GuiGraphics guiGraphics, ResourceLocation pAtlasLocation, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight
   ) {
      this.epicfight$renderCrosshair(guiGraphics, false);
   }

   @Unique
   private void epicfight$renderCrosshair(GuiGraphics guiGraphics, boolean setupBlend) {
      if (setupBlend) {
         RenderSystem.blendFuncSeparate(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO);
      }

      MutableBoolean drawVanillaCrosshair = new MutableBoolean(true);
      if (ClientConfig.mineBlockGuideOption.switchCrosshair()) {
         EpicFightCapabilities.getUnparameterizedEntityPatch(this.f_92986_.f_91074_, LocalPlayerPatch.class).ifPresent(playerpatch -> {
            if (playerpatch.isVanillaMode()) {
               drawVanillaCrosshair.setValue(RenderEngine.hitResultNotEquals(this.f_92986_.f_91077_, Type.BLOCK));
            } else {
               drawVanillaCrosshair.setValue(playerpatch.canPlayAttackAnimation());
            }
         });
      }

      if (drawVanillaCrosshair.booleanValue()) {
         guiGraphics.m_280218_(f_279580_, (guiGraphics.m_280182_() - 15) / 2, (guiGraphics.m_280206_() - 15) / 2, 0, 0, 15, 15);
      } else {
         guiGraphics.m_280218_(EntityUI.BATTLE_ICON, (guiGraphics.m_280182_() - 15) / 2, (guiGraphics.m_280206_() - 15) / 2, 0, 240, 15, 15);
      }

      if (setupBlend) {
         RenderSystem.defaultBlendFunc();
      }
   }
}
