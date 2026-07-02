package fabric.net.mca.entity.ai.goal;

import fabric.net.mca.entity.GrimReaperEntity;
import java.util.EnumSet;
import net.minecraft.class_1352;
import net.minecraft.class_243;
import net.minecraft.class_5531;
import net.minecraft.class_1352.class_4134;
import org.jetbrains.annotations.Nullable;

public class GrimReaperIdleGoal extends class_1352 {
   protected final GrimReaperEntity reaper;
   protected final double speedModifier;
   protected final int interval;
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;

   public GrimReaperIdleGoal(GrimReaperEntity reaper, double speed) {
      this(reaper, speed, 120);
   }

   public GrimReaperIdleGoal(GrimReaperEntity reaper, double speed, int interval) {
      this.reaper = reaper;
      this.speedModifier = speed;
      this.interval = interval;
      this.method_6265(EnumSet.of(class_4134.field_18405));
   }

   public boolean method_6264() {
      if (this.reaper.method_6051().method_43048(this.interval) != 0) {
         return false;
      }

      class_243 vector3d = this.getPosition();
      if (vector3d == null) {
         return false;
      }

      this.wantedX = vector3d.field_1352;
      this.wantedY = vector3d.field_1351;
      this.wantedZ = vector3d.field_1350;
      return true;
   }

   @Nullable
   protected class_243 getPosition() {
      return this.reaper.method_5968() != null
         ? this.reaper.method_5968().method_19538()
         : class_5531.method_31508(this.reaper, 8, 6, -1, class_243.method_24955(this.reaper.method_24515()), 1.0);
   }

   public boolean method_6266() {
      return !this.reaper.method_5942().method_6357();
   }

   public void method_6269() {
      this.reaper.method_5942().method_6337(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
   }

   public void method_6270() {
      this.reaper.method_5942().method_6340();
      super.method_6270();
   }
}
