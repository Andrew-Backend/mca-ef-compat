package forge.net.mca.mixin;

import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.chatAI.ChatAI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerPlayNetworkHandler {
   @Shadow
   public ServerPlayer f_9743_;

   @Inject(method = "m_7388_", at = @At("HEAD"))
   public void sendMessage(ServerboundChatPacket message, CallbackInfo ci) {
      if (Config.getInstance().enableVillagerChatAI) {
         String msg = StringUtils.normalizeSpace(message.f_133827_());
         if (!msg.startsWith("/")) {
            Optional<VillagerEntityMCA> villager = ChatAI.getVillagerForConversation(this.f_9743_, msg);
            villager.ifPresent(villagerEntityMCA -> this.mca$runAsyncAnswerRequest(this.f_9743_, villagerEntityMCA, msg));
         }
      }
   }

   @Unique
   private void mca$runAsyncAnswerRequest(ServerPlayer player, VillagerEntityMCA villager, String msg) {
      CompletableFuture.runAsync(() -> {
         Optional<String> answer = ChatAI.answer(player, villager, msg);
         answer.ifPresent(a -> villager.conversationManager.addMessage(player, Component.m_237113_(a)));
      });
   }
}
