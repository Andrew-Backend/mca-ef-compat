package yesman.epicfight.world.capabilities.entitypatch;

import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public abstract class HurtableEntityPatch<T extends LivingEntity> extends EntityPatch<T> {
   private boolean stunReductionDecreases;
   protected float stunTimeReductionDefault;
   protected float stunTimeReduction;
   protected boolean cancelKnockback;

   protected void updateStunTime() {
      this.cancelKnockback = false;
      if (this.stunReductionDecreases) {
         float stunArmor = this.getStunArmor();
         this.stunTimeReduction = this.stunTimeReduction
            - 0.1F * (1.1F - this.stunTimeReduction * this.stunTimeReduction) * (1.0F - stunArmor / (7.5F + stunArmor));
         if (this.stunTimeReduction < 0.0F) {
            this.stunReductionDecreases = false;
            this.stunTimeReduction = 0.0F;
         }
      } else if (this.stunTimeReduction < this.stunTimeReductionDefault) {
         this.stunTimeReduction = this.stunTimeReduction + 0.02F * (1.1F - this.stunTimeReduction * this.stunTimeReduction);
         if (this.stunTimeReduction > this.stunTimeReductionDefault) {
            this.stunTimeReduction = this.stunTimeReductionDefault;
         }
      }
   }

   @Override
   public OpenMatrix4f getModelMatrix(float partialTicks) {
      return null;
   }

   public void tick(LivingTickEvent event) {
      if (!this.original.m_9236_().m_5776_()) {
         this.updateStunTime();
      }
   }

   public abstract boolean applyStun(StunType var1, float var2);

   public float getWeight() {
      return (float)this.original.m_21133_(Attributes.f_22276_) * 2.0F;
   }

   public float getStunShield() {
      return 0.0F;
   }

   public void setStunShield(float value) {
   }

   public void damageStunShield(float damage, float impact) {
      this.setStunShield(this.getStunShield() - impact);
   }

   public void setStunReductionOnHit(StunType stunType) {
      this.stunReductionDecreases = true;
      if (stunType != StunType.NONE) {
         this.stunTimeReduction = this.stunTimeReduction + Math.max((1.0F - this.stunTimeReduction) * 0.8F, 0.5F);
         this.stunTimeReduction = Math.min(1.0F, this.stunTimeReduction);
         this.stunReductionDecreases = true;
      }
   }

   public float getStunReduction() {
      return this.stunTimeReduction;
   }

   public void setDefaultStunReduction(EquipmentSlot equipmentslot, ItemStack from, ItemStack to) {
      List<AttributeModifier> modifiersToAdd = CapabilityItem.getAttributeModifiers((Attribute)EpicFightAttributes.STUN_ARMOR.get(), equipmentslot, to, null);
      List<AttributeModifier> modifiersToRemove = CapabilityItem.getAttributeModifiers(
         (Attribute)EpicFightAttributes.STUN_ARMOR.get(), equipmentslot, from, null
      );
      AttributeInstance tempAttr = new AttributeInstance((Attribute)EpicFightAttributes.STUN_ARMOR.get(), i -> {});
      tempAttr.m_22102_(this.original.m_21051_((Attribute)EpicFightAttributes.STUN_ARMOR.get()));

      for (AttributeModifier modifier : modifiersToAdd) {
         if (!tempAttr.m_22109_(modifier)) {
            tempAttr.m_22118_(modifier);
         }
      }

      for (AttributeModifier modifier : modifiersToRemove) {
         if (tempAttr.m_22109_(modifier)) {
            tempAttr.m_22130_(modifier);
         }
      }

      float stunArmor = (float)tempAttr.m_22135_();
      this.stunReductionDecreases = stunArmor < this.getStunArmor();
      this.stunTimeReductionDefault = stunArmor / (stunArmor + 7.5F);
   }

   public float getStunArmor() {
      AttributeInstance stunArmor = this.original.m_21051_((Attribute)EpicFightAttributes.STUN_ARMOR.get());
      return (float)(stunArmor == null ? 0.0 : stunArmor.m_22135_());
   }

   public EntityState getEntityState() {
      return EntityState.DEFAULT_STATE;
   }

   public boolean shouldCancelKnockback() {
      return this.cancelKnockback;
   }

   public abstract boolean isStunned();

   public void knockBackEntity(Vec3 sourceLocation, float power) {
      double d1 = sourceLocation.m_7096_() - this.original.m_20185_();

      double d0;
      for (d0 = sourceLocation.m_7094_() - this.original.m_20189_(); d1 * d1 + d0 * d0 < 1.0E-4; d0 = (Math.random() - Math.random()) * 0.01) {
         d1 = (Math.random() - Math.random()) * 0.01;
      }

      power = (float)(power * (1.0 - this.original.m_21133_(Attributes.f_22278_)));
      if (power > 0.0) {
         this.original.f_19864_ = true;
         this.original.f_19812_ = true;
         Vec3 vec3 = this.original.m_20184_();
         Vec3 vec31 = new Vec3(d1, 0.0, d0).m_82541_().m_82490_(power);
         this.original
            .m_20334_(
               vec3.f_82479_ / 2.0 - vec31.f_82479_,
               this.original.f_19861_ ? Math.min(0.4, vec3.f_82480_ / 2.0) : vec3.f_82480_,
               vec3.f_82481_ / 2.0 - vec31.f_82481_
            );
      }
   }

   public void playSound(SoundEvent sound, float pitchModifierMin, float pitchModifierMax) {
      this.playSound(sound, 1.0F, pitchModifierMin, pitchModifierMax);
   }

   public void playSound(SoundEvent sound, float volume, float pitchModifierMin, float pitchModifierMax) {
      if (sound != null) {
         float pitch = (this.original.m_217043_().m_188501_() * 2.0F - 1.0F) * (pitchModifierMax - pitchModifierMin);
         if (!this.isLogicalClient()) {
            this.original
               .m_9236_()
               .m_6263_(
                  null, this.original.m_20185_(), this.original.m_20186_(), this.original.m_20189_(), sound, this.original.m_5720_(), volume, 1.0F + pitch
               );
         } else {
            this.original
               .m_9236_()
               .m_7785_(
                  this.original.m_20185_(), this.original.m_20186_(), this.original.m_20189_(), sound, this.original.m_5720_(), volume, 1.0F + pitch, false
               );
         }
      }
   }

   @Override
   public boolean overrideRender() {
      return false;
   }
}
