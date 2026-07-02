package fabric.net.mca;

import fabric.net.mca.network.ClientInteractionManager;
import fabric.net.mca.network.ClientInteractionManagerImpl;

public abstract class ClientProxyAbstractImpl extends ClientProxy.Impl {
   private final ClientInteractionManager networkHandler = new ClientInteractionManagerImpl();

   @Override
   public final ClientInteractionManager getNetworkHandler() {
      return this.networkHandler;
   }
}
