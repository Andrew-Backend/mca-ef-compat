package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;
import net.minecraft.network.chat.Component;

public class ShowToastRequest implements Message {
   private static final long serialVersionUID = 1055734972572313374L;
   private final String title;
   private final String message;

   public ShowToastRequest(String title, String message) {
      this.title = title;
      this.message = message;
   }

   public Component getTitle() {
      return Component.m_237115_(this.title);
   }

   public Component getMessage() {
      return Component.m_237115_(this.message);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleToastMessage(this);
   }
}
