package fabric.net.mca.client.render;

import fabric.net.mca.client.model.VillagerEntityModelMCA;
import fabric.net.mca.client.model.ZombieVillagerEntityModelMCA;
import fabric.net.mca.client.render.layer.ClothingLayer;
import fabric.net.mca.client.render.layer.FaceLayer;
import fabric.net.mca.client.render.layer.HairLayer;
import fabric.net.mca.client.render.layer.SkinLayer;
import fabric.net.mca.entity.ZombieVillagerEntityMCA;
import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5617.class_5618;

public class ZombieVillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<ZombieVillagerEntityMCA> {
   public ZombieVillagerEntityMCARenderer(class_5618 ctx) {
      super(ctx, createModel(VillagerEntityModelMCA.bodyData(class_5605.field_27715)).hideWears());
      this.method_4046(new SkinLayer(this, (VillagerEntityModelMCA)this.field_4737));
      this.method_4046(new FaceLayer(this, createModel(VillagerEntityModelMCA.bodyData(new class_5605(0.01F))).hideWears(), "zombie"));
      this.method_4046(new ClothingLayer(this, createModel(VillagerEntityModelMCA.bodyData(new class_5605(0.075F))), "zombie"));
      this.method_4046(new HairLayer(this, createModel(VillagerEntityModelMCA.hairData(new class_5605(0.1F)))));
   }

   private static VillagerEntityModelMCA<ZombieVillagerEntityMCA> createModel(class_5609 data) {
      return new ZombieVillagerEntityModelMCA(class_5607.method_32110(data, 64, 64).method_32109());
   }

   protected boolean isShaking(ZombieVillagerEntityMCA entity) {
      return entity.method_7198() || entity.method_7206();
   }
}
