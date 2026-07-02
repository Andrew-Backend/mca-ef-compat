package forge.net.mca;

import forge.net.mca.network.ClientInteractionManager;
import forge.net.mca.network.ClientInteractionManagerImpl;

public abstract class ClientProxyAbstractImpl extends ClientProxy.Impl {
   private final ClientInteractionManager networkHandler = new ClientInteractionManagerImpl();

   @Override
   public final ClientInteractionManager getNetworkHandler() {
      return this.networkHandler;
   }
}
