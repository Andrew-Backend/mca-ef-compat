package fabric.net.mca.client.model;

import com.google.common.collect.ImmutableList;
import fabric.net.mca.Config;
import fabric.net.mca.entity.VillagerLike;
import fabric.net.mca.entity.ai.relationship.AgeState;
import fabric.net.mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_572;
import net.minecraft.class_630;

public class VillagerEntityBaseModelMCA<T extends class_1309 & VillagerLike<T>> extends class_572<T> implements CommonVillagerModel<T> {
   protected static final String BREASTS = "breasts";
   public final class_630 breasts;
   final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
   float breastSize;

   public VillagerEntityBaseModelMCA(class_630 root) {
      super(root);
      this.breasts = root.method_32086("breasts");
   }

   public static class_5609 getModelData(class_5605 dilation) {
      class_5609 modelData = class_572.method_32011(dilation, 0.0F);
      class_5610 data = modelData.method_32111();
      data.method_32117("breasts", newBreasts(dilation, 0), class_5603.field_27701);
      return modelData;
   }

   protected static class_5606 newBreasts(class_5605 dilation, int oy) {
      class_5606 builder = class_5606.method_32108();
      if (Config.getInstance().enableBoobs) {
         builder.method_32101(18, 21 + oy).method_32098(-3.25F, -1.25F, -1.5F, 6.0F, 3.0F, 3.0F, dilation);
      }

      return builder;
   }

   protected Iterable<class_630> method_22946() {
      return ImmutableList.of(this.field_3398, this.field_3394);
   }

   protected Iterable<class_630> method_22948() {
      return ImmutableList.of(this.field_3391, this.field_3401, this.field_27433, this.field_3392, this.field_3397);
   }

   public void method_17086(T entity, float limbAngle, float limbDistance, float tickDelta) {
      super.method_17086(entity, limbDistance, limbAngle, tickDelta);
      this.field_3449 = this.field_3449 | entity.getAgeState() == AgeState.BABY;
   }

   public void method_17087(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      if (villager.getAgeState() == AgeState.BABY && !villager.method_5765()) {
         limbDistance = (float)Math.sin(villager.field_6012 / 12.0F);
         limbAngle = (float)Math.cos(villager.field_6012 / 9.0F) * 3.0F;
         headYaw += (float)Math.sin(villager.field_6012 / 2.0F);
      }

      if (villager.method_6109()) {
         limbAngle /= 3.0F;
      }

      limbAngle /= 0.2F + villager.getRawScaleFactor();
      super.method_17087(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      if (villager.getVillagerBrain().isPanicking()) {
         float toRadians = (float) (Math.PI / 180.0);
         float armRaise = ((float)Math.sin(animationProgress / 5.0F) * 30.0F - 180.0F + (float)Math.sin(animationProgress / 3.0F) * 3.0F) * toRadians;
         float waveSideways = ((float)Math.sin(animationProgress / 2.0F) * 12.0F - 17.0F) * toRadians;
         this.field_27433.field_3654 = armRaise;
         this.field_27433.field_3674 = -waveSideways;
         this.field_3401.field_3654 = -armRaise;
         this.field_3401.field_3674 = waveSideways;
      }

      this.applyVillagerDimensions(villager, villager.method_18276());
   }

   public void method_2818(class_572<T> target) {
      super.method_2818(target);
      if (target instanceof VillagerEntityBaseModelMCA<T> m) {
         this.copyCommonAttributes(m);
         m.breasts.field_3665 = this.breasts.field_3665;
         m.breasts.method_17138(this.breasts);
      }
   }

   public void method_2828(class_4587 matrices, class_4588 vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.renderCommon(matrices, vertices, light, overlay, red, green, blue, alpha);
   }

   @Override
   public class_630 getBreastPart() {
      return this.breasts;
   }

   @Override
   public class_630 getBodyPart() {
      return this.field_3391;
   }

   @Override
   public Iterable<class_630> getCommonHeadParts() {
      return this.method_22946();
   }

   @Override
   public Iterable<class_630> getCommonBodyParts() {
      return this.method_22948();
   }

   @Override
   public Iterable<class_630> getBreastParts() {
      return ImmutableList.of(this.breasts);
   }

   @Override
   public VillagerDimensions.Mutable getDimensions() {
      return this.dimensions;
   }

   @Override
   public float getBreastSize() {
      return this.breastSize;
   }

   @Override
   public void setBreastSize(float breastSize) {
      this.breastSize = breastSize;
   }
}
