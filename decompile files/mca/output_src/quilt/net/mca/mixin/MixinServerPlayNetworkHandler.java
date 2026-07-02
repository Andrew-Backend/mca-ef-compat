package quilt.net.mca.mixin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_2561;
import net.minecraft.class_2797;
import net.minecraft.class_3222;
import net.minecraft.class_3244;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.chatAI.ChatAI;

@Mixin(class_3244.class)
public class MixinServerPlayNetworkHandler {
   @Shadow
   public class_3222 field_14140;

   @Inject(method = "method_12048", at = @At("HEAD"))
   public void sendMessage(class_2797 message, CallbackInfo ci) {
      if (Config.getInstance().enableVillagerChatAI) {
         String msg = StringUtils.normalizeSpace(message.comp_945());
         if (!msg.startsWith("/")) {
            Optional<VillagerEntityMCA> villager = ChatAI.getVillagerForConversation(this.field_14140, msg);
            villager.ifPresent(villagerEntityMCA -> this.mca$runAsyncAnswerRequest(this.field_14140, villagerEntityMCA, msg));
         }
      }
   }

   @Unique
   private void mca$runAsyncAnswerRequest(class_3222 player, VillagerEntityMCA villager, String msg) {
      CompletableFuture.runAsync(() -> {
         Optional<String> answer = ChatAI.answer(player, villager, msg);
         answer.ifPresent(a -> villager.conversationManager.addMessage(player, class_2561.method_43470(a)));
      });
   }
}
