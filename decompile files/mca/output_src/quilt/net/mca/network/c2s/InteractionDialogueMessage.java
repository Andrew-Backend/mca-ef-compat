package quilt.net.mca.network.c2s;

import java.util.UUID;
import net.minecraft.class_3222;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.resources.Dialogues;

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
