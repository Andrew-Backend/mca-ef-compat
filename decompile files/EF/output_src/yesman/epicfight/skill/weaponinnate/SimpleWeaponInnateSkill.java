package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class SimpleWeaponInnateSkill extends WeaponInnateSkill {
   protected AnimationManager.AnimationAccessor<? extends AttackAnimation> attackAnimation;

   public static SimpleWeaponInnateSkill.Builder createSimpleWeaponInnateBuilder() {
      return new SimpleWeaponInnateSkill.Builder().setCategory(SkillCategories.WEAPON_INNATE).setResource(Skill.Resource.WEAPON_CHARGE);
   }

   public SimpleWeaponInnateSkill(SimpleWeaponInnateSkill.Builder builder) {
      super(builder);
      this.attackAnimation = builder.attackAnimation;
   }

   @Override
   public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
      container.getExecutor().playAnimationSynchronized(this.attackAnimation, 0.0F);
      super.executeOnServer(container, args);
   }

   @Override
   public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
      List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
      this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strike:");
      return list;
   }

   public WeaponInnateSkill registerPropertiesToAnimation() {
      AttackAnimation anim = this.attackAnimation.get();

      for (AttackAnimation.Phase phase : anim.phases) {
         phase.addProperties(this.properties.get(0).entrySet());
      }

      return this;
   }

   public static class Builder extends SkillBuilder<SimpleWeaponInnateSkill> {
      protected AnimationManager.AnimationAccessor<? extends AttackAnimation> attackAnimation;

      public SimpleWeaponInnateSkill.Builder setAnimations(AnimationManager.AnimationAccessor<? extends AttackAnimation> attackAnimation) {
         this.attackAnimation = attackAnimation;
         return this;
      }
   }
}
