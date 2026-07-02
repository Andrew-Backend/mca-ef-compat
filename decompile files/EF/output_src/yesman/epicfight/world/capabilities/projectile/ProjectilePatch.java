package yesman.epicfight.world.capabilities.projectile;

import java.util.Map;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.RangedWeaponCapability;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public abstract class ProjectilePatch<T extends Projectile> extends EntityPatch<T> {
   protected float impact;
   protected float armorNegation;
   protected Vec3 initialFirePosition;
   protected boolean hasHit;

   public void onJoinWorld(T projectileEntity, EntityJoinLevelEvent event) {
      Entity shooter = projectileEntity.m_19749_();
      boolean flag = true;
      if (shooter != null && shooter instanceof LivingEntity livingshooter) {
         this.initialFirePosition = shooter.m_20182_();
         ItemStack heldItem = livingshooter.m_21205_();
         CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(heldItem);
         if (itemCap instanceof RangedWeaponCapability) {
            Map<Attribute, AttributeModifier> modifierMap = itemCap.getDamageAttributesInCondition(CapabilityItem.Styles.RANGED);
            if (modifierMap != null) {
               this.armorNegation = modifierMap.containsKey(EpicFightAttributes.ARMOR_NEGATION.get())
                  ? (float)modifierMap.get(EpicFightAttributes.ARMOR_NEGATION.get()).m_22218_()
                  : (float)((Attribute)EpicFightAttributes.ARMOR_NEGATION.get()).m_22082_();
               this.impact = modifierMap.containsKey(EpicFightAttributes.IMPACT.get())
                  ? (float)modifierMap.get(EpicFightAttributes.IMPACT.get()).m_22218_()
                  : (float)((Attribute)EpicFightAttributes.IMPACT.get()).m_22082_();
               if (modifierMap.containsKey(EpicFightAttributes.MAX_STRIKES.get())) {
                  this.setMaxStrikes(projectileEntity, (int)modifierMap.get(EpicFightAttributes.MAX_STRIKES.get()).m_22218_());
               }
            }

            flag = false;
         }
      }

      if (flag) {
         this.armorNegation = 0.0F;
         this.impact = 0.0F;
      }
   }

   @Override
   public void onAddedToWorld() {
      if (this.getOriginal().m_9236_().m_5776_()) {
         double entityId = Double.longBitsToDouble(this.getOriginal().m_19879_());
         this.getOriginal().m_9236_().m_7106_((ParticleOptions)EpicFightParticles.PROJECTILE_TRAIL.get(), entityId, 0.0, 0.0, 0.0, 0.0, 0.0);
      }
   }

   public boolean onProjectileImpact(ProjectileImpactEvent event) {
      return false;
   }

   protected abstract void setMaxStrikes(T var1, int var2);

   public abstract EpicFightDamageSource createEpicFightDamageSource();

   @Override
   public boolean overrideRender() {
      return false;
   }

   @Override
   public OpenMatrix4f getModelMatrix(float partialTicks) {
      return super.getMatrix(partialTicks);
   }

   public void setHit(boolean hit) {
      this.hasHit = hit;
   }

   public boolean hit() {
      return this.hasHit;
   }
}
