package yesman.epicfight.world.capabilities.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class BowCapability extends RangedWeaponCapability {
   protected BowCapability(CapabilityItem.Builder builder) {
      super(builder);
   }

   @Override
   public LivingMotion getLivingMotion(LivingEntityPatch<?> entitypatch, InteractionHand hand) {
      return entitypatch.getOriginal().m_6117_() && entitypatch.getOriginal().m_21211_().m_41780_() == UseAnim.BOW ? LivingMotions.AIM : null;
   }
}
