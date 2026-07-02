package fabric.net.mca.entity.ai.brain.tasks;

import java.util.List;
import java.util.Map;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_7893;
import net.minecraft.class_4097.class_4098;

public class SequenceTask<E extends class_1309> extends class_4097<E> {
   private final List<class_7893<? super E>> tasks;
   int progress = 0;

   public SequenceTask(Map<class_4140<?>, class_4141> requiredMemoryState, List<class_7893<? super E>> tasks) {
      super(requiredMemoryState);
      this.tasks = tasks;
   }

   private class_7893<? super E> getRunningTask() {
      return this.tasks.get(this.progress);
   }

   protected void method_18920(class_3218 world, E entity, long time) {
      this.getRunningTask().method_18922(world, entity, time);
   }

   protected void method_18924(class_3218 world, E entity, long time) {
      this.tasks.stream().filter(task -> task.method_18921() == class_4098.field_18338).forEach(task -> task.method_18923(world, entity, time));
   }

   protected void method_18926(class_3218 world, E entity, long time) {
      this.progress = 0;
      this.tasks.stream().filter(task -> task.method_18921() == class_4098.field_18338).forEach(task -> task.method_18925(world, entity, time));
   }

   protected boolean method_18927(class_3218 world, E entity, long time) {
      if (this.tasks.stream().anyMatch(task -> task.method_18921() == class_4098.field_18338)) {
         return true;
      } else if (this.progress < this.tasks.size() - 1) {
         this.progress++;
         this.getRunningTask().method_18922(world, entity, time);
         return true;
      } else {
         return false;
      }
   }

   protected boolean method_18915(long time) {
      return false;
   }
}
