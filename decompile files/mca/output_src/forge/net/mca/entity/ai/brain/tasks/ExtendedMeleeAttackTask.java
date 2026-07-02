package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.Messenger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class ExtendedMeleeAttackTask extends Behavior<Mob> {
   private final float range;
   private final int interval;
   private final MemoryModuleType<? extends LivingEntity> target;

   public ExtendedMeleeAttackTask(int interval, float range) {
      this(interval, range, MemoryModuleType.f_26372_);
   }

   public ExtendedMeleeAttackTask(int interval, float range, MemoryModuleType<? extends LivingEntity> target) {
      super(
         ImmutableMap.of(
            MemoryModuleType.f_26371_, MemoryStatus.REGISTERED, target, MemoryStatus.VALUE_PRESENT, MemoryModuleType.f_26373_, MemoryStatus.VALUE_ABSENT
         )
      );
      this.range = range;
      this.interval = interval;
      this.target = target;
   }

   protected boolean shouldRun(ServerLevel serverWorld, Mob attacker) {
      LivingEntity target = this.getTarget(attacker);
      return BehaviorUtils.m_22667_(attacker, target) && this.withinRange(attacker, target);
   }

   protected void run(ServerLevel serverWorld, Mob mobEntity, long l) {
      LivingEntity livingEntity = this.getTarget(mobEntity);
      BehaviorUtils.m_22595_(mobEntity, livingEntity);
      if (mobEntity instanceof VillagerLike<?> villager) {
         mobEntity.m_6674_(villager.getDominantHand());
      } else {
         mobEntity.m_6674_(InteractionHand.MAIN_HAND);
      }

      mobEntity.m_7327_(livingEntity);
      mobEntity.m_6274_().m_21882_(MemoryModuleType.f_26373_, true, this.interval);
      if (livingEntity.m_21224_() && mobEntity instanceof Messenger messenger && mobEntity.m_217043_().m_188501_() < 0.3) {
         messenger.sendChatToAllAround("villager.kill");
      }
   }

   private boolean withinRange(LivingEntity attacker, LivingEntity target) {
      double d = attacker.m_20275_(target.m_20185_(), target.m_20186_(), target.m_20189_());
      double r = attacker.m_20205_() + target.m_20205_() + this.range;
      return d <= r;
   }

   private LivingEntity getTarget(Mob mobEntity) {
      return (LivingEntity)mobEntity.m_6274_().m_257414_(this.target).get();
   }
}
