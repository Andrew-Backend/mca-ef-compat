package fabric.net.mca.client.model;

import com.google.common.collect.ImmutableList;
import fabric.net.mca.entity.VillagerLike;
import net.minecraft.class_1309;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_572;
import net.minecraft.class_591;
import net.minecraft.class_630;

public class VillagerEntityModelMCA<T extends class_1309 & VillagerLike<T>> extends VillagerEntityBaseModelMCA<T> {
   protected static final String BREASTPLATE = "breastplate";
   public final class_630 breastsWear;
   public final class_630 leftArmwear;
   public final class_630 rightArmwear;
   public final class_630 leftLegwear;
   public final class_630 rightLegwear;
   public final class_630 bodyWear;
   private boolean wearsHidden;

   public VillagerEntityModelMCA(class_630 tree) {
      super(tree);
      this.bodyWear = tree.method_32086("jacket");
      this.leftArmwear = tree.method_32086("left_sleeve");
      this.rightArmwear = tree.method_32086("right_sleeve");
      this.leftLegwear = tree.method_32086("left_pants");
      this.rightLegwear = tree.method_32086("right_pants");
      this.breastsWear = tree.method_32086("breastplate");
   }

   public static class_5609 hairData(class_5605 dilation) {
      class_5609 modelData = bodyData(dilation);
      class_5610 root = modelData.method_32111();
      root.method_32117(
         "hat",
         class_5606.method_32108().method_32101(32, 0).method_32098(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation.method_32094(0.3F)),
         class_5603.field_27701
      );
      return modelData;
   }

   public static class_5609 bodyData(class_5605 dilation) {
      return bodyData(dilation, false);
   }

   public static class_5609 bodyData(class_5605 dilation, boolean slim) {
      class_5609 modelData = class_591.method_32028(dilation, slim);
      class_5610 root = modelData.method_32111();
      root.method_32117("breasts", newBreasts(dilation, 0), class_5603.field_27701);
      root.method_32117("breastplate", newBreasts(dilation.method_32094(0.1F), 16), class_5603.field_27701);
      return modelData;
   }

   public static class_5609 armorData(class_5605 dilation) {
      class_5609 modelData = class_572.method_32011(dilation, 0.0F);
      class_5610 root = modelData.method_32111();
      root.method_32117("breasts", newBreasts(dilation, 0), class_5603.field_27701);
      return modelData;
   }

   @Override
   protected Iterable<class_630> method_22948() {
      return ImmutableList.of(
         this.field_3391,
         this.field_3401,
         this.field_27433,
         this.field_3392,
         this.field_3397,
         this.bodyWear,
         this.leftLegwear,
         this.rightLegwear,
         this.leftArmwear,
         this.rightArmwear
      );
   }

   @Override
   public Iterable<class_630> getBreastParts() {
      return ImmutableList.of(this.breasts, this.breastsWear);
   }

   @Override
   public void method_17087(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      super.method_17087(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      this.leftLegwear.method_17138(this.field_3397);
      this.rightLegwear.method_17138(this.field_3392);
      this.leftArmwear.method_17138(this.field_27433);
      this.rightArmwear.method_17138(this.field_3401);
      this.bodyWear.method_17138(this.field_3391);
      this.breastsWear.method_17138(this.breasts);
   }

   public void method_2805(boolean visible) {
      super.method_2805(visible);
      this.leftArmwear.field_3665 = !this.wearsHidden && visible;
      this.rightArmwear.field_3665 = !this.wearsHidden && visible;
      this.leftLegwear.field_3665 = !this.wearsHidden && visible;
      this.rightLegwear.field_3665 = !this.wearsHidden && visible;
      this.bodyWear.field_3665 = !this.wearsHidden && visible;
   }

   public VillagerEntityModelMCA<T> hideWears() {
      this.wearsHidden = true;
      this.breastsWear.field_3665 = false;
      this.leftArmwear.field_3665 = false;
      this.rightArmwear.field_3665 = false;
      this.leftLegwear.field_3665 = false;
      this.rightLegwear.field_3665 = false;
      this.bodyWear.field_3665 = false;
      return this;
   }

   @Override
   public void method_2818(class_572<T> target) {
      super.method_2818(target);
      if (target instanceof VillagerEntityModelMCA) {
         this.copyAttributes((VillagerEntityModelMCA<T>)target);
      }
   }

   private void copyAttributes(VillagerEntityModelMCA<T> target) {
      target.leftLegwear.method_17138(this.leftLegwear);
      target.rightLegwear.method_17138(this.rightLegwear);
      target.leftArmwear.method_17138(this.leftArmwear);
      target.rightArmwear.method_17138(this.rightArmwear);
      target.bodyWear.method_17138(this.bodyWear);
      target.breastsWear.method_17138(this.breastsWear);
   }

   public <M extends class_572<T>> void copyVisibility(M model) {
      this.field_3398.field_3665 = model.field_3398.field_3665;
      this.field_3394.field_3665 = model.field_3398.field_3665;
      this.field_3391.field_3665 = model.field_3391.field_3665;
      this.bodyWear.field_3665 = model.field_3391.field_3665;
      this.breasts.field_3665 = model.field_3391.field_3665;
      this.breastsWear.field_3665 = model.field_3391.field_3665;
      this.field_27433.field_3665 = model.field_27433.field_3665;
      this.leftArmwear.field_3665 = model.field_27433.field_3665;
      this.field_3401.field_3665 = model.field_3401.field_3665;
      this.rightArmwear.field_3665 = model.field_3401.field_3665;
      this.field_3397.field_3665 = model.field_3397.field_3665;
      this.leftLegwear.field_3665 = model.field_3397.field_3665;
      this.field_3392.field_3665 = model.field_3392.field_3665;
      this.rightLegwear.field_3665 = model.field_3392.field_3665;
   }
}
