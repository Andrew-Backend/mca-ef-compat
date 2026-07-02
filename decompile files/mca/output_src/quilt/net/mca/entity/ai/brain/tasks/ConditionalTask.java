package quilt.net.mca.entity.ai.brain.tasks;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4097.class_4098;

public class ConditionalTask<E extends class_1309> extends class_4097<E> {
   private final class_4097<? super E> task;
   private final Predicate<E> predicate;

   public ConditionalTask(class_4097<? super E> task, Predicate<E> predicate) {
      super(Map.of());
      this.task = task;
      this.predicate = predicate;
   }

   protected void method_18920(class_3218 world, E entity, long time) {
      this.task.method_18922(world, entity, time);
   }

   protected void method_18924(class_3218 world, E entity, long time) {
      this.task.method_18923(world, entity, time);
   }

   protected void method_18926(class_3218 world, E entity, long time) {
      this.task.method_18925(world, entity, time);
   }

   protected boolean method_18927(class_3218 world, E entity, long time) {
      return this.predicate.test(entity) && this.task.method_18921() == class_4098.field_18338;
   }

   protected boolean method_18919(class_3218 world, E entity) {
      return this.predicate.test(entity);
   }

   protected boolean method_18915(long time) {
      return false;
   }
}
