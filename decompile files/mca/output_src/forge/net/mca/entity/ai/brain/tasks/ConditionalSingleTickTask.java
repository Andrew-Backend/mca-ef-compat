package forge.net.mca.entity.ai.brain.tasks;

import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;

public class ConditionalSingleTickTask<E extends LivingEntity> extends OneShot<E> {
   private final OneShot<? super E> task;
   private final Predicate<E> predicate;

   public ConditionalSingleTickTask(OneShot<? super E> task, Predicate<E> predicate) {
      this.task = task;
      this.predicate = predicate;
   }

   public boolean m_257808_(ServerLevel world, E entity, long time) {
      return this.predicate.test(entity) && this.task.m_257808_(world, entity, time);
   }
}
