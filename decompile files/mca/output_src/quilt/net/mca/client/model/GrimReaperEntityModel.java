package quilt.net.mca.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_572;
import net.minecraft.class_630;
import quilt.net.mca.entity.GrimReaperEntity;
import quilt.net.mca.entity.ReaperAttackState;

public class GrimReaperEntityModel<T extends GrimReaperEntity> extends class_572<T> {
   private static final Map<ReaperAttackState, ModelTransformSet> POSES = ImmutableMap.of(
      ReaperAttackState.PRE,
      new ModelTransformSet.Builder()
         .rotate("head", -15.6F, 40.4F, 0.0F)
         .rotate("body", 0.0F, -13.0F, 0.0F)
         .rotate("left_arm", -130.0F, -112.0F, 7.8F)
         .rotate("right_arm", -36.5F, 122.6F, 0.0F)
         .rotate("left_leg", 18.0F, -13.0F, 0.0F)
         .rotate("right_leg", 13.0F, -13.0F, 0.0F)
         .rotate("scythe_handle", 0.0F, 0.0F, 90.0F)
         .build(),
      ReaperAttackState.POST,
      new ModelTransformSet.Builder()
         .rotate("head", 44.3F, 41.7F, 0.0F)
         .rotate("body", 34.0F, 34.0F, 0.0F)
         .rotate("left_arm", -44.0F, 62.0F, 7.8F)
         .with("right_arm", -5.0F, 1.7F, 3.3F, -36.5F, 122.6F, 0.0F)
         .with("left_leg", 5.4F, 9.8F, 4.6F, 28.7F, 39.0F, -2.6F)
         .with("right_leg", 2.0F, 10.0F, 6.6F, 31.3F, 34.0F, -5.2F)
         .with("scythe_handle", -10.0F, 10.0F, 0.0F, 0.0F, -10.0F, 90.0F)
         .build(),
      ReaperAttackState.BLOCK,
      new ModelTransformSet.Builder()
         .rotate("head", 7.8F, 0.0F, 0.0F)
         .with("body", 0.0F, 0.0F, 1.0F, -5.2F, 5.2F, 0.0F)
         .rotate("left_arm", -86.0F, 23.5F, 7.8F)
         .rotate("right_arm", -70.0F, 0.0F, 107.0F)
         .rotate("left_leg", -7.8F, 2.6F, 0.0F)
         .rotate("right_leg", -7.8F, 5.2F, 0.0F)
         .rotate("scythe_handle", 120.0F, 88.0F, 0.0F)
         .build(),
      ReaperAttackState.REST,
      new ModelTransformSet.Builder()
         .rotate("head", 62.6F, 0.0F, 1.8F)
         .rotate("body", 0.0F, 5.2F, 0.0F)
         .rotate("left_arm", 0.0F, 0.0F, -20.0F, ModelTransformSet.Op.ADD)
         .rotate("right_arm", 0.0F, 0.0F, 20.0F, ModelTransformSet.Op.ADD)
         .rotate("left_leg", 2.6F, 2.6F, 0.0F)
         .rotate("right_leg", 2.6F, 5.2F, 0.0F)
         .with("scythe_handle", 0.0F, 10.0F, 0.0F, 90.0F, -20.0F, 90.0F, ModelTransformSet.Op.KEEP, ModelTransformSet.Op.KEEP)
         .build()
   );
   private final class_630 scythe;
   public ReaperAttackState reaperState = ReaperAttackState.IDLE;
   private final class_5603 scytheTransform;

   public GrimReaperEntityModel(class_630 tree) {
      super(tree);
      this.scythe = tree.method_32086("left_arm").method_32086("scythe_handle");
      this.scytheTransform = this.scythe.method_32084();
   }

   public static class_5609 getModelData(class_5605 dilation) {
      class_5609 modelData = class_572.method_32011(dilation, 0.0F);
      class_5610 data = modelData.method_32111();
      data.method_32116("left_arm")
         .method_32117(
            "scythe_handle",
            class_5606.method_32108()
               .method_32101(36, 32)
               .method_32098(0.0F, -26.0F, 0.0F, 1.0F, 31.0F, 1.0F, dilation)
               .method_32101(0, 32)
               .method_32098(0.5F, -26.0F, 0.5F, 16.0F, 16.0F, 0.0F, dilation),
            ModelTransformSet.Builder.createTransform(0.0F, 10.0F, 0.0F, 90.0F, -20.0F, 90.0F)
         );
      return modelData;
   }

   public void setAngles(T entity, float f, float g, float h, float i, float j) {
      super.method_17087(entity, f, g, h, i, j);
      this.field_3391.method_2851(0.0F, 0.0F, 0.0F);
      this.field_3391.method_33425(0.0F, 0.0F, 0.0F);
      this.field_3397.method_2851(1.9F, 12.0F, 0.0F);
      this.field_3397.method_33425(0.0F, 0.0F, 0.0F);
      this.field_3392.method_2851(-1.9F, 12.0F, 0.0F);
      this.field_3392.method_33425(0.0F, 0.0F, 0.0F);
      this.scythe.method_32085(this.scytheTransform);
      this.reaperState = entity.getAttackState();
      ModelTransformSet set = POSES.get(this.reaperState);
      if (set != null) {
         set.get("head").applyTo(this.field_3398);
         set.get("body").applyTo(this.field_3391);
         set.get("left_arm").applyTo(this.field_27433);
         set.get("right_arm").applyTo(this.field_3401);
         set.get("left_leg").applyTo(this.field_3397);
         set.get("right_leg").applyTo(this.field_3392);
         set.get("scythe_handle").applyTo(this.scythe);
      }

      this.field_3394.method_17138(this.field_3398);
   }

   protected Iterable<class_630> method_22946() {
      return ImmutableList.of(this.field_3398, this.field_3394);
   }

   protected Iterable<class_630> method_22948() {
      return ImmutableList.of(this.field_3391, this.field_3401, this.field_27433, this.field_3392, this.field_3397);
   }
}
