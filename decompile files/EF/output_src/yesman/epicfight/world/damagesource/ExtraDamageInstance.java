package yesman.epicfight.world.damagesource;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantments;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.weaponinnate.EviscerateSkill;

public class ExtraDamageInstance {
   public static final ExtraDamageInstance.ExtraDamage EVISCERATE_LOST_HEALTH = new ExtraDamageInstance.ExtraDamage(
      (attacker, itemstack, target, baseDamage, params) -> {
         int tier = 0;
         if (itemstack.m_41720_() instanceof TieredItem tieredItem) {
            tier += tieredItem.m_43314_().m_6604_();
         }

         return Math.min((target.m_21233_() - target.m_21223_()) * (params[0] + 0.05F * tier), ((EviscerateSkill)EpicFightSkills.EVISCERATE).getDamageCap());
      },
      (itemstack, tooltips, baseDamage, params) -> {
         int tier = 0;
         if (itemstack.m_41720_() instanceof TieredItem tieredItem) {
            tier += tieredItem.m_43314_().m_6604_();
         }

         tooltips.m_7220_(
            Component.m_237110_(
                  "damage_source.epicfight.target_lost_health",
                  new Object[]{Component.m_237113_(ItemStack.f_41584_.format((params[0] + tier * 0.05F) * 100.0F) + "%").m_130940_(ChatFormatting.RED)}
               )
               .m_130940_(ChatFormatting.DARK_GRAY)
         );
      }
   );
   public static final ExtraDamageInstance.ExtraDamage SWEEPING_EDGE_ENCHANTMENT = new ExtraDamageInstance.ExtraDamage(
      (attacker, itemstack, target, baseDamage, params) -> {
         int i = itemstack.getEnchantmentLevel(Enchantments.f_44983_);
         float modifier = i > 0 ? i / (i + 1.0F) : 0.0F;
         return baseDamage * modifier;
      },
      (itemstack, tooltips, baseDamage, params) -> {
         int i = itemstack.getEnchantmentLevel(Enchantments.f_44983_);
         if (i > 0) {
            double modifier = i / (i + 1.0);
            double damage = baseDamage * modifier;
            MutableComponent sweepedgetooltip = Component.m_237110_(
                  "damage_source.epicfight.sweeping_edge_enchant_level",
                  new Object[]{Component.m_237113_(ItemStack.f_41584_.format(damage)).m_130940_(ChatFormatting.DARK_PURPLE), i}
               )
               .m_130940_(ChatFormatting.DARK_GRAY);
            tooltips.m_7220_(sweepedgetooltip);
         }
      }
   );
   private final ExtraDamageInstance.ExtraDamage calculator;
   private final float[] params;

   public ExtraDamageInstance(ExtraDamageInstance.ExtraDamage calculator, float... params) {
      this.calculator = calculator;
      this.params = params;
   }

   public float[] getParams() {
      return this.params;
   }

   public Object[] toTransableComponentParams() {
      Object[] params = new Object[this.params.length];

      for (int i = 0; i < params.length; i++) {
         params[i] = Component.m_237113_(ItemStack.f_41584_.format(this.params[i] * 100.0F) + "%").m_130940_(ChatFormatting.RED);
      }

      return params;
   }

   public float get(LivingEntity attacker, ItemStack hurtItem, LivingEntity target, float baseDamage) {
      return this.calculator.extraDamage.getBonusDamage(attacker, hurtItem, target, baseDamage, this.params);
   }

   public void setTooltips(ItemStack itemstack, MutableComponent tooltip, double baseDamage) {
      this.calculator.tooltip.setTooltip(itemstack, tooltip, baseDamage, this.params);
   }

   public static class ExtraDamage {
      ExtraDamageInstance.ExtraDamageFunction extraDamage;
      ExtraDamageInstance.ExtraDamageTooltipFunction tooltip;

      public ExtraDamage(ExtraDamageInstance.ExtraDamageFunction extraDamage, ExtraDamageInstance.ExtraDamageTooltipFunction tooltip) {
         this.extraDamage = extraDamage;
         this.tooltip = tooltip;
      }

      public ExtraDamageInstance create(float... params) {
         return new ExtraDamageInstance(this, params);
      }
   }

   @FunctionalInterface
   public interface ExtraDamageFunction {
      float getBonusDamage(LivingEntity var1, ItemStack var2, LivingEntity var3, float var4, float[] var5);
   }

   @FunctionalInterface
   public interface ExtraDamageTooltipFunction {
      void setTooltip(ItemStack var1, MutableComponent var2, double var3, float[] var5);
   }
}
