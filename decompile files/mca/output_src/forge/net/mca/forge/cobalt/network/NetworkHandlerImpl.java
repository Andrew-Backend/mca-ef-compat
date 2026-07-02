package forge.net.mca.forge.cobalt.network;

import forge.net.mca.MCA;
import forge.net.mca.cobalt.network.Message;
import forge.net.mca.cobalt.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
   private final String PROTOCOL_VERSION = "1";
   private final SimpleChannel channel = NetworkRegistry.newSimpleChannel(MCA.locate("main"), () -> "1", "1"::equals, "1"::equals);
   private int id = 0;

   @Override
   public <T extends Message> void registerMessage(Class<T> msg) {
      this.channel.registerMessage(this.id++, msg, Message::encode, b -> Message.decode(b), (m, ctx) -> {
         ((Context)ctx.get()).enqueueWork(() -> {
            ServerPlayer sender = ((Context)ctx.get()).getSender();
            if (sender == null) {
               m.receive();
            } else {
               m.receive(sender);
            }
         });
         ((Context)ctx.get()).setPacketHandled(true);
      });
   }

   @Override
   public void sendToServer(Message m) {
      this.channel.sendToServer(m);
   }

   @Override
   public void sendToPlayer(Message m, ServerPlayer e) {
      this.channel.send(PacketDistributor.PLAYER.with(() -> e), m);
   }
}
