package forge.net.mca.cobalt.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public interface Message extends Serializable {
   static Message decode(FriendlyByteBuf b) {
      byte[] data = new byte[b.readableBytes()];
      b.readBytes(data);

      try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
         return (Message)ois.readObject();
      } catch (IOException | ClassNotFoundException e) {
         throw new RuntimeException("SneakyThrows", e);
      }
   }

   default void encode(FriendlyByteBuf b) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
         oos.writeObject(this);
      } catch (IOException e) {
         throw new RuntimeException("SneakyThrows", e);
      }

      b.writeBytes(baos.toByteArray());
   }

   default void receive() {
   }

   default void receive(ServerPlayer player) {
   }
}
