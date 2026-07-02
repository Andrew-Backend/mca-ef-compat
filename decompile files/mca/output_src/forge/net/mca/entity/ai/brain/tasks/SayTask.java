package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class SayTask extends Behavior<VillagerEntityMCA> {
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

   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA entity) {
      return entity.m_9236_().m_46467_() - this.lastShout > this.interval && this.condition.test(entity);
   }

   protected void run(ServerLevel world, VillagerEntityMCA entity, long time) {
      entity.sendChatToAllAround(this.phrase);
      this.lastShout = entity.m_9236_().m_46467_();
   }
}
