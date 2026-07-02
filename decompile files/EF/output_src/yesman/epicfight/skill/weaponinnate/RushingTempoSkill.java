package yesman.epicfight.skill.weaponinnate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class RushingTempoSkill extends WeaponInnateSkill {
   private final Map<AnimationManager.AnimationAccessor<? extends StaticAnimation>, AnimationManager.AnimationAccessor<? extends AttackAnimation>> comboAnimation = Maps.newHashMap();

   public RushingTempoSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
      super(builder);
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
   }

   @Override
   public void onRemoved(SkillContainer container) {
   }

   @Override
   public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
      AssetAccessor<? extends DynamicAnimation> animation = container.getExecutor().<Animator>getAnimator().getPlayerFor(null).getAnimation();
      if (this.comboAnimation.containsKey(animation)) {
         container.getExecutor().playAnimationSynchronized(this.comboAnimation.get(animation), 0.0F);
         super.executeOnServer(container, args);
      }
   }

   @Override
   public boolean checkExecuteCondition(SkillContainer container) {
      EntityState playerState = container.getExecutor().getEntityState();
      return this.comboAnimation.containsKey(container.getExecutor().<Animator>getAnimator().getPlayerFor(null).getAnimation())
         && playerState.canUseSkill()
         && playerState.inaction();
   }

   @Override
   public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
      List<Component> list = Lists.newArrayList();
      String traslatableText = this.getTranslationKey();
      list.add(
         Component.m_237115_(traslatableText)
            .m_130940_(ChatFormatting.WHITE)
            .m_7220_(Component.m_237113_(String.format("[%.0f]", this.consumption)).m_130940_(ChatFormatting.AQUA))
      );
      list.add(Component.m_237110_(traslatableText + ".tooltip", new Object[]{this.maxStackSize}).m_130940_(ChatFormatting.DARK_GRAY));
      this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strike:");
      return list;
   }

   public WeaponInnateSkill registerPropertiesToAnimation() {
      this.comboAnimation.clear();
      this.comboAnimation.put(Animations.TACHI_AUTO1, Animations.RUSHING_TEMPO1);
      this.comboAnimation.put(Animations.TACHI_AUTO2, Animations.RUSHING_TEMPO2);
      this.comboAnimation.put(Animations.TACHI_AUTO3, Animations.RUSHING_TEMPO3);
      this.comboAnimation.values().forEach(animation -> animation.get().phases[0].addProperties(this.properties.get(0).entrySet()));
      return this;
   }
}
