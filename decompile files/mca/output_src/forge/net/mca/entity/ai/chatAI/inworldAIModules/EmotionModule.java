package forge.net.mca.entity.ai.chatAI.inworldAIModules;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.api.Interaction;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.api.TriggerEvent;
import java.util.Map;

public class EmotionModule {
   private static final Map<String, Interaction.Emotion.SpaffCode> moodMap = Map.of(
      "depressed",
      Interaction.Emotion.SpaffCode.SADNESS,
      "sad",
      Interaction.Emotion.SpaffCode.SADNESS,
      "unhappy",
      Interaction.Emotion.SpaffCode.SADNESS,
      "passive",
      Interaction.Emotion.SpaffCode.NEUTRAL,
      "fine",
      Interaction.Emotion.SpaffCode.NEUTRAL,
      "happy",
      Interaction.Emotion.SpaffCode.JOY,
      "overjoyed",
      Interaction.Emotion.SpaffCode.JOY
   );

   public TriggerEvent.Parameter getEmotionTriggerParameter(VillagerEntityMCA villager) {
      String villagerMood = villager.getVillagerBrain().getMood().getName();
      return new TriggerEvent.Parameter("emotion", moodMap.getOrDefault(villagerMood, Interaction.Emotion.SpaffCode.NEUTRAL).name());
   }
}
