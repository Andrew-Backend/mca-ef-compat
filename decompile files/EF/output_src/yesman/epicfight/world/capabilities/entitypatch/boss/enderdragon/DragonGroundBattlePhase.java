package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;

public class DragonGroundBattlePhase extends PatchedDragonPhase {
   private final List<Player> recognizedPlayers = Lists.newArrayList();
   private PathFinder pathFinder;
   private int aggroCounter;
   private int noPathWarningCounter;
   CombatBehaviors<EnderDragonPatch> combatBehaviors;

   public DragonGroundBattlePhase(EnderDragon dragon) {
      super(dragon);
      if (!dragon.m_9236_().m_5776_()) {
         this.combatBehaviors = MobCombatBehaviors.ENDER_DRAGON.build(this.dragonpatch);
         NodeEvaluator nodeEvaluator = new WalkNodeEvaluator();
         nodeEvaluator.m_77351_(true);
         this.pathFinder = new PathFinder(nodeEvaluator, 100);
      }
   }

   public void m_7083_() {
      this.dragonpatch.setGroundPhase();
   }

   public void m_6989_() {
      LivingEntity target = this.f_31176_.m_5448_();
      if (target != null) {
         if (isValidTarget(target) && isInEndSpikes(target)) {
            EntityState state = this.dragonpatch.getEntityState();
            this.combatBehaviors.tick();
            this.aggroCounter--;
            if (this.combatBehaviors.hasActivatedMove()) {
               if (state.canBasicAttack()) {
                  CombatBehaviors.Behavior<EnderDragonPatch> result = this.combatBehaviors.tryProceed();
                  if (result != null) {
                     result.execute(this.dragonpatch);
                  }
               }
            } else if (!state.inaction()) {
               CombatBehaviors.Behavior<EnderDragonPatch> result = this.combatBehaviors.selectRandomBehaviorSeries();
               if (result != null) {
                  result.execute(this.dragonpatch);
               } else {
                  if (this.f_31176_.f_19797_ % 20 == 0) {
                     if (!this.checkTargetPath(target)) {
                        if (this.noPathWarningCounter++ >= 3) {
                           this.fly();
                        }
                     } else {
                        this.noPathWarningCounter = 0;
                     }
                  }

                  double dx = target.m_20185_() - this.f_31176_.m_20185_();
                  double dz = target.m_20189_() - this.f_31176_.m_20189_();
                  float yRot = 180.0F - (float)Math.toDegrees(Mth.m_14136_(dx, dz));
                  this.f_31176_.m_146922_(MathUtils.rotlerp(this.f_31176_.m_146908_(), yRot, 6.0F));
                  Vec3 forward = this.f_31176_.m_20156_().m_82490_(-0.25);
                  this.f_31176_.m_6478_(MoverType.SELF, forward);
               }
            } else if (this.aggroCounter < 0) {
               this.aggroCounter = 200;
               this.searchNearestTarget();
            }
         } else if (!this.dragonpatch.getEntityState().inaction()) {
            this.searchNearestTarget();
         }
      } else {
         this.searchNearestTarget();
         if (this.f_31176_.m_5448_() == null && !this.dragonpatch.getEntityState().inaction()) {
            this.dragonpatch.playAnimationSynchronized(Animations.DRAGON_GROUND_TO_FLY, 0.0F);
            this.f_31176_.m_31157_().m_31416_(PatchedPhases.FLYING);
            ((DragonFlyingPhase)this.f_31176_.m_31157_().m_31415_()).enableAirstrike();
         }
      }
   }

   public float m_7584_(DamageSource damagesource, float amount) {
      if (damagesource.m_7640_() instanceof AbstractArrow) {
         damagesource.m_7640_().m_20254_(1);
         return 0.0F;
      } else {
         return super.m_7584_(damagesource, amount);
      }
   }

   private void refreshNearbyPlayers(double within) {
      this.recognizedPlayers.clear();
      this.recognizedPlayers.addAll(this.getPlayersNearbyWithin(within));
   }

   private boolean checkTargetPath(LivingEntity target) {
      BlockPos blockpos = this.f_31176_.m_20183_();

      while (this.f_31176_.m_9236_().m_8055_(blockpos).m_280555_()) {
         blockpos = blockpos.m_7494_();
      }

      while (!this.f_31176_.m_9236_().m_8055_(blockpos.m_7495_()).m_280555_()) {
         blockpos = blockpos.m_7495_();
      }

      int sight = 60;
      PathNavigationRegion pathnavigationregion = new PathNavigationRegion(
         this.f_31176_.m_9236_(), blockpos.m_7918_(-sight, -sight, -sight), blockpos.m_7918_(sight, sight, sight)
      );
      Path path = this.pathFinder.m_77427_(pathnavigationregion, this.f_31176_, ImmutableSet.of(target.m_20183_()), sight, 0, 1.0F);
      BlockPos pathEnd = path.m_77375_(path.m_77398_() - 1).m_77288_();
      BlockPos targetPos = path.m_77406_();
      double xd = Math.abs(pathEnd.m_123341_() - targetPos.m_123341_());
      double yd = Math.abs(pathEnd.m_123342_() - targetPos.m_123342_());
      double zd = Math.abs(pathEnd.m_123343_() - targetPos.m_123343_());
      return xd < this.f_31176_.m_20205_() && yd < this.f_31176_.m_20206_() && zd < this.f_31176_.m_20205_();
   }

   private void searchNearestTarget() {
      this.refreshNearbyPlayers(60.0);
      if (!this.recognizedPlayers.isEmpty()) {
         int nearestPlayerIndex = 0;
         double nearestDistance = this.recognizedPlayers.get(0).m_20280_(this.f_31176_);

         for (int i = 1; i < this.recognizedPlayers.size(); i++) {
            double distance = this.recognizedPlayers.get(i).m_20280_(this.f_31176_);
            if (distance < nearestDistance) {
               nearestPlayerIndex = i;
               nearestDistance = distance;
            }
         }

         Player nearestPlayer = this.recognizedPlayers.get(nearestPlayerIndex);
         if (isValidTarget(nearestPlayer) && isInEndSpikes(nearestPlayer)) {
            this.dragonpatch.setAttakTargetSync(nearestPlayer);
            return;
         }
      }

      this.dragonpatch.setAttakTargetSync(null);
   }

   public void fly() {
      this.combatBehaviors.execute(6);
   }

   public void resetFlyCooldown() {
      this.combatBehaviors.resetCooldown(6, false);
   }

   public boolean m_7080_() {
      return true;
   }

   public EnderDragonPhase<? extends DragonPhaseInstance> m_7309_() {
      return PatchedPhases.GROUND_BATTLE;
   }
}
