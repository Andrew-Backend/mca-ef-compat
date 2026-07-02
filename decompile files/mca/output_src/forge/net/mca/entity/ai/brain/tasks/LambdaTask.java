package forge.net.mca.entity.ai.brain.tasks;

import forge.net.mca.entity.VillagerEntityMCA;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class LambdaTask<E extends VillagerEntityMCA> extends Behavior<E> {
   private final Consumer<E> lambda;

   public LambdaTask(Consumer<E> lambda) {
      super(Map.of());
      this.lambda = lambda;
   }

   protected boolean shouldRun(ServerLevel world, E entity) {
      return true;
   }

   protected boolean shouldKeepRunning(ServerLevel world, E entity, long time) {
      return false;
   }

   protected boolean m_7773_(long time) {
      return false;
   }

   protected void run(ServerLevel world, E entity, long time) {
      this.lambda.accept(entity);
   }
}
