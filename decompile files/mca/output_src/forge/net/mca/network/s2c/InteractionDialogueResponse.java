package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.resources.data.dialogue.Question;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public class InteractionDialogueResponse implements Message {
   private static final long serialVersionUID = 1371939319244994642L;
   public final String question;
   public final List<String> answers;

   public InteractionDialogueResponse(Question question, ServerPlayer player, VillagerEntityMCA villager) {
      this.question = question.getName();
      this.answers = question.getValidAnswers(player, villager);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleDialogueResponse(this);
   }
}
