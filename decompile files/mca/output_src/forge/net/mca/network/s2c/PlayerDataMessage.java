package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.network.NbtDataMessage;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public class PlayerDataMessage extends NbtDataMessage {
   private static final long serialVersionUID = 145267688456022788L;
   public final UUID uuid;

   public PlayerDataMessage(UUID uuid, CompoundTag nbt) {
      super(nbt);
      this.uuid = uuid;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handlePlayerDataMessage(this);
   }
}
