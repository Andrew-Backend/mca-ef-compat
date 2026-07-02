package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.Config;
import forge.net.mca.cobalt.network.Message;
import net.minecraft.server.level.ServerPlayer;

public class OpenDestinyGuiRequest implements Message {
   private static final long serialVersionUID = -8912548616237596312L;
   public final int player;
   public final boolean allowTeleportation;

   public OpenDestinyGuiRequest(ServerPlayer player) {
      this.player = player.m_19879_();
      this.allowTeleportation = Config.getInstance().allowDestinyTeleportation;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleDestinyGuiRequest(this);
   }
}
