package quilt.net.mca.network.s2c;

import java.util.List;
import net.minecraft.class_3222;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.resources.data.dialogue.Question;

public class InteractionDialogueResponse implements Message {
   private static final long serialVersionUID = 1371939319244994642L;
   public final String question;
   public final List<String> answers;

   public InteractionDialogueResponse(Question question, class_3222 player, VillagerEntityMCA villager) {
      this.question = question.getName();
      this.answers = question.getValidAnswers(player, villager);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleDialogueResponse(this);
   }
}
