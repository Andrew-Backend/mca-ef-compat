package forge.net.mca.cobalt.network;

import net.minecraft.server.level.ServerPlayer;

public abstract class NetworkHandler {
   private static NetworkHandler.Impl INSTANCE;

   public static <T extends Message> void registerMessage(Class<T> msg) {
      INSTANCE.registerMessage(msg);
   }

   public static void sendToServer(Message m) {
      INSTANCE.sendToServer(m);
   }

   public static void sendToPlayer(Message m, ServerPlayer e) {
      INSTANCE.sendToPlayer(m, e);
   }

   public abstract static class Impl {
      protected Impl() {
         NetworkHandler.INSTANCE = this;
      }

      public abstract <T extends Message> void registerMessage(Class<T> var1);

      public abstract void sendToServer(Message var1);

      public abstract void sendToPlayer(Message var1, ServerPlayer var2);
   }
}
