package forge.net.mca.network;

import forge.net.mca.cobalt.network.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

public abstract class NbtDataMessage implements Message {
   private static final long serialVersionUID = 3409849549326097419L;
   private final NbtDataMessage.Data data;

   public NbtDataMessage(CompoundTag data) {
      this.data = new NbtDataMessage.Data(data);
   }

   public CompoundTag getData() {
      return this.data.nbt;
   }

   private static final class Data implements Serializable {
      private static final long serialVersionUID = 5728742776742369248L;
      transient CompoundTag nbt;

      Data(CompoundTag nbt) {
         this.nbt = nbt;
      }

      private void writeObject(ObjectOutputStream out) throws IOException {
         NbtIo.m_128941_(this.nbt, out);
      }

      private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         this.nbt = NbtIo.m_128928_(in);
      }
   }
}
