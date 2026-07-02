package yesman.epicfight.world.entity.ai.attribute;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class EpicFightAttributeSupplier extends AttributeSupplier {
   private static Map<Attribute, AttributeInstance> putEpicFightAttributes(Map<Attribute, AttributeInstance> originalMap) {
      AttributeSupplier supplier = AttributeSupplier.m_22244_()
         .m_22266_(Attributes.f_22281_)
         .m_22266_((Attribute)EpicFightAttributes.WEIGHT.get())
         .m_22266_((Attribute)EpicFightAttributes.IMPACT.get())
         .m_22266_((Attribute)EpicFightAttributes.ARMOR_NEGATION.get())
         .m_22266_((Attribute)EpicFightAttributes.MAX_STRIKES.get())
         .m_22266_((Attribute)EpicFightAttributes.STUN_ARMOR.get())
         .m_22266_((Attribute)EpicFightAttributes.EXECUTION_RESISTANCE.get())
         .m_22266_((Attribute)EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())
         .m_22266_((Attribute)EpicFightAttributes.OFFHAND_IMPACT.get())
         .m_22266_((Attribute)EpicFightAttributes.OFFHAND_MAX_STRIKES.get())
         .m_22266_((Attribute)EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())
         .m_22265_();
      Map<Attribute, AttributeInstance> newMap = new HashMap<>(supplier.f_22241_);
      newMap.putAll(originalMap);
      return ImmutableMap.copyOf(newMap);
   }

   public EpicFightAttributeSupplier(AttributeSupplier copy) {
      super(putEpicFightAttributes(copy.f_22241_));
   }
}
