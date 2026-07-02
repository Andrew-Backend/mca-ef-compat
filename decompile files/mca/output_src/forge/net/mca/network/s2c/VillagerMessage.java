package forge.net.mca.network.s2c;

import forge.net.mca.ClientProxy;
import forge.net.mca.cobalt.network.Message;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component.Serializer;

public class VillagerMessage implements Message {
   private static final long serialVersionUID = -4135222437610000843L;
   private final String prefix;
   private final String message;
   private final UUID uuid;

   public VillagerMessage(MutableComponent prefix, MutableComponent message, UUID uuid) {
      this.prefix = Serializer.m_130703_(prefix);
      this.message = Serializer.m_130703_(message);
      this.uuid = uuid;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleVillagerMessage(this);
   }

   public MutableComponent safeLoadFromJson(String json) {
      MutableComponent mutableText = Serializer.m_130701_(json);
      return mutableText == null ? Component.m_237113_("") : mutableText;
   }

   public MutableComponent getMessage() {
      return this.safeLoadFromJson(this.prefix).m_7220_(this.safeLoadFromJson(this.message));
   }

   public MutableComponent getContent() {
      return this.safeLoadFromJson(this.message);
   }

   public UUID getUuid() {
      return this.uuid;
   }
}
