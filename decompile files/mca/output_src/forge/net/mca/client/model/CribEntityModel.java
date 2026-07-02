package forge.net.mca.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forge.net.mca.entity.CribEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class CribEntityModel<T extends CribEntity> extends EntityModel<T> {
   private final ModelPart CRIB;

   public CribEntityModel(ModelPart root) {
      this.CRIB = root.m_171324_("Crib");
   }

   public static MeshDefinition getModelData(CubeDeformation dilation) {
      MeshDefinition modelData = new MeshDefinition();
      PartDefinition data = modelData.m_171576_();
      PartDefinition crib = data.m_171599_(
         "Crib",
         CubeListBuilder.m_171558_().m_171514_(0, 0).m_171488_(-9.0F, -5.0F, -13.0F, 19.0F, 2.0F, 25.0F, dilation),
         PartPose.m_171419_(-0.5F, 6.0F, 0.5F)
      );
      crib.m_171599_(
         "Bars",
         CubeListBuilder.m_171558_()
            .m_171514_(0, 4)
            .m_171488_(9.0F, -16.0F, -11.5F, 0.0F, 11.0F, 23.0F, dilation)
            .m_171514_(46, 49)
            .m_171488_(-8.0F, -16.0F, 11.5F, 17.0F, 11.0F, 0.0F, dilation)
            .m_171514_(0, 4)
            .m_171488_(-8.0F, -16.0F, -11.5F, 0.0F, 11.0F, 23.0F, dilation)
            .m_171514_(46, 49)
            .m_171488_(-8.0F, -16.0F, -11.5F, 17.0F, 11.0F, 0.0F, dilation),
         PartPose.m_171419_(0.0F, 0.0F, -0.5F)
      );
      crib.m_171599_(
         "Frame",
         CubeListBuilder.m_171558_()
            .m_171514_(25, 27)
            .m_171488_(8.0F, -17.0F, -11.0F, 2.0F, 1.0F, 21.0F, dilation)
            .m_171514_(50, 30)
            .m_171488_(-7.0F, -17.0F, 10.0F, 15.0F, 1.0F, 2.0F, dilation)
            .m_171514_(50, 27)
            .m_171488_(-7.0F, -17.0F, -13.0F, 15.0F, 1.0F, 2.0F, dilation)
            .m_171514_(25, 27)
            .m_171480_()
            .m_171488_(-9.0F, -17.0F, -11.0F, 2.0F, 1.0F, 21.0F, dilation)
            .m_171555_(false),
         PartPose.m_171419_(0.0F, 0.0F, 0.0F)
      );
      crib.m_171599_(
         "Legs",
         CubeListBuilder.m_171558_()
            .m_171514_(0, 0)
            .m_171488_(8.0F, -17.0F, -13.0F, 2.0F, 12.0F, 2.0F, dilation)
            .m_171514_(0, 0)
            .m_171488_(8.0F, -17.0F, 10.0F, 2.0F, 12.0F, 2.0F, dilation)
            .m_171514_(9, 0)
            .m_171488_(8.0F, -3.0F, 10.0F, 2.0F, 3.0F, 2.0F, dilation)
            .m_171514_(9, 0)
            .m_171480_()
            .m_171488_(-9.0F, -3.0F, 10.0F, 2.0F, 3.0F, 2.0F, dilation)
            .m_171555_(false)
            .m_171514_(9, 0)
            .m_171488_(-9.0F, -3.0F, -13.0F, 2.0F, 3.0F, 2.0F, dilation)
            .m_171514_(9, 0)
            .m_171480_()
            .m_171488_(8.0F, -3.0F, -13.0F, 2.0F, 3.0F, 2.0F, dilation)
            .m_171555_(false)
            .m_171514_(0, 0)
            .m_171480_()
            .m_171488_(-9.0F, -17.0F, 10.0F, 2.0F, 12.0F, 2.0F, dilation)
            .m_171555_(false)
            .m_171514_(0, 0)
            .m_171480_()
            .m_171488_(-9.0F, -17.0F, -13.0F, 2.0F, 12.0F, 2.0F, dilation)
            .m_171555_(false),
         PartPose.m_171419_(0.0F, 0.0F, 0.0F)
      );
      return modelData;
   }

   public void m_7695_(PoseStack stack, VertexConsumer consumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
      this.CRIB.m_104306_(stack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
   }

   public void setAngles(T entity, float f, float g, float h, float i, float j) {
   }
}
