package quilt.net.mca.entity.ai.chatAI.inworldAIModules;

import net.minecraft.class_3222;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.chatAI.TriggerCommandInfos;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.api.Interaction;
import quilt.net.mca.entity.ai.chatAI.inworldAIModules.api.TriggerEvent;

public class TriggerModule {
   public void processTriggers(Interaction interaction, class_3222 player, VillagerEntityMCA villager) {
      TriggerEvent[] triggerEvents = interaction.outgoingTriggers();

      for (TriggerEvent event : triggerEvents) {
         TriggerCommandInfos.findCommand(event.trigger(), player, villager).ifPresent(commandInfo -> commandInfo.call.accept(player, villager));
      }
   }
}
