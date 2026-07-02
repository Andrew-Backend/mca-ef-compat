package yesman.epicfight.world.capabilities.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.mob.WitherSkeletonPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.WitherSkeletonMinion;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class WitherSkullPatch extends ProjectilePatch<WitherSkull> {
   public void onJoinWorld(WitherSkull projectileEntity, EntityJoinLevelEvent event) {
      super.onJoinWorld(projectileEntity, event);
      this.impact = 1.0F;
   }

   protected void setMaxStrikes(WitherSkull projectileEntity, int maxStrikes) {
   }

   @Override
   public boolean onProjectileImpact(ProjectileImpactEvent event) {
      if (event.getProjectile().m_9236_().m_5776_()) {
         return false;
      } else if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
         return entityHitResult.m_82443_() instanceof WitherSkeletonMinion;
      } else {
         if (event.getProjectile().m_9236_() instanceof ServerLevel serverLevel && Math.random() < 0.2) {
            Vec3 location = event.getRayTraceResult().m_82450_();
            BlockPos blockpos = new MutableBlockPos(location.f_82479_, location.f_82480_, location.f_82481_);
            Projectile projectile = event.getProjectile();
            EntityType<?> entityType = (EntityType<?>)EpicFightEntities.WITHER_SKELETON_MINION.get();
            if (NaturalSpawner.m_47051_(SpawnPlacements.m_21752_(entityType), serverLevel, blockpos, entityType)
               && SpawnPlacements.m_217074_(entityType, serverLevel, MobSpawnType.REINFORCEMENT, blockpos, serverLevel.f_46441_)
               && !EpicFightGameRules.NO_MOBS_IN_BOSSFIGHT.getRuleValue(serverLevel)) {
               WitherBoss summoner = projectile.m_19749_() instanceof WitherBoss ? (WitherBoss)projectile.m_19749_() : null;
               WitherSkeletonMinion witherskeletonminion = new WitherSkeletonMinion(
                  serverLevel, summoner, projectile.m_20185_(), projectile.m_20186_() + 0.1, projectile.m_20189_()
               );
               witherskeletonminion.m_6518_(serverLevel, serverLevel.m_6436_(blockpos), MobSpawnType.REINFORCEMENT, null, null);
               witherskeletonminion.m_146922_(projectile.m_146908_() - 180.0F);
               serverLevel.m_7967_(witherskeletonminion);
               EpicFightCapabilities.getParameterizedEntityPatch(witherskeletonminion, WitherSkeletonMinion.class, WitherSkeletonPatch.class)
                  .ifPresent(witherskeletonpatch -> witherskeletonpatch.playAnimationInstantly(Animations.WITHER_SKELETON_SPECIAL_SPAWN));
            }
         }

         return false;
      }
   }

   @Override
   public EpicFightDamageSource createEpicFightDamageSource() {
      return EpicFightDamageSources.witherSkull(this.original, this.original.m_19749_())
         .setStunType(StunType.SHORT)
         .addRuntimeTag(DamageTypeTags.f_268524_)
         .setBaseArmorNegation(this.armorNegation)
         .setBaseImpact(this.impact)
         .setInitialPosition(this.initialFirePosition);
   }
}
