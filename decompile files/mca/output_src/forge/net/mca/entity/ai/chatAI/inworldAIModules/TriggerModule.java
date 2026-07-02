package forge.net.mca.entity.ai.chatAI.inworldAIModules;

import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.chatAI.TriggerCommandInfos;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.api.Interaction;
import forge.net.mca.entity.ai.chatAI.inworldAIModules.api.TriggerEvent;
import net.minecraft.server.level.ServerPlayer;

public class TriggerModule {
   public void processTriggers(Interaction interaction, ServerPlayer player, VillagerEntityMCA villager) {
      TriggerEvent[] triggerEvents = interaction.outgoingTriggers();

      for (TriggerEvent event : triggerEvents) {
         TriggerCommandInfos.findCommand(event.trigger(), player, villager).ifPresent(commandInfo -> commandInfo.call.accept(player, villager));
      }
   }
}
