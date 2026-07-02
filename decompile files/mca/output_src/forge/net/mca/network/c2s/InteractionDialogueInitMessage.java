package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.network.s2c.InteractionDialogueResponse;
import forge.net.mca.resources.Dialogues;
import forge.net.mca.resources.data.dialogue.Question;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class InteractionDialogueInitMessage implements Message {
   private static final long serialVersionUID = -8007274573058750406L;
   private final UUID villagerUUID;

   public InteractionDialogueInitMessage(UUID uuid) {
      this.villagerUUID = uuid;
   }

   @Override
   public void receive(ServerPlayer player) {
      if (player.m_284548_().m_8791_(this.villagerUUID) instanceof VillagerEntityMCA villager) {
         Question question = Dialogues.getInstance().getQuestion("root");
         if (question.isAuto()) {
            Dialogues.getInstance().selectAnswer(villager, player, question.getName(), question.getRandomAnswer().getName());
         } else {
            NetworkHandler.sendToPlayer(new InteractionDialogueResponse(question, player, villager), player);
         }
      }
   }
}
