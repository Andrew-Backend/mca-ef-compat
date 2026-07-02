package quilt.net.mca.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_572;
import net.minecraft.class_630;
import quilt.net.mca.entity.ai.relationship.AgeState;
import quilt.net.mca.entity.ai.relationship.VillagerDimensions;

public class PlayerArmorExtendedModel<T extends class_1309> extends class_572<T> implements CommonVillagerModel<T> {
   public final class_630 breasts;
   final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
   float breastSize;

   public PlayerArmorExtendedModel(class_630 root) {
      super(root);
      this.breasts = root.method_32086("breasts");
   }

   public void method_2818(class_572<T> target) {
      super.method_2818(target);
      if (target instanceof PlayerEntityExtendedModel<T> playerTarget) {
         this.copyAttributes(playerTarget);
      }
   }

   private void copyAttributes(PlayerEntityExtendedModel<T> target) {
      this.copyCommonAttributes(target);
      target.breasts.field_3665 = this.breasts.field_3665;
      target.breasts.method_17138(this.breasts);
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

   public void method_17087(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      if (CommonVillagerModel.getVillager(villager).getAgeState() == AgeState.BABY && !villager.method_5765()) {
         limbDistance = (float)Math.sin(villager.field_6012 / 12.0F);
         limbAngle = (float)Math.cos(villager.field_6012 / 9.0F) * 3.0F;
         headYaw += (float)Math.sin(villager.field_6012 / 2.0F);
      }

      super.method_17087(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      this.applyVillagerDimensions(CommonVillagerModel.getVillager(villager), villager.method_18276());
   }
}
