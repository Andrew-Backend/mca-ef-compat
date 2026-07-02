package quilt.net.mca.entity.ai.chatAI;

import java.util.Optional;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.EmotionModule;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.RelationshipModule;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.SessionModule;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.TriggerModule;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.api.Interaction;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.api.TriggerEvent;

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
   public Optional<String> answer(class_3222 player, VillagerEntityMCA villager, String msg) {
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
         player.method_7353(class_2561.method_43471("mca.ai_broken").method_27692(class_124.field_1061), false);
         return Optional.empty();
      }
   }
}
