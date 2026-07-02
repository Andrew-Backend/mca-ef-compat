package fabric.net.mca;

import fabric.net.mca.network.ClientInteractionManager;
import net.minecraft.class_1657;
import org.jetbrains.annotations.Nullable;

public class ClientProxy {
   private static ClientProxy.Impl INSTANCE = new ClientProxy.Impl();

   @Nullable
   public static class_1657 getClientPlayer() {
      return INSTANCE.getClientPlayer();
   }

   public static ClientInteractionManager getNetworkHandler() {
      return INSTANCE.getNetworkHandler();
   }

   public static class Impl {
      protected Impl() {
         ClientProxy.INSTANCE = this;
      }

      public class_1657 getClientPlayer() {
         return null;
      }

      public ClientInteractionManager getNetworkHandler() {
         return null;
      }
   }
}
