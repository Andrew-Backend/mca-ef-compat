package quilt.net.mca.entity.ai.brain.tasks;

import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import quilt.net.mca.entity.VillagerEntityMCA;

public class LambdaTask<E extends VillagerEntityMCA> extends class_4097<E> {
   private final Consumer<E> lambda;

   public LambdaTask(Consumer<E> lambda) {
      super(Map.of());
      this.lambda = lambda;
   }

   protected boolean shouldRun(class_3218 world, E entity) {
      return true;
   }

   protected boolean shouldKeepRunning(class_3218 world, E entity, long time) {
      return false;
   }

   protected boolean method_18915(long time) {
      return false;
   }

   protected void run(class_3218 world, E entity, long time) {
      this.lambda.accept(entity);
   }
}
