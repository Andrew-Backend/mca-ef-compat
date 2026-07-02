package fabric.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import fabric.net.mca.entity.VillagerEntityMCA;
import net.minecraft.class_1268;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_3218;
import net.minecraft.class_4097;

public class HoldItemTask extends class_4097<VillagerEntityMCA> {
   private final class_1268 hand;
   private final class_1799 item;

   public HoldItemTask(class_1268 hand, class_1792 item) {
      this(hand, new class_1799(item));
   }

   public HoldItemTask(class_1268 hand, class_1799 item) {
      super(ImmutableMap.of());
      this.hand = hand;
      this.item = item;
   }

   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      villager.method_6122(this.hand, this.item);
   }
}
