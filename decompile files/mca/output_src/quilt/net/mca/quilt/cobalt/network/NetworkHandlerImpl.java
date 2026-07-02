package quilt.net.mca.quilt.cobalt.network;

import io.netty.buffer.Unpooled;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import quilt.net.mca.cobalt.network.Message;
import quilt.net.mca.cobalt.network.NetworkHandler;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
   @Override
   public <T extends Message> void registerMessage(Class<T> msg) {
      class_2960 id = new class_2960("mca", msg.getName().toLowerCase(Locale.ENGLISH));
      ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responder) -> {
         Message m = Message.decode(buffer);
         server.execute(() -> m.receive(player));
      });
      if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
         NetworkHandlerImpl.ClientProxy.register(id, msg);
      }
   }

   @Override
   public void sendToServer(Message m) {
      class_2540 buf = new class_2540(Unpooled.buffer());
      m.encode(buf);
      ClientPlayNetworking.send(new class_2960("mca", m.getClass().getName().toLowerCase(Locale.ENGLISH)), buf);
   }

   @Override
   public void sendToPlayer(Message m, class_3222 e) {
      class_2540 buf = new class_2540(Unpooled.buffer());
      m.encode(buf);
      ServerPlayNetworking.send(e, new class_2960("mca", m.getClass().getName().toLowerCase(Locale.ENGLISH)), buf);
   }

   private static final class ClientProxy {
      private ClientProxy() {
         throw new RuntimeException("new ClientProxy()");
      }

      public static <T extends Message> void register(class_2960 id, Class<T> msg) {
         ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
            Message m = Message.decode(buffer);
            client.execute(m::receive);
         });
      }
   }
}
