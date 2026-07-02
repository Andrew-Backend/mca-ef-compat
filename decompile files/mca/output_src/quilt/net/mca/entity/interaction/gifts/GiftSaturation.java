package quilt.net.mca.entity.interaction.gifts;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import quilt.net.mca.Config;
import quilt.net.mca.util.NbtHelper;

public class GiftSaturation {
   private List<class_2960> values = new LinkedList<>();

   public void add(class_1799 stack) {
      if (!stack.method_7960()) {
         class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
         this.values.add(id);

         while (this.values.size() > Config.getInstance().giftDesaturationQueueLength) {
            this.pop();
         }
      }
   }

   public int get(class_1799 stack) {
      class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
      return (int)this.values.stream().filter(v -> v.equals(id)).count();
   }

   public void readFromNbt(class_2499 nbt) {
      this.values = NbtHelper.toList(nbt, v -> new class_2960(v.method_10714()));
   }

   public class_2499 toNbt() {
      return NbtHelper.fromList(this.values, v -> class_2519.method_23256(v.toString()));
   }

   public void pop() {
      if (!this.values.isEmpty()) {
         this.values.remove(0);
      }
   }
}
