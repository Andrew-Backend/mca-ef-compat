package quilt.net.mca.client.model;

import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_583;
import net.minecraft.class_630;
import quilt.net.mca.entity.CribEntity;

public class CribEntityModel<T extends CribEntity> extends class_583<T> {
   private final class_630 CRIB;

   public CribEntityModel(class_630 root) {
      this.CRIB = root.method_32086("Crib");
   }

   public static class_5609 getModelData(class_5605 dilation) {
      class_5609 modelData = new class_5609();
      class_5610 data = modelData.method_32111();
      class_5610 crib = data.method_32117(
         "Crib",
         class_5606.method_32108().method_32101(0, 0).method_32098(-9.0F, -5.0F, -13.0F, 19.0F, 2.0F, 25.0F, dilation),
         class_5603.method_32090(-0.5F, 6.0F, 0.5F)
      );
      crib.method_32117(
         "Bars",
         class_5606.method_32108()
            .method_32101(0, 4)
            .method_32098(9.0F, -16.0F, -11.5F, 0.0F, 11.0F, 23.0F, dilation)
            .method_32101(46, 49)
            .method_32098(-8.0F, -16.0F, 11.5F, 17.0F, 11.0F, 0.0F, dilation)
            .method_32101(0, 4)
            .method_32098(-8.0F, -16.0F, -11.5F, 0.0F, 11.0F, 23.0F, dilation)
            .method_32101(46, 49)
            .method_32098(-8.0F, -16.0F, -11.5F, 17.0F, 11.0F, 0.0F, dilation),
         class_5603.method_32090(0.0F, 0.0F, -0.5F)
      );
      crib.method_32117(
         "Frame",
         class_5606.method_32108()
            .method_32101(25, 27)
            .method_32098(8.0F, -17.0F, -11.0F, 2.0F, 1.0F, 21.0F, dilation)
            .method_32101(50, 30)
            .method_32098(-7.0F, -17.0F, 10.0F, 15.0F, 1.0F, 2.0F, dilation)
            .method_32101(50, 27)
            .method_32098(-7.0F, -17.0F, -13.0F, 15.0F, 1.0F, 2.0F, dilation)
            .method_32101(25, 27)
            .method_32096()
            .method_32098(-9.0F, -17.0F, -11.0F, 2.0F, 1.0F, 21.0F, dilation)
            .method_32106(false),
         class_5603.method_32090(0.0F, 0.0F, 0.0F)
      );
      crib.method_32117(
         "Legs",
         class_5606.method_32108()
            .method_32101(0, 0)
            .method_32098(8.0F, -17.0F, -13.0F, 2.0F, 12.0F, 2.0F, dilation)
            .method_32101(0, 0)
            .method_32098(8.0F, -17.0F, 10.0F, 2.0F, 12.0F, 2.0F, dilation)
            .method_32101(9, 0)
            .method_32098(8.0F, -3.0F, 10.0F, 2.0F, 3.0F, 2.0F, dilation)
            .method_32101(9, 0)
            .method_32096()
            .method_32098(-9.0F, -3.0F, 10.0F, 2.0F, 3.0F, 2.0F, dilation)
            .method_32106(false)
            .method_32101(9, 0)
            .method_32098(-9.0F, -3.0F, -13.0F, 2.0F, 3.0F, 2.0F, dilation)
            .method_32101(9, 0)
            .method_32096()
            .method_32098(8.0F, -3.0F, -13.0F, 2.0F, 3.0F, 2.0F, dilation)
            .method_32106(false)
            .method_32101(0, 0)
            .method_32096()
            .method_32098(-9.0F, -17.0F, 10.0F, 2.0F, 12.0F, 2.0F, dilation)
            .method_32106(false)
            .method_32101(0, 0)
            .method_32096()
            .method_32098(-9.0F, -17.0F, -13.0F, 2.0F, 12.0F, 2.0F, dilation)
            .method_32106(false),
         class_5603.method_32090(0.0F, 0.0F, 0.0F)
      );
      return modelData;
   }

   public void method_2828(class_4587 stack, class_4588 consumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.CRIB.method_22699(stack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   public void setAngles(T entity, float f, float g, float h, float i, float j) {
   }
}
