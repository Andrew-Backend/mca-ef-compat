package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import javax.annotation.Nullable;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonFlyingPhase extends PatchedDragonPhase {
   private Path currentPath;
   private Vec3 targetLocation;
   private boolean clockwise;
   private boolean executeAirstrike;

   public DragonFlyingPhase(EnderDragon p_31230_) {
      super(p_31230_);
   }

   public EnderDragonPhase<DragonFlyingPhase> m_7309_() {
      return PatchedPhases.FLYING;
   }

   public void m_6989_() {
      double d0 = this.targetLocation == null
         ? 0.0
         : this.targetLocation.m_82531_(this.f_31176_.m_20185_(), this.f_31176_.m_20186_(), this.f_31176_.m_20189_());
      if (d0 < 100.0 || d0 > 22500.0 || this.f_31176_.f_19862_ || this.f_31176_.f_19863_ && this.f_31176_.m_31158_() != null) {
         this.findNewTarget();
      }
   }

   public void m_7083_() {
      this.currentPath = null;
      this.targetLocation = null;
   }

   @Nullable
   public Vec3 m_5535_() {
      return this.dragonpatch.getEntityState().inaction() ? null : this.targetLocation;
   }

   public void enableAirstrike() {
      this.executeAirstrike = false;
   }

   private void findNewTarget() {
      if (this.currentPath != null && this.currentPath.m_77392_()) {
         for (Player player : this.getPlayersNearbyWithin(100.0)) {
            if (isValidTarget(player)) {
               if (!this.executeAirstrike && this.f_31176_.m_217043_().m_188501_() > this.dragonpatch.getNearbyCrystals() * 0.1F) {
                  if (isInEndSpikes(player)) {
                     this.executeAirstrike = true;
                  }

                  this.dragonpatch.setAttakTargetSync(player);
                  this.f_31176_.m_31157_().m_31416_(PatchedPhases.AIRSTRIKE);
               } else if (isInEndSpikes(player)) {
                  this.f_31176_.m_31157_().m_31416_(PatchedPhases.LANDING);
               }

               return;
            }
         }
      }

      if (this.currentPath == null || this.currentPath.m_77392_()) {
         int j = this.f_31176_.m_31155_();
         int k = j;
         if (this.f_31176_.m_217043_().m_188503_(8) == 0) {
            this.clockwise = !this.clockwise;
            k = j + 6;
         }

         if (this.clockwise) {
            k++;
         } else {
            k--;
         }

         if (this.f_31176_.m_31158_() != null && this.dragonpatch.getNearbyCrystals() >= 0) {
            k %= 12;
            if (k < 0) {
               k += 12;
            }
         } else {
            k -= 12;
            k &= 7;
            k += 12;
         }

         this.currentPath = this.f_31176_.m_31104_(j, k, null);
         if (this.currentPath != null) {
            this.currentPath.m_77374_();
         }
      }

      this.navigateToNextPathNode();
   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null && !this.currentPath.m_77392_()) {
         Vec3i vec3i = this.currentPath.m_77400_();
         this.currentPath.m_77374_();
         double d0 = vec3i.m_123341_();
         double d1 = vec3i.m_123343_();

         double d2;
         do {
            d2 = vec3i.m_123342_() + this.f_31176_.m_217043_().m_188501_() * 20.0F;
         } while (d2 < vec3i.m_123342_());

         this.targetLocation = new Vec3(d0, d2, d1);
      }
   }
}
