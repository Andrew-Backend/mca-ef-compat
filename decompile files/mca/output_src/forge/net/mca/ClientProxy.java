package forge.net.mca;

import forge.net.mca.network.ClientInteractionManager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ClientProxy {
   private static ClientProxy.Impl INSTANCE = new ClientProxy.Impl();

   @Nullable
   public static Player getClientPlayer() {
      return INSTANCE.getClientPlayer();
   }

   public static ClientInteractionManager getNetworkHandler() {
      return INSTANCE.getNetworkHandler();
   }

   public static class Impl {
      protected Impl() {
         ClientProxy.INSTANCE = this;
      }

      public Player getClientPlayer() {
         return null;
      }

      public ClientInteractionManager getNetworkHandler() {
         return null;
      }
   }
}
