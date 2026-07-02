package quilt.net.mca;

import quilt.net.mca.network.ClientInteractionManager;
import quilt.net.mca.network.ClientInteractionManagerImpl;

public abstract class ClientProxyAbstractImpl extends ClientProxy.Impl {
   private final ClientInteractionManager networkHandler = new ClientInteractionManagerImpl();

   @Override
   public final ClientInteractionManager getNetworkHandler() {
      return this.networkHandler;
   }
}
