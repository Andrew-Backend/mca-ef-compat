package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class ArmorCapability extends CapabilityItem {
   protected static final UUID[] ARMOR_MODIFIERS_BY_SLOTS = new UUID[]{
      UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
      UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
      UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
      UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")
   };
   protected final double weight;
   protected final double stunArmor;
   private final EquipmentSlot equipmentSlot;

   protected ArmorCapability(CapabilityItem.Builder builder) {
      super(builder);
      ArmorCapability.Builder armorBuilder = (ArmorCapability.Builder)builder;
      this.equipmentSlot = armorBuilder.equipmentSlot;
      this.weight = armorBuilder.weight;
      this.stunArmor = armorBuilder.stunArmor;
   }

   @Override
   public void modifyItemTooltip(ItemStack stack, List<Component> itemTooltip, LivingEntityPatch<?> entitypatch) {
   }

   @Override
   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot, LivingEntityPatch<?> entitypatch) {
      Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
      if (equipmentSlot == this.equipmentSlot) {
         map.put(
            (Attribute)EpicFightAttributes.WEIGHT.get(),
            new AttributeModifier(ARMOR_MODIFIERS_BY_SLOTS[equipmentSlot.m_20749_()], "Armor modifier", this.weight, Operation.ADDITION)
         );
         map.put(
            (Attribute)EpicFightAttributes.STUN_ARMOR.get(),
            new AttributeModifier(ARMOR_MODIFIERS_BY_SLOTS[equipmentSlot.m_20749_()], "Armor modifier", this.stunArmor, Operation.ADDITION)
         );
      }

      return map;
   }

   public static ArmorCapability.Builder builder() {
      return new ArmorCapability.Builder();
   }

   public static class Builder extends CapabilityItem.Builder {
      EquipmentSlot equipmentSlot;
      double weight;
      double stunArmor;

      protected Builder() {
         this.constructor = ArmorCapability::new;
         this.weight = -1.0;
         this.stunArmor = -1.0;
      }

      public ArmorCapability.Builder item(Item item) {
         if (item instanceof ArmorItem armorItem) {
            ArmorMaterial armorMaterial = armorItem.m_40401_();
            Type armorType = armorItem.m_266204_();
            this.equipmentSlot = armorItem.m_40402_();
            if (this.weight < 0.0) {
               this.weight = armorMaterial.m_7366_(armorType) * 2.5F;
            }

            if (this.stunArmor < 0.0) {
               this.stunArmor = armorMaterial.m_7366_(armorType) * 0.375F;
            }
         }

         return this;
      }

      public ArmorCapability.Builder weight(double weight) {
         this.weight = weight;
         return this;
      }

      public ArmorCapability.Builder stunArmor(double stunArmor) {
         this.stunArmor = stunArmor;
         return this;
      }
   }
}
