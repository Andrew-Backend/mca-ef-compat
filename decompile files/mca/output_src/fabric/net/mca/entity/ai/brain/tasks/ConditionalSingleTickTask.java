package fabric.net.mca.entity.ai.brain.tasks;

import java.util.function.Predicate;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import net.minecraft.class_7894;

public class ConditionalSingleTickTask<E extends class_1309> extends class_7894<E> {
   private final class_7894<? super E> task;
   private final Predicate<E> predicate;

   public ConditionalSingleTickTask(class_7894<? super E> task, Predicate<E> predicate) {
      this.task = task;
      this.predicate = predicate;
   }

   public boolean trigger(class_3218 world, E entity, long time) {
      return this.predicate.test(entity) && this.task.trigger(world, entity, time);
   }
}
