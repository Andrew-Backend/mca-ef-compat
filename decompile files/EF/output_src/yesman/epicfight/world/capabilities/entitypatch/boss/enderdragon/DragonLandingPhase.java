package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.gameasset.Animations;

public class DragonLandingPhase extends PatchedDragonPhase {
   private final BlockPos[] landingCandidates = new BlockPos[3];
   private Vec3 landingPosition;
   private boolean actualLandingPhase;

   public DragonLandingPhase(EnderDragon enderdragon) {
      super(enderdragon);
      this.landingCandidates[0] = enderdragon.m_9236_().m_5452_(Types.MOTION_BLOCKING, new BlockPos(-3, 0, -11));
      this.landingCandidates[1] = enderdragon.m_9236_().m_5452_(Types.MOTION_BLOCKING, new BlockPos(17, 0, 0));
      this.landingCandidates[2] = enderdragon.m_9236_().m_5452_(Types.MOTION_BLOCKING, new BlockPos(0, 0, 17));
   }

   public void m_7083_() {
      this.actualLandingPhase = false;
      this.landingPosition = this.getFarthestLandingPosition();
      if (!this.dragonpatch.isLogicalClient()) {
         ((DragonGroundBattlePhase)this.dragonpatch.getOriginal().m_31157_().m_31418_(PatchedPhases.GROUND_BATTLE)).resetFlyCooldown();
      }
   }

   public Vec3 getFarthestLandingPosition() {
      double max = 0.0;
      Vec3 result = null;

      for (int i = 0; i < this.landingCandidates.length; i++) {
         Vec3 vec3d = new Vec3(this.landingCandidates[i].m_123341_(), this.landingCandidates[i].m_123342_(), this.landingCandidates[i].m_123343_());
         double distanceSqr = vec3d.m_82557_(this.f_31176_.m_20182_());
         if (distanceSqr > max) {
            max = distanceSqr;
            result = vec3d;
         }
      }

      return result;
   }

   public void m_6989_() {
      double dx = this.landingPosition.f_82479_ - this.f_31176_.m_20185_();
      double dy = this.landingPosition.f_82480_ - this.f_31176_.m_20186_();
      double dz = this.landingPosition.f_82481_ - this.f_31176_.m_20189_();
      double squaredD = dx * dx + dy * dy + dz * dz;
      double squaredHorizontalD = dx * dx + dz * dz;
      if (this.actualLandingPhase) {
         if (squaredHorizontalD < 50.0) {
            this.f_31176_.m_31157_().m_31416_(PatchedPhases.GROUND_BATTLE);
         }
      } else {
         float f5 = this.m_7072_();
         double horizontalD = Math.sqrt(squaredHorizontalD);
         double yMove = dy;
         if (horizontalD > 0.0) {
            yMove = Mth.m_14008_(dy, -f5, f5) * Mth.m_14008_((Math.abs(dy) - 13.0) * 0.01, 0.01, 0.03);
         }

         this.f_31176_.m_20256_(this.f_31176_.m_20184_().m_82520_(0.0, yMove, 0.0));
         this.f_31176_.m_146922_(Mth.m_14177_(this.f_31176_.m_146908_()));
         Vec3 vec32 = this.landingPosition.m_82492_(this.f_31176_.m_20185_(), this.f_31176_.m_20186_(), this.f_31176_.m_20189_()).m_82541_();
         Vec3 vec33 = new Vec3(
               Mth.m_14031_(this.f_31176_.m_146908_() * (float) (Math.PI / 180.0)),
               this.f_31176_.m_20184_().f_82480_,
               -Mth.m_14089_(this.f_31176_.m_146908_() * (float) (Math.PI / 180.0))
            )
            .m_82541_();
         float f6 = Math.max(((float)vec33.m_82526_(vec32) + 0.5F) / 1.5F, 0.0F);
         if (Math.abs(dx) > 1.0E-5F || Math.abs(dz) > 1.0E-5F) {
            double d5 = Mth.m_14008_(Mth.m_14175_(180.0 - Mth.m_14136_(dx, dz) * 180.0F / (float)Math.PI - this.f_31176_.m_146908_()), -50.0, 50.0);
            this.f_31176_.f_31085_ *= 0.8F;
            this.f_31176_.f_31085_ = (float)(this.f_31176_.f_31085_ + d5 * this.m_7089_());
            this.f_31176_.m_146922_(this.f_31176_.m_146908_() + this.f_31176_.f_31085_ * 0.1F);
         }

         float f18 = (float)(2.0 / (squaredD + 1.0));
         this.f_31176_.m_19920_(0.06F * (f6 * f18 + (1.0F - f18)), new Vec3(0.0, 0.0, -1.0));
         if (this.f_31176_.f_31083_) {
            this.f_31176_.m_6478_(MoverType.SELF, this.f_31176_.m_20184_().m_82490_(0.8F));
         } else {
            this.f_31176_.m_6478_(MoverType.SELF, this.f_31176_.m_20184_());
         }

         Vec3 vec34 = this.f_31176_.m_20184_().m_82541_();
         double d6 = 0.8 + 0.15 * (vec34.m_82526_(vec33) + 1.0) / 2.0;
         this.f_31176_.m_20256_(this.f_31176_.m_20184_().m_82542_(d6, 0.91F, d6));
         if (squaredD < 400.0
            && Math.abs(dy) < 14.0
            && new Vec3(dx, 0.0, dz).m_82541_().m_82526_(Vec3.m_82503_(new Vec2(0.0F, 180.0F + this.f_31176_.m_146908_()))) > 0.95) {
            this.dragonpatch.playAnimationSynchronized(Animations.DRAGON_FLY_TO_GROUND, 0.0F);
            this.actualLandingPhase = true;
         }
      }
   }

   public Vec3 getLandingPosition() {
      return this.landingPosition;
   }

   public EnderDragonPhase<? extends DragonPhaseInstance> m_7309_() {
      return PatchedPhases.LANDING;
   }
}
