package quilt.net.mca.network.s2c;

import net.minecraft.class_2561;
import quilt.net.mca.ClientProxy;
import quilt.net.mca.cobalt.network.Message;

public class ShowToastRequest implements Message {
   private static final long serialVersionUID = 1055734972572313374L;
   private final String title;
   private final String message;

   public ShowToastRequest(String title, String message) {
      this.title = title;
      this.message = message;
   }

   public class_2561 getTitle() {
      return class_2561.method_43471(this.title);
   }

   public class_2561 getMessage() {
      return class_2561.method_43471(this.message);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleToastMessage(this);
   }
}
