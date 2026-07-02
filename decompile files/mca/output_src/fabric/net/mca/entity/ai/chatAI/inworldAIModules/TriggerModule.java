package fabric.net.mca.entity.ai.chatAI.inworldAIModules;

import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.chatAI.TriggerCommandInfos;
import fabric.net.mca.entity.ai.chatAI.inworldAIModules.api.Interaction;
import fabric.net.mca.entity.ai.chatAI.inworldAIModules.api.TriggerEvent;
import net.minecraft.class_3222;

public class TriggerModule {
   public void processTriggers(Interaction interaction, class_3222 player, VillagerEntityMCA villager) {
      TriggerEvent[] triggerEvents = interaction.outgoingTriggers();

      for (TriggerEvent event : triggerEvents) {
         TriggerCommandInfos.findCommand(event.trigger(), player, villager).ifPresent(commandInfo -> commandInfo.call.accept(player, villager));
      }
   }
}
