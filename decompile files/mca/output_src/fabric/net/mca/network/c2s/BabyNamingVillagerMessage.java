package fabric.net.mca.network.c2s;

import fabric.net.mca.cobalt.network.Message;
import fabric.net.mca.item.BabyItem;
import net.minecraft.class_1799;
import net.minecraft.class_3222;

public class BabyNamingVillagerMessage implements Message {
   private static final long serialVersionUID = -7160822837267592011L;
   private final int slot;
   private final String name;

   public BabyNamingVillagerMessage(int slot, String name) {
      this.slot = slot;
      this.name = name;
   }

   @Override
   public void receive(class_3222 player) {
      class_1799 stack = player.method_31548().method_5438(this.slot);
      BabyItem.getBabyNbt(stack).method_10582("babyName", this.name);
   }
}
