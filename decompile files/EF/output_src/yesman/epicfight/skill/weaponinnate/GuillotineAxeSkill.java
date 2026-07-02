package yesman.epicfight.skill.weaponinnate;

import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

public class GuillotineAxeSkill extends SimpleWeaponInnateSkill {
   private static final UUID EVENT_UUID = UUID.fromString("b84e577a-c653-11ed-afa1-0242ac120002");

   public GuillotineAxeSkill(SimpleWeaponInnateSkill.Builder builder) {
      super(builder);
   }

   @Override
   public void onInitiate(SkillContainer container) {
      super.onInitiate(container);
      container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID, event -> {
         if (event.getDamageSource().getAnimation() == Animations.THE_GUILLOTINE) {
            ValueModifier.ResultCalculator executionMinHealth = ValueModifier.calculator();
            this.getProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, this.properties.get(0)).ifPresent(executionMinHealth::attach);
            executionMinHealth.multiply(0.8F);
            float health = event.getTarget().m_21223_();
            float baseDamage = (float)((ServerPlayerPatch)event.getPlayerPatch()).getOriginal().m_21133_(Attributes.f_22281_);
            float modifiedBaseDamage = ((ServerPlayerPatch)event.getPlayerPatch()).getModifiedBaseDamage(baseDamage);
            float executionHealth = executionMinHealth.getResult(modifiedBaseDamage);
            if (health < executionHealth && event.getDamageSource() != null) {
               event.getDamageSource().setExecute();
            }
         }
      });
   }

   @Override
   public void onRemoved(SkillContainer container) {
      super.onRemoved(container);
      container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Component> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
      List<Component> list = Lists.newArrayList();
      List<Object> tooltipArgs = Lists.newArrayList();
      String traslatableText = this.getTranslationKey();
      double itemBaseDamage = playerpatch.getOriginal().m_21051_(Attributes.f_22281_).m_22115_() + EnchantmentHelper.m_44833_(itemstack, MobType.f_21640_);
      Set<AttributeModifier> attributeModifiers = new HashSet<>();
      attributeModifiers.addAll(playerpatch.getOriginal().m_21051_(Attributes.f_22281_).m_22122_());
      attributeModifiers.addAll(CapabilityItem.getAttributeModifiers(Attributes.f_22281_, EquipmentSlot.MAINHAND, itemstack, playerpatch));

      for (AttributeModifier modifier : attributeModifiers) {
         itemBaseDamage += modifier.m_22218_();
      }

      ValueModifier.ResultCalculator executionMinHealth = ValueModifier.calculator();
      this.getProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, this.properties.get(0)).ifPresent(executionMinHealth::attach);
      executionMinHealth.multiply(0.8F);
      tooltipArgs.add(ChatFormatting.RED + ItemStack.f_41584_.format(executionMinHealth.getResult((float)itemBaseDamage)));
      list.add(
         Component.m_237115_(traslatableText)
            .m_130940_(ChatFormatting.WHITE)
            .m_7220_(Component.m_237113_(String.format("[%.0f]", this.consumption)).m_130940_(ChatFormatting.AQUA))
      );
      list.add(Component.m_237110_(traslatableText + ".tooltip", tooltipArgs.toArray(new Object[0])).m_130940_(ChatFormatting.DARK_GRAY));
      this.generateTooltipforPhase(list, itemstack, cap, playerpatch, this.properties.get(0), "Each Strike:");
      return list;
   }
}
