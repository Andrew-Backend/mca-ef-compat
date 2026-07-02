package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.Items;

public class BowTask<E extends Mob & CrossbowAttackMob> extends Behavior<E> {
   private int lastShot;
   private final int fireInterval;
   private final int squaredRange;

   public BowTask(int fireInterval, int range) {
      super(ImmutableMap.of(MemoryModuleType.f_26371_, MemoryStatus.REGISTERED, MemoryModuleType.f_26372_, MemoryStatus.VALUE_PRESENT));
      this.fireInterval = fireInterval;
      this.squaredRange = range * range;
   }

   protected boolean shouldRun(ServerLevel serverWorld, E entity) {
      LivingEntity livingEntity = getAttackTarget(entity);
      return livingEntity != null
         && entity.m_21055_(Items.f_42411_)
         && BehaviorUtils.m_22667_(entity, livingEntity)
         && BehaviorUtils.m_22632_(entity, livingEntity, 0);
   }

   protected void keepRunning(ServerLevel world, E entity, long time) {
      super.m_6725_(world, entity, time);
      LivingEntity target = getAttackTarget(entity);
      double d = entity.m_20280_(target);
      float backward = 0.0F;
      if (d > this.squaredRange * 1.25F) {
         backward = 0.5F;
      } else if (d < this.squaredRange * 0.75F) {
         backward = -0.5F;
      }

      float strafe = (float)(Math.cos((float)time / 20.0F) * 0.5);
      entity.m_21566_().m_24988_(backward, strafe);
      entity.m_21391_(target, 30.0F, 30.0F);
      if (entity.f_19797_ - this.lastShot > this.fireInterval) {
         ((RangedAttackMob)entity).m_6504_(target, 1.0F);
         this.lastShot = entity.f_19797_;
      }
   }

   protected boolean shouldKeepRunning(ServerLevel world, E entity, long time) {
      return this.shouldRun(world, entity);
   }

   protected void run(ServerLevel world, E entity, long time) {
      entity.m_21561_(true);
   }

   private static LivingEntity getAttackTarget(LivingEntity entity) {
      return (LivingEntity)entity.m_6274_().m_257414_(MemoryModuleType.f_26372_).orElse(null);
   }

   protected void finishRunning(ServerLevel world, E entity, long time) {
      super.m_6732_(world, entity, time);
      entity.m_21561_(false);
   }
}
