package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.cobalt.network.NetworkHandler;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.network.s2c.InteractionDialogueResponse;
import fabric.net.mca.resources.Dialogues;
import fabric.net.mca.resources.data.dialogue.Question;
import java.util.UUID;
import net.minecraft.class_3222;

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
