package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;

public class InteractionDialogueQuestionResponse implements Message {
   private static final long serialVersionUID = 1371939319244994642L;
   public final String questionText;
   public final boolean silent;

   public InteractionDialogueQuestionResponse(boolean silent, Component questionText) {
      this.questionText = Serializer.m_130703_(questionText);
      this.silent = silent;
   }

   public MutableComponent getQuestionText() {
      return Serializer.m_130701_(this.questionText);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleDialogueQuestionResponse(this);
   }
}
