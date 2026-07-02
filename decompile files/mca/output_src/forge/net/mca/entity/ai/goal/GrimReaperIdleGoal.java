package forge.net.mca.entity.ai.goal;

import forge.net.mca.entity.GrimReaperEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GrimReaperIdleGoal extends Goal {
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
      this.m_7021_(EnumSet.of(Flag.MOVE));
   }

   public boolean m_8036_() {
      if (this.reaper.m_217043_().m_188503_(this.interval) != 0) {
         return false;
      }

      Vec3 vector3d = this.getPosition();
      if (vector3d == null) {
         return false;
      }

      this.wantedX = vector3d.f_82479_;
      this.wantedY = vector3d.f_82480_;
      this.wantedZ = vector3d.f_82481_;
      return true;
   }

   @Nullable
   protected Vec3 getPosition() {
      return this.reaper.m_5448_() != null
         ? this.reaper.m_5448_().m_20182_()
         : AirRandomPos.m_148387_(this.reaper, 8, 6, -1, Vec3.m_82539_(this.reaper.m_20183_()), 1.0);
   }

   public boolean m_8045_() {
      return !this.reaper.m_21573_().m_26571_();
   }

   public void m_8056_() {
      this.reaper.m_21573_().m_26519_(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
   }

   public void m_8041_() {
      this.reaper.m_21573_().m_26573_();
      super.m_8041_();
   }
}
