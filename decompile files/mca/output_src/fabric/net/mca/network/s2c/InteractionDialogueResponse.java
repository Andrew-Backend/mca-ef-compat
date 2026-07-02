package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.resources.data.dialogue.Question;
import java.util.List;
import net.minecraft.class_3222;

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
