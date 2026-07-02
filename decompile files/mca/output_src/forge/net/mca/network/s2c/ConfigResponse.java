package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.CommonConfig;
import forge.net.mca.cobalt.network.Message;

public class ConfigResponse implements Message {
   private static final long serialVersionUID = -559319583580183137L;
   private final CommonConfig config;

   public ConfigResponse(CommonConfig config) {
      this.config = new CommonConfig(config);
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleConfigResponse(this);
   }

   public CommonConfig getConfig() {
      return this.config;
   }
}
