package yesman.epicfight.world.item;

import java.util.function.Supplier;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public enum EpicFightItemTier implements Tier {
   UCHIGATANA(4, 1625, 9.0F, 6.0F, 22, () -> Ingredient.m_43929_(new ItemLike[]{Items.f_42418_})),
   GLOVE(4, 255, 9.0F, 0.0F, 16, () -> Ingredient.m_43929_(new ItemLike[]{Items.f_42416_}));

   private final int harvestLevel;
   private final int maxUses;
   private final float efficiency;
   private final float attackDamage;
   private final int enchantability;
   private final LazyLoadedValue<Ingredient> repairMaterial;

   EpicFightItemTier(int harvestLevelIn, int maxUsesIn, float efficiencyIn, float attackDamageIn, int enchantabilityIn, Supplier<Ingredient> repairMaterialIn) {
      this.harvestLevel = harvestLevelIn;
      this.maxUses = maxUsesIn;
      this.efficiency = efficiencyIn;
      this.attackDamage = attackDamageIn;
      this.enchantability = enchantabilityIn;
      this.repairMaterial = new LazyLoadedValue(repairMaterialIn);
   }

   public int m_6609_() {
      return this.maxUses;
   }

   public float m_6624_() {
      return this.efficiency;
   }

   public float m_6631_() {
      return this.attackDamage;
   }

   public int m_6604_() {
      return this.harvestLevel;
   }

   public int m_6601_() {
      return this.enchantability;
   }

   public Ingredient m_6282_() {
      return (Ingredient)this.repairMaterial.m_13971_();
   }
}
