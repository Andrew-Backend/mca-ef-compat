package forge.net.mca.entity.ai.brain.tasks;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.Behavior.Status;

public class ConditionalTask<E extends LivingEntity> extends Behavior<E> {
   private final Behavior<? super E> task;
   private final Predicate<E> predicate;

   public ConditionalTask(Behavior<? super E> task, Predicate<E> predicate) {
      super(Map.of());
      this.task = task;
      this.predicate = predicate;
   }

   protected void m_6735_(ServerLevel world, E entity, long time) {
      this.task.m_22554_(world, entity, time);
   }

   protected void m_6725_(ServerLevel world, E entity, long time) {
      this.task.m_22558_(world, entity, time);
   }

   protected void m_6732_(ServerLevel world, E entity, long time) {
      this.task.m_22562_(world, entity, time);
   }

   protected boolean m_6737_(ServerLevel world, E entity, long time) {
      return this.predicate.test(entity) && this.task.m_22536_() == Status.RUNNING;
   }

   protected boolean m_6114_(ServerLevel world, E entity) {
      return this.predicate.test(entity);
   }

   protected boolean m_7773_(long time) {
      return false;
   }
}
