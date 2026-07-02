package quilt.net.mca.network.s2c;

import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_2561.class_2562;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;

public class InteractionDialogueQuestionResponse implements Message {
   private static final long serialVersionUID = 1371939319244994642L;
   public final String questionText;
   public final boolean silent;

   public InteractionDialogueQuestionResponse(boolean silent, class_2561 questionText) {
      this.questionText = class_2562.method_10867(questionText);
      this.silent = silent;
   }

   public class_5250 getQuestionText() {
      return class_2562.method_10877(this.questionText);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleDialogueQuestionResponse(this);
   }
}
