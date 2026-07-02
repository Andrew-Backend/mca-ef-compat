package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import java.util.function.Predicate;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import quilt.net.mca.entity.VillagerEntityMCA;

public class SayTask extends class_4097<VillagerEntityMCA> {
   private final String phrase;
   private final int interval;
   private final Predicate<VillagerEntityMCA> condition;
   private long lastShout;

   public SayTask(String phrase) {
      this(phrase, 0, v -> true);
   }

   public SayTask(String phrase, int interval, Predicate<VillagerEntityMCA> condition) {
      super(ImmutableMap.of());
      this.phrase = phrase;
      this.interval = interval;
      this.condition = condition;
   }

   protected boolean shouldRun(class_3218 world, VillagerEntityMCA entity) {
      return entity.method_37908().method_8510() - this.lastShout > this.interval && this.condition.test(entity);
   }

   protected void run(class_3218 world, VillagerEntityMCA entity, long time) {
      entity.sendChatToAllAround(this.phrase);
      this.lastShout = entity.method_37908().method_8510();
   }
}
