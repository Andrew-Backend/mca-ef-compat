package yesman.epicfight.world.item;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import yesman.epicfight.main.EpicFightMod;

public enum EpicFightArmorMaterials implements ArmorMaterial {
   STRAY_CLOTH("stray_cloth", 4, new int[]{1, 2, 3, 1}, 15, SoundEvents.f_11678_, 0.0F, 0.0F, () -> Ingredient.m_43929_(new ItemLike[]{Items.f_42401_}));

   private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
   private final String name;
   private final int enchantability;
   private final int durabilityMultiplier;
   private final int[] damageReductionAmountArray;
   private final SoundEvent soundEvent;
   private final float toughness;
   private final float knockbackResistance;
   private final LazyLoadedValue<Ingredient> repairMaterial;

   EpicFightArmorMaterials(
      String nameIn,
      int maxDamageFactorIn,
      int[] damageReductionAmountsIn,
      int enchantabilityIn,
      SoundEvent equipSoundIn,
      float toughness,
      float knockbackResistance,
      Supplier<Ingredient> repairMaterialSupplier
   ) {
      this.name = nameIn;
      this.durabilityMultiplier = maxDamageFactorIn;
      this.damageReductionAmountArray = damageReductionAmountsIn;
      this.enchantability = enchantabilityIn;
      this.soundEvent = equipSoundIn;
      this.toughness = toughness;
      this.knockbackResistance = knockbackResistance;
      this.repairMaterial = new LazyLoadedValue(repairMaterialSupplier);
   }

   public String m_6082_() {
      return EpicFightMod.prefix(this.name);
   }

   public float m_6651_() {
      return this.toughness;
   }

   public float m_6649_() {
      return this.knockbackResistance;
   }

   public int m_266425_(Type type) {
      return HEALTH_PER_SLOT[type.m_266308_().m_20749_()] * this.durabilityMultiplier;
   }

   public int m_7366_(Type type) {
      return this.damageReductionAmountArray[type.m_266308_().m_20749_()];
   }

   public int m_6646_() {
      return this.enchantability;
   }

   public SoundEvent m_7344_() {
      return this.soundEvent;
   }

   public Ingredient m_6230_() {
      return (Ingredient)this.repairMaterial.m_13971_();
   }
}
