package yesman.epicfight.world.entity;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class WitherGhostClone extends FlyingMob {
   public WitherGhostClone(EntityType<? extends FlyingMob> entityType, Level level) {
      super(entityType, level);
      this.m_20242_(true);
      this.f_19794_ = true;
   }

   public WitherGhostClone(ServerLevel level, Vec3 position, LivingEntity target) {
      this((EntityType<? extends FlyingMob>)EpicFightEntities.WITHER_GHOST_CLONE.get(), level);
      this.m_146884_(position);
      this.m_7618_(Anchor.FEET, target.m_20182_());
      this.m_6710_(target);
   }

   public boolean m_6469_(DamageSource damagesource, float damage) {
      return !damagesource.m_269533_(DamageTypeTags.f_268738_) ? false : super.m_6469_(damagesource, damage);
   }

   public static Builder createAttributes() {
      return Mob.m_21552_()
         .m_22266_((Attribute)EpicFightAttributes.WEIGHT.get())
         .m_22266_((Attribute)EpicFightAttributes.ARMOR_NEGATION.get())
         .m_22266_((Attribute)EpicFightAttributes.IMPACT.get())
         .m_22266_((Attribute)EpicFightAttributes.MAX_STRIKES.get())
         .m_22266_(Attributes.f_22281_);
   }

   public void m_8024_() {
      if (this.f_19797_ >= 40) {
         this.m_142687_(RemovalReason.DISCARDED);
      }
   }

   public MobType m_6336_() {
      return MobType.f_21641_;
   }
}
