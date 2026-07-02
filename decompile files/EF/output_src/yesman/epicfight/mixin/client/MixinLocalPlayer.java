package yesman.epicfight.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPUpdatePlayerInput;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {
   public MixinLocalPlayer(ClientLevel arg1, GameProfile arg2) {
      super(arg1, arg2);
   }

   @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V", shift = Shift.BEFORE), method = "tick()V")
   private void epicfight$tick(CallbackInfo callbackInfo) {
      LocalPlayer epicfight$entity = (LocalPlayer)this;
      LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(epicfight$entity, LocalPlayerPatch.class);
      if (localPlayerPatch != null) {
         localPlayerPatch.dx = epicfight$entity.f_20900_;
         localPlayerPatch.dz = epicfight$entity.f_20902_;
      }

      EpicFightNetworkManager.sendToServer(new CPUpdatePlayerInput(epicfight$entity.m_19879_(), epicfight$entity.f_20900_, epicfight$entity.f_20902_));
   }

   @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
   private void onDrop(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
      if (ClientEngine.getInstance().controlEngine.isSwitchOrDropBlocked()) {
         cir.cancel();
      }
   }

   public void m_19920_(float amount, Vec3 relative) {
      Vec3 vec3 = EpicFightCameraAPI.getInstance().getRelativeMove(relative, amount);
      this.m_20256_(this.m_20184_().m_82549_(vec3));
   }
}
