package forge.net.mca.entity.ai.brain.tasks;

import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.Behavior.Status;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SequenceTask<E extends LivingEntity> extends Behavior<E> {
   private final List<BehaviorControl<? super E>> tasks;
   int progress = 0;

   public SequenceTask(Map<MemoryModuleType<?>, MemoryStatus> requiredMemoryState, List<BehaviorControl<? super E>> tasks) {
      super(requiredMemoryState);
      this.tasks = tasks;
   }

   private BehaviorControl<? super E> getRunningTask() {
      return this.tasks.get(this.progress);
   }

   protected void m_6735_(ServerLevel world, E entity, long time) {
      this.getRunningTask().m_22554_(world, entity, time);
   }

   protected void m_6725_(ServerLevel world, E entity, long time) {
      this.tasks.stream().filter(task -> task.m_22536_() == Status.RUNNING).forEach(task -> task.m_22558_(world, entity, time));
   }

   protected void m_6732_(ServerLevel world, E entity, long time) {
      this.progress = 0;
      this.tasks.stream().filter(task -> task.m_22536_() == Status.RUNNING).forEach(task -> task.m_22562_(world, entity, time));
   }

   protected boolean m_6737_(ServerLevel world, E entity, long time) {
      if (this.tasks.stream().anyMatch(task -> task.m_22536_() == Status.RUNNING)) {
         return true;
      } else if (this.progress < this.tasks.size() - 1) {
         this.progress++;
         this.getRunningTask().m_22554_(world, entity, time);
         return true;
      } else {
         return false;
      }
   }

   protected boolean m_7773_(long time) {
      return false;
   }
}
