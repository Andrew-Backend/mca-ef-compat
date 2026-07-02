package quilt.net.mca.client.render;

import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5617.class_5618;
import quilt.net.mca.client.model.VillagerEntityModelMCA;
import quilt.net.mca.client.render.layer.ClothingLayer;
import quilt.net.mca.client.render.layer.FaceLayer;
import quilt.net.mca.client.render.layer.HairLayer;
import quilt.net.mca.client.render.layer.SkinLayer;
import quilt.net.mca.entity.VillagerEntityMCA;

public class VillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<VillagerEntityMCA> {
   public VillagerEntityMCARenderer(class_5618 ctx) {
      super(ctx, createModel(VillagerEntityModelMCA.bodyData(class_5605.field_27715)).hideWears());
      this.method_4046(new SkinLayer(this, (VillagerEntityModelMCA)this.field_4737));
      this.method_4046(new FaceLayer(this, createModel(VillagerEntityModelMCA.bodyData(new class_5605(0.01F))).hideWears(), "normal"));
      this.method_4046(new ClothingLayer(this, createModel(VillagerEntityModelMCA.bodyData(new class_5605(0.0625F))), "normal"));
      this.method_4046(new HairLayer(this, createModel(VillagerEntityModelMCA.hairData(new class_5605(0.125F)))));
   }

   private static VillagerEntityModelMCA<VillagerEntityMCA> createModel(class_5609 data) {
      return new VillagerEntityModelMCA(class_5607.method_32110(data, 64, 64).method_32109());
   }
}
