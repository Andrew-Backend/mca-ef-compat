package forge.net.mca.entity.ai.chatAI;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.EmotionModule;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.RelationshipModule;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.SessionModule;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.TriggerModule;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.api.Interaction;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.api.TriggerEvent;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class InworldAI implements ChatAIStrategy {
   private final SessionModule sessionModule;
   private final RelationshipModule relationshipModule;
   private final TriggerModule triggerModule;
   private final EmotionModule emotionModule;

   public InworldAI(String resourceName) {
      this.sessionModule = new SessionModule(resourceName);
      this.relationshipModule = new RelationshipModule();
      this.triggerModule = new TriggerModule();
      this.emotionModule = new EmotionModule();
   }

   @Override
   public Optional<String> answer(ServerPlayer player, VillagerEntityMCA villager, String msg) {
      TriggerEvent.Parameter relationshipTrigger = this.relationshipModule.getRelationshipTriggerParameter(player, villager);
      TriggerEvent.Parameter emotionTrigger = this.emotionModule.getEmotionTriggerParameter(villager);
      TriggerEvent event = new TriggerEvent("status-update", new TriggerEvent.Parameter[]{relationshipTrigger, emotionTrigger});
      Optional<Interaction> optionalResponse = this.sessionModule.getResponse(player, msg, event);
      if (optionalResponse.isPresent()) {
         Interaction response = optionalResponse.get();
         this.relationshipModule.updateRelationship(response, player, villager);
         this.triggerModule.processTriggers(response, player, villager);
         String answer = String.join("", response.textList());
         return answer.isEmpty() ? Optional.empty() : Optional.of(answer);
      } else {
         player.m_5661_(Component.m_237115_("mca.ai_broken").m_130940_(ChatFormatting.RED), false);
         return Optional.empty();
      }
   }
}
