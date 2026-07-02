package fabric.net.mca.fabric.cobalt.network;

import fabric.net.mca.MCA;
import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.cobalt.network.NetworkHandler;
import io.netty.buffer.Unpooled;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
   private final Map<Class<?>, class_2960> cache = new ConcurrentHashMap<>();

   private class_2960 getMessageIdentifier(Message msg) {
      return this.cache.computeIfAbsent(msg.getClass(), this::getMessageIdentifier);
   }

   private <T> class_2960 getMessageIdentifier(Class<T> msg) {
      return MCA.locate(msg.getSimpleName().toLowerCase(Locale.ROOT));
   }

   @Override
   public <T extends Message> void registerMessage(Class<T> msg) {
      class_2960 id = this.getMessageIdentifier(msg);
      ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responder) -> {
         Message m = Message.decode(buffer);
         server.execute(() -> m.receive(player));
      });
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         NetworkHandlerImpl.ClientProxy.register(id);
      }
   }

   @Override
   public void sendToServer(Message msg) {
      class_2540 buf = new class_2540(Unpooled.buffer());
      msg.encode(buf);
      ClientPlayNetworking.send(this.getMessageIdentifier(msg), buf);
   }

   @Override
   public void sendToPlayer(Message msg, class_3222 e) {
      class_2540 buf = new class_2540(Unpooled.buffer());
      msg.encode(buf);
      ServerPlayNetworking.send(e, this.getMessageIdentifier(msg), buf);
   }

   private static final class ClientProxy {
      private ClientProxy() {
         throw new RuntimeException("new ClientProxy()");
      }

      public static void register(class_2960 id) {
         ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
            Message m = Message.decode(buffer);
            client.execute(m::receive);
         });
      }
   }
}
