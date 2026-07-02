package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.network.NbtDataMessage;
import java.util.UUID;
import net.minecraft.class_2487;

public class PlayerDataMessage extends NbtDataMessage {
   private static final long serialVersionUID = 145267688456022788L;
   public final UUID uuid;

   public PlayerDataMessage(UUID uuid, class_2487 nbt) {
      super(nbt);
      this.uuid = uuid;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handlePlayerDataMessage(this);
   }
}
