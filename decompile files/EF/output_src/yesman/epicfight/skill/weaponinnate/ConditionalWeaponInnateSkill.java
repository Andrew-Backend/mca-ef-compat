package yesman.epicfight.skill.weaponinnate;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Function;
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
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ConditionalWeaponInnateSkill extends WeaponInnateSkill {
   protected final AnimationManager.AnimationAccessor<? extends AttackAnimation>[] attackAnimations;
   protected final Function<ServerPlayerPatch, Integer> selector;

   public static ConditionalWeaponInnateSkill.Builder createConditionalWeaponInnateBuilder() {
      return new ConditionalWeaponInnateSkill.Builder().setCategory(SkillCategories.WEAPON_INNATE).setResource(Skill.Resource.WEAPON_CHARGE);
   }

   public ConditionalWeaponInnateSkill(ConditionalWeaponInnateSkill.Builder builder) {
      super(builder);
      this.properties = Lists.newArrayList();
      this.attackAnimations = builder.animations;
      this.selector = builder.selector;
   }

   @Override
   public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
      List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
      this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strikes:");
      return list;
   }

   public WeaponInnateSkill registerPropertiesToAnimation() {
      for (AnimationManager.AnimationAccessor<? extends AttackAnimation> animationProvider : this.attackAnimations) {
         AttackAnimation anim = animationProvider.get();

         for (AttackAnimation.Phase phase : anim.phases) {
            phase.addProperties(this.properties.get(0).entrySet());
         }
      }

      return this;
   }

   @Override
   public void executeOnServer(SkillContainer containter, FriendlyByteBuf args) {
      this.playSkillAnimation(containter.getServerExecutor());
      super.executeOnServer(containter, args);
   }

   protected int getAnimationInCondition(ServerPlayerPatch executor) {
      return this.selector.apply(executor);
   }

   protected void playSkillAnimation(ServerPlayerPatch executor) {
      executor.playAnimationSynchronized(this.attackAnimations[this.getAnimationInCondition(executor)], 0.0F);
   }

   public static class Builder extends SkillBuilder<ConditionalWeaponInnateSkill> {
      protected Function<ServerPlayerPatch, Integer> selector;
      protected AnimationManager.AnimationAccessor<? extends AttackAnimation>[] animations;

      public ConditionalWeaponInnateSkill.Builder setSelector(Function<ServerPlayerPatch, Integer> selector) {
         this.selector = selector;
         return this;
      }

      @SafeVarargs
      public final ConditionalWeaponInnateSkill.Builder setAnimations(AnimationManager.AnimationAccessor<? extends AttackAnimation>... animations) {
         this.animations = animations;
         return this;
      }
   }
}
