package yesman.epicfight.world.capabilities.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public class DragonFireballPatch extends ProjectilePatch<DragonFireball> {
   public void onJoinWorld(DragonFireball projectileEntity, EntityJoinLevelEvent event) {
      super.onJoinWorld(projectileEntity, event);
      this.impact = 1.0F;
      projectileEntity.f_36813_ *= 2.0;
      projectileEntity.f_36814_ *= 2.0;
      projectileEntity.f_36815_ *= 2.0;
   }

   protected void setMaxStrikes(DragonFireball projectileEntity, int maxStrikes) {
   }

   @Override
   public boolean onProjectileImpact(ProjectileImpactEvent event) {
      if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
         Entity entity = entityHitResult.m_82443_();
         if (!entity.m_9236_().m_5776_() && !entity.m_7306_(event.getProjectile().m_19749_())) {
            entity.m_6469_(entity.m_9236_().m_269111_().m_269104_(event.getProjectile(), event.getProjectile().m_19749_()), 8.0F);
         }
      }

      return false;
   }

   @Override
   public EpicFightDamageSource createEpicFightDamageSource() {
      return null;
   }
}
