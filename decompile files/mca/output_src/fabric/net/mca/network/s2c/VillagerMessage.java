package fabric.net.mca.network.s2c;

import fabric.net.mca.ClientProxy;
import fabric.net.mca.cobalt.network.Message;
import java.util.UUID;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_2561.class_2562;

public class VillagerMessage implements Message {
   private static final long serialVersionUID = -4135222437610000843L;
   private final String prefix;
   private final String message;
   private final UUID uuid;

   public VillagerMessage(class_5250 prefix, class_5250 message, UUID uuid) {
      this.prefix = class_2562.method_10867(prefix);
      this.message = class_2562.method_10867(message);
      this.uuid = uuid;
   }

   @Override
   public void receive() {
      ClientProxy.getNetworkHandler().handleVillagerMessage(this);
   }

   public class_5250 safeLoadFromJson(String json) {
      class_5250 mutableText = class_2562.method_10877(json);
      return mutableText == null ? class_2561.method_43470("") : mutableText;
   }

   public class_5250 getMessage() {
      return this.safeLoadFromJson(this.prefix).method_10852(this.safeLoadFromJson(this.message));
   }

   public class_5250 getContent() {
      return this.safeLoadFromJson(this.message);
   }

   public UUID getUuid() {
      return this.uuid;
   }
}
