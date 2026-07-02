package fabric.net.mca.entity.ai.goal;

import fabric.net.mca.entity.GrimReaperEntity;
import fabric.net.mca.entity.ReaperAttackState;
import fabric.net.mca.entity.ai.TaskUtils;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1304;
import net.minecraft.class_1352;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_3730;

public class GrimReaperRestGoal extends class_1352 {
   private static final int COOLDOWN = 1000;
   private final GrimReaperEntity reaper;
   private int lastHeal = -1000;
   private int healingCount = 0;
   private static final int MAX_HEALING_COUNT = 5;
   private static final int MAX_HEALING_TIME = 400;
   private int healingTime;

   public GrimReaperRestGoal(GrimReaperEntity reaper) {
      this.reaper = reaper;
   }

   public boolean method_6264() {
      return this.reaper.field_6012 > this.lastHeal + 1000
         && this.reaper.method_6032() <= this.reaper.method_6063() * (1.0F - (this.healingCount + 1.0F) / 5.0F);
   }

   public boolean method_6266() {
      return this.healingTime > 0;
   }

   public boolean method_6267() {
      return false;
   }

   public void method_6269() {
      this.reaper.method_5859(this.reaper.method_23317(), this.reaper.method_23318() + 8.0, this.reaper.method_23321());
      this.healingTime = 400;
      this.lastHeal = this.reaper.field_6012;
      this.healingCount++;
   }

   public void method_6270() {
      this.reaper.setAttackState(ReaperAttackState.IDLE);
   }

   public void method_6268() {
      this.healingTime--;
      this.reaper.setAttackState(ReaperAttackState.REST);
      this.reaper.method_18799(class_243.field_1353);
      if (!this.reaper.method_37908().field_9236 && this.healingTime % (10 + this.healingCount * 5) == 0) {
         this.reaper.method_6033(this.reaper.method_6032() + 1.0F);
      }

      if (!this.reaper.method_37908().field_9236 && this.healingTime % 50 == 0) {
         int dX = this.reaper.method_6051().method_43048(16) - 8;
         int dZ = this.reaper.method_6051().method_43048(16) - 8;
         int y = TaskUtils.getSpawnSafeTopLevel(this.reaper.method_37908(), (int)this.reaper.method_23317() + dX, 256, (int)this.reaper.method_23321() + dZ);
         class_1299.field_6112
            .method_47821(
               (class_3218)this.reaper.method_37908(),
               class_2338.method_49637(this.reaper.method_23317() + dX, y, this.reaper.method_23321() + dZ),
               class_3730.field_16461
            );
         if (!this.reaper.method_37908().field_9236 && this.healingTime % 100 == 0) {
            class_1299<?> m = this.reaper.method_6051().method_43057() < 0.5F ? class_1299.field_6051 : class_1299.field_6137;
            class_1297 e = m.method_47821(
               (class_3218)this.reaper.method_37908(),
               class_2338.method_49637(this.reaper.method_23317() + dX, y, this.reaper.method_23321() + dZ),
               class_3730.field_16461
            );
            if (e != null) {
               if (m == class_1299.field_6137) {
                  e.method_5673(class_1304.field_6173, new class_1799(class_1802.field_8102));
               } else {
                  e.method_5673(class_1304.field_6173, new class_1799(class_1802.field_8371));
               }

               e.method_5673(class_1304.field_6169, new class_1799(class_1802.field_8743));
               e.method_5673(class_1304.field_6174, new class_1799(class_1802.field_8523));
               e.method_5673(class_1304.field_6172, new class_1799(class_1802.field_8396));
               e.method_5673(class_1304.field_6166, new class_1799(class_1802.field_8660));
            }
         }
      }
   }
}
