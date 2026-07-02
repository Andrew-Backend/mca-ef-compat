package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.resources.Dialogues;
import java.util.UUID;
import net.minecraft.class_3222;

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
   public void receive(class_3222 player) {
      if (player.method_51469().method_14190(this.villagerUUID) instanceof VillagerEntityMCA villager) {
         Dialogues.getInstance().selectAnswer(villager, player, this.question, this.answer);
      }
   }
}
