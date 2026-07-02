package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RangedWeaponCapability extends CapabilityItem {
   protected Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> rangeAnimationModifiers;
   protected CapabilityItem.ZoomInType zoomInType;

   protected RangedWeaponCapability(CapabilityItem.Builder builder) {
      super(builder);
      RangedWeaponCapability.Builder rangedBuilder = (RangedWeaponCapability.Builder)builder;
      this.rangeAnimationModifiers = rangedBuilder.rangeAnimationModifiers;
      this.zoomInType = rangedBuilder.zoomInType;
   }

   @Override
   public Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifier(
      LivingEntityPatch<?> playerdata, InteractionHand hand
   ) {
      return hand == InteractionHand.MAIN_HAND ? this.rangeAnimationModifiers : super.getLivingMotionModifier(playerdata, hand);
   }

   @Override
   public boolean availableOnHorse() {
      return true;
   }

   @Override
   public boolean canBePlacedOffhand() {
      return false;
   }

   @Override
   public CapabilityItem.ZoomInType getZoomInType() {
      return this.zoomInType;
   }

   public static RangedWeaponCapability.Builder builder() {
      return new RangedWeaponCapability.Builder();
   }

   public static class Builder extends CapabilityItem.Builder {
      private Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> rangeAnimationModifiers;
      private CapabilityItem.ZoomInType zoomInType = CapabilityItem.ZoomInType.USE_TICK;

      protected Builder() {
         this.category = CapabilityItem.WeaponCategories.RANGED;
         this.constructor = RangedWeaponCapability::new;
         this.rangeAnimationModifiers = Maps.newHashMap();
      }

      public RangedWeaponCapability.Builder addAnimationsModifier(
         LivingMotion livingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation> animations
      ) {
         this.rangeAnimationModifiers.put(livingMotion, animations);
         return this;
      }

      public RangedWeaponCapability.Builder zoomInType(CapabilityItem.ZoomInType zoomInType) {
         this.zoomInType = zoomInType;
         return this;
      }
   }
}
