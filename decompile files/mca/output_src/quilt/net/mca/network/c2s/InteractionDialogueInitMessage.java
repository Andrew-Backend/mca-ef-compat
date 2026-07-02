package quilt.net.mca.network.c2s;

import java.util.UUID;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.network.s2c.InteractionDialogueResponse;
import quilt.net.mca.resources.Dialogues;
import quilt.net.mca.resources.data.dialogue.Question;

public class InteractionDialogueInitMessage implements Message {
   private static final long serialVersionUID = -8007274573058750406L;
   private final UUID villagerUUID;

   public InteractionDialogueInitMessage(UUID uuid) {
      this.villagerUUID = uuid;
   }

   @Override
   public void receive(class_3222 player) {
      if (player.method_51469().method_14190(this.villagerUUID) instanceof VillagerEntityMCA villager) {
         Question question = Dialogues.getInstance().getQuestion("root");
         if (question.isAuto()) {
            Dialogues.getInstance().selectAnswer(villager, player, question.getName(), question.getRandomAnswer().getName());
         } else {
            NetworkHandler.sendToPlayer(new InteractionDialogueResponse(question, player, villager), player);
         }
      }
   }
}
