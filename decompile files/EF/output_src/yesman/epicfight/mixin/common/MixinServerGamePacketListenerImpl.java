package yesman.epicfight.mixin.common;

import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {
   @Shadow
   public ServerPlayer f_9743_;

   @Inject(at = @At("TAIL"), method = "handleResourcePackResponse(Lnet/minecraft/network/protocol/game/ServerboundResourcePackPacket;)V")
   public void epicfight_handleResourcePackResponse(ServerboundResourcePackPacket packet, CallbackInfo info) {
      if (packet.m_179741_() == Action.SUCCESSFULLY_LOADED) {
         ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(this.f_9743_, ServerPlayerPatch.class);
         if (playerpatch != null) {
            playerpatch.modifyLivingMotionByCurrentItem(false);
         }
      }
   }
}
