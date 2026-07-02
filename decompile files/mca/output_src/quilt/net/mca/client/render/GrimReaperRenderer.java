package quilt.net.mca.client.render;

import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_909;
import net.minecraft.class_5617.class_5618;
import quilt.net.mca.MCA;
import quilt.net.mca.client.model.GrimReaperEntityModel;
import quilt.net.mca.entity.GrimReaperEntity;

public class GrimReaperRenderer extends class_909<GrimReaperEntity, GrimReaperEntityModel<GrimReaperEntity>> {
   private static final class_2960 TEXTURE = MCA.locate("textures/entity/grimreaper.png");

   public GrimReaperRenderer(class_5618 ctx) {
      super(ctx, new GrimReaperEntityModel(class_5607.method_32110(GrimReaperEntityModel.getModelData(class_5605.field_27715), 64, 64).method_32109()), 0.5F);
   }

   protected void scale(GrimReaperEntity reaper, class_4587 matrices, float tickDelta) {
      matrices.method_22905(1.3F, 1.3F, 1.3F);
   }

   public class_2960 getTexture(GrimReaperEntity reaper) {
      return TEXTURE;
   }
}
