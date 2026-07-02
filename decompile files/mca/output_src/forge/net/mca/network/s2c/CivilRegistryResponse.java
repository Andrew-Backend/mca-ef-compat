package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;

public class CivilRegistryResponse implements Message {
   private final int index;
   private final List<String> lines;

   public CivilRegistryResponse(int index, List<Component> lines) {
      this.index = index;
      this.lines = lines.stream().<String>map(Serializer::m_130703_).collect(Collectors.toList());
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleCivilRegistryResponse(this);
   }

   public int getIndex() {
      return this.index;
   }

   public List<Component> getLines() {
      return this.lines.stream().<Component>map(Serializer::m_130701_).collect(Collectors.toList());
   }
}
