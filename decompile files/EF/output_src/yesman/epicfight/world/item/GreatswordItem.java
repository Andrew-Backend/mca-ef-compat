package yesman.epicfight.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;

public class GreatswordItem extends WeaponItem {
   protected static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("16295ED8-B092-4A75-9A94-BCD8D56668BB");
   private final float attackDamage;
   private final float attackSpeed;

   public GreatswordItem(Properties build, Tier tier) {
      super(tier, 0, 0.0F, build);
      this.attackDamage = 11.0F + tier.m_6631_();
      this.attackSpeed = -2.85F - 0.05F * tier.m_6604_();
   }

   public int m_6473_() {
      return 5;
   }

   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      if (slot == EquipmentSlot.MAINHAND) {
         Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
         builder.put(Attributes.f_22281_, new AttributeModifier(f_41374_, "Weapon modifier", this.attackDamage, Operation.ADDITION));
         builder.put(Attributes.f_22283_, new AttributeModifier(f_41375_, "Weapon modifier", this.attackSpeed, Operation.ADDITION));
         builder.put(Attributes.f_22279_, new AttributeModifier(MOVEMENT_SPEED_MODIFIER, "Weapon modifier", -0.02, Operation.ADDITION));
         return builder.build();
      } else {
         return super.getAttributeModifiers(slot, stack);
      }
   }
}
