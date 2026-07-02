package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import net.minecraft.class_1268;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4215;
import quilt.net.mca.entity.VillagerLike;
import quilt.net.mca.entity.ai.Messenger;

public class ExtendedMeleeAttackTask extends class_4097<class_1308> {
   private final float range;
   private final int interval;
   private final class_4140<? extends class_1309> target;

   public ExtendedMeleeAttackTask(int interval, float range) {
      this(interval, range, class_4140.field_22355);
   }

   public ExtendedMeleeAttackTask(int interval, float range, class_4140<? extends class_1309> target) {
      super(ImmutableMap.of(class_4140.field_18446, class_4141.field_18458, target, class_4141.field_18456, class_4140.field_22475, class_4141.field_18457));
      this.range = range;
      this.interval = interval;
      this.target = target;
   }

   protected boolean shouldRun(class_3218 serverWorld, class_1308 attacker) {
      class_1309 target = this.getTarget(attacker);
      return class_4215.method_24565(attacker, target) && this.withinRange(attacker, target);
   }

   protected void run(class_3218 serverWorld, class_1308 mobEntity, long l) {
      class_1309 livingEntity = this.getTarget(mobEntity);
      class_4215.method_19554(mobEntity, livingEntity);
      if (mobEntity instanceof VillagerLike<?> villager) {
         mobEntity.method_6104(villager.getDominantHand());
      } else {
         mobEntity.method_6104(class_1268.field_5808);
      }

      mobEntity.method_6121(livingEntity);
      mobEntity.method_18868().method_24525(class_4140.field_22475, true, this.interval);
      if (livingEntity.method_29504() && mobEntity instanceof Messenger messenger && mobEntity.method_6051().method_43057() < 0.3) {
         messenger.sendChatToAllAround("villager.kill");
      }
   }

   private boolean withinRange(class_1309 attacker, class_1309 target) {
      double d = attacker.method_5649(target.method_23317(), target.method_23318(), target.method_23321());
      double r = attacker.method_17681() + target.method_17681() + this.range;
      return d <= r;
   }

   private class_1309 getTarget(class_1308 mobEntity) {
      return (class_1309)mobEntity.method_18868().method_46873(this.target).get();
   }
}
