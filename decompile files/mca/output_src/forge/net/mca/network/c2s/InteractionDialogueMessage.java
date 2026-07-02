package forge.net.mca.network.c2s;

import forge.net.mca.cobalt.network.Message;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.resources.Dialogues;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class InteractionDialogueMessage implements Message {
   private static final long serialVersionUID = 1462101145658166706L;
   private final UUID villagerUUID;
   private final String question;
   private final String answer;

   public InteractionDialogueMessage(UUID uuid, String question, String answer) {
      this.villagerUUID = uuid;
      this.question = question;
      this.answer = answer;
   }

   @Override
   public void receive(ServerPlayer player) {
      if (player.m_284548_().m_8791_(this.villagerUUID) instanceof VillagerEntityMCA villager) {
         Dialogues.getInstance().selectAnswer(villager, player, this.question, this.answer);
      }
   }
}
