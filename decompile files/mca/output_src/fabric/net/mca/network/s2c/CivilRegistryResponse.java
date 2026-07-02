package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.cobalt.network.Message;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.class_2561;
import net.minecraft.class_2561.class_2562;

public class CivilRegistryResponse implements Message {
   private final int index;
   private final List<String> lines;

   public CivilRegistryResponse(int index, List<class_2561> lines) {
      this.index = index;
      this.lines = lines.stream().<String>map(class_2562::method_10867).collect(Collectors.toList());
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleCivilRegistryResponse(this);
   }

   public int getIndex() {
      return this.index;
   }

   public List<class_2561> getLines() {
      return this.lines.stream().<class_2561>map(class_2562::method_10877).collect(Collectors.toList());
   }
}
