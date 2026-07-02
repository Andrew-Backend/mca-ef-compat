package yesman.epicfight.world.capabilities.entitypatch.mob;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class VindicatorPatch<T extends PathfinderMob> extends AbstractIllagerPatch<T> {
   public VindicatorPatch() {
      super(Factions.ILLAGER);
   }

   @Override
   public void initAnimator(Animator animator) {
      super.initAnimator(animator);
      animator.addLivingAnimation(LivingMotions.ANGRY, Animations.VINDICATOR_IDLE_AGGRESSIVE);
      animator.addLivingAnimation(LivingMotions.CHASE, Animations.VINDICATOR_CHASE);
   }

   public static void initAttributes(EntityAttributeModificationEvent event) {
      event.add(EntityType.f_20493_, (Attribute)EpicFightAttributes.IMPACT.get(), 1.0);
   }

   @Override
   protected void setWeaponMotions() {
      super.setWeaponMotions();
      this.weaponLivingMotions
         .put(
            CapabilityItem.WeaponCategories.GREATSWORD,
            ImmutableMap.of(
               CapabilityItem.Styles.TWO_HAND,
               Set.of(Pair.of(LivingMotions.WALK, Animations.ILLAGER_WALK), Pair.of(LivingMotions.CHASE, Animations.BIPED_WALK_TWOHAND))
            )
         );
      this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.AXE, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.VINDICATOR_ONEHAND));
      this.weaponAttackMotions.put(CapabilityItem.WeaponCategories.SWORD, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.VINDICATOR_ONEHAND));
   }

   @Override
   public void updateMotion(boolean considerInaction) {
      if (this.original.m_21223_() <= 0.0F) {
         this.currentLivingMotion = LivingMotions.DEATH;
      } else if (this.state.inaction() && considerInaction) {
         this.currentLivingMotion = LivingMotions.INACTION;
      } else {
         boolean isAngry = this.original.m_5912_();
         if (this.original.f_267362_.m_267731_() > 0.01F) {
            this.currentLivingMotion = isAngry ? LivingMotions.CHASE : LivingMotions.WALK;
         } else {
            this.currentLivingMotion = isAngry ? LivingMotions.ANGRY : LivingMotions.IDLE;
         }
      }
   }

   @Override
   public void setAIAsMounted(Entity ridingEntity) {
   }
}
