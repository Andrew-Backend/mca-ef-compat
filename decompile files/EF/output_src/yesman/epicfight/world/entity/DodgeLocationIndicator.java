package yesman.epicfight.world.entity;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class DodgeLocationIndicator extends LivingEntity {
   private static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
   private LivingEntityPatch<?> entitypatch;

   public DodgeLocationIndicator(EntityType<? extends LivingEntity> type, Level level) {
      super(type, level);
   }

   public DodgeLocationIndicator(LivingEntityPatch<?> entitypatch) {
      this((EntityType<? extends LivingEntity>)EpicFightEntities.DODGE_LOCATION_INDICATOR.get(), entitypatch.getOriginal().m_9236_());
      this.entitypatch = entitypatch;
      this.m_146884_(entitypatch.getOriginal().m_20182_());
      this.m_20011_(entitypatch.getOriginal().m_20191_().m_82363_(1.0, 0.0, 1.0));
      if (this.m_9236_().m_5776_()) {
         this.m_146870_();
      }
   }

   public void m_8119_() {
      if (this.f_19797_ > 5) {
         this.m_146870_();
      }
   }

   public boolean m_6469_(DamageSource damageSource, float amount) {
      if (this.m_9236_().m_5776_()) {
         return false;
      }

      if (!DodgeAnimation.DODGEABLE_SOURCE_VALIDATOR.apply(damageSource).dealtDamage()) {
         this.entitypatch.onDodgeSuccess(damageSource, this.m_20191_().m_82399_());
      }

      this.m_146870_();
      return false;
   }

   public Iterable<ItemStack> m_6168_() {
      return EMPTY_LIST;
   }

   public ItemStack m_6844_(EquipmentSlot p_21127_) {
      return ItemStack.f_41583_;
   }

   public void m_8061_(EquipmentSlot p_21036_, ItemStack p_21037_) {
   }

   public HumanoidArm m_5737_() {
      return HumanoidArm.RIGHT;
   }
}
