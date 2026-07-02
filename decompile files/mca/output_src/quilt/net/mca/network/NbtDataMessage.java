package quilt.net.mca.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import net.minecraft.class_2487;
import net.minecraft.class_2507;
import quilt.net.mca.cobalt.network.Message;

public abstract class NbtDataMessage implements Message {
   private static final long serialVersionUID = 3409849549326097419L;
   private final NbtDataMessage.Data data;

   public NbtDataMessage(class_2487 data) {
      this.data = new NbtDataMessage.Data(data);
   }

   public class_2487 getData() {
      return this.data.nbt;
   }

   private static final class Data implements Serializable {
      private static final long serialVersionUID = 5728742776742369248L;
      transient class_2487 nbt;

      Data(class_2487 nbt) {
         this.nbt = nbt;
      }

      private void writeObject(ObjectOutputStream out) throws IOException {
         class_2507.method_10628(this.nbt, out);
      }

      private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         this.nbt = class_2507.method_10627(in);
      }
   }
}
