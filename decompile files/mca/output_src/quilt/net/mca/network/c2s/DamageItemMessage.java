package quilt.net.mca.network.c2s;

import java.util.Arrays;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_7923;
import quilt.net.mca.cobalt.network.Message;

public class DamageItemMessage implements Message {
   private static final long serialVersionUID = -8975978126445189429L;
   private final String itemIdentifier;

   public DamageItemMessage(class_2960 identifier) {
      this.itemIdentifier = identifier.toString();
   }

   @Override
   public void receive(class_3222 player) {
      Arrays.stream(class_1268.values()).forEach(hand -> {
         class_1799 stack = player.method_5998(hand);
         if (class_7923.field_41178.method_10221(stack.method_7909()).toString().equals(this.itemIdentifier)) {
            stack.method_7956(1, player, e -> e.method_20235(class_1304.field_6173));
         }
      });
   }
}
