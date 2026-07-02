package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1603;
import net.minecraft.class_1802;
import net.minecraft.class_3218;
import net.minecraft.class_3745;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4215;

public class BowTask<E extends class_1308 & class_3745> extends class_4097<E> {
   private int lastShot;
   private final int fireInterval;
   private final int squaredRange;

   public BowTask(int fireInterval, int range) {
      super(ImmutableMap.of(class_4140.field_18446, class_4141.field_18458, class_4140.field_22355, class_4141.field_18456));
      this.fireInterval = fireInterval;
      this.squaredRange = range * range;
   }

   protected boolean shouldRun(class_3218 serverWorld, E entity) {
      class_1309 livingEntity = getAttackTarget(entity);
      return livingEntity != null
         && entity.method_24518(class_1802.field_8102)
         && class_4215.method_24565(entity, livingEntity)
         && class_4215.method_25940(entity, livingEntity, 0);
   }

   protected void keepRunning(class_3218 world, E entity, long time) {
      super.method_18924(world, entity, time);
      class_1309 target = getAttackTarget(entity);
      double d = entity.method_5858(target);
      float backward = 0.0F;
      if (d > this.squaredRange * 1.25F) {
         backward = 0.5F;
      } else if (d < this.squaredRange * 0.75F) {
         backward = -0.5F;
      }

      float strafe = (float)(Math.cos((float)time / 20.0F) * 0.5);
      entity.method_5962().method_6243(backward, strafe);
      entity.method_5951(target, 30.0F, 30.0F);
      if (entity.field_6012 - this.lastShot > this.fireInterval) {
         ((class_1603)entity).method_7105(target, 1.0F);
         this.lastShot = entity.field_6012;
      }
   }

   protected boolean shouldKeepRunning(class_3218 world, E entity, long time) {
      return this.shouldRun(world, entity);
   }

   protected void run(class_3218 world, E entity, long time) {
      entity.method_19540(true);
   }

   private static class_1309 getAttackTarget(class_1309 entity) {
      return (class_1309)entity.method_18868().method_46873(class_4140.field_22355).orElse(null);
   }

   protected void finishRunning(class_3218 world, E entity, long time) {
      super.method_18926(world, entity, time);
      entity.method_19540(false);
   }
}
