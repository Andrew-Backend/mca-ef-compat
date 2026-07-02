package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_7923;

public class SetTargetMessage implements Message {
   private static final long serialVersionUID = 7257172480717481644L;
   private final String itemIdentifier;
   private final String targetName;
   private final String targetUUID;

   public SetTargetMessage(class_2960 identifier, String targetName, UUID targetUUID) {
      this.itemIdentifier = identifier.toString();
      this.targetName = targetName;
      this.targetUUID = targetUUID.toString();
   }

   @Override
   public void receive(class_3222 player) {
      Arrays.stream(class_1268.values()).forEach(hand -> {
         class_1799 stack = player.method_5998(hand);
         if (class_7923.field_41178.method_10221(stack.method_7909()).toString().equals(this.itemIdentifier)) {
            stack.method_7948().method_10582("targetName", this.targetName);
            stack.method_7948().method_25927("targetUUID", UUID.fromString(this.targetUUID));
         }
      });
   }
}
