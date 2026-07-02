package forge.net.mca.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.GrimReaperEntity;
import forge.net.mca.entity.ReaperAttackState;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class GrimReaperEntityModel<T extends GrimReaperEntity> extends HumanoidModel<T> {
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
   private final ModelPart scythe;
   public ReaperAttackState reaperState = ReaperAttackState.IDLE;
   private final PartPose scytheTransform;

   public GrimReaperEntityModel(ModelPart tree) {
      super(tree);
      this.scythe = tree.m_171324_("left_arm").m_171324_("scythe_handle");
      this.scytheTransform = this.scythe.m_171308_();
   }

   public static MeshDefinition getModelData(CubeDeformation dilation) {
      MeshDefinition modelData = HumanoidModel.m_170681_(dilation, 0.0F);
      PartDefinition data = modelData.m_171576_();
      data.m_171597_("left_arm")
         .m_171599_(
            "scythe_handle",
            CubeListBuilder.m_171558_()
               .m_171514_(36, 32)
               .m_171488_(0.0F, -26.0F, 0.0F, 1.0F, 31.0F, 1.0F, dilation)
               .m_171514_(0, 32)
               .m_171488_(0.5F, -26.0F, 0.5F, 16.0F, 16.0F, 0.0F, dilation),
            ModelTransformSet.Builder.createTransform(0.0F, 10.0F, 0.0F, 90.0F, -20.0F, 90.0F)
         );
      return modelData;
   }

   public void setAngles(T entity, float f, float g, float h, float i, float j) {
      super.m_6973_(entity, f, g, h, i, j);
      this.f_102810_.m_104227_(0.0F, 0.0F, 0.0F);
      this.f_102810_.m_171327_(0.0F, 0.0F, 0.0F);
      this.f_102814_.m_104227_(1.9F, 12.0F, 0.0F);
      this.f_102814_.m_171327_(0.0F, 0.0F, 0.0F);
      this.f_102813_.m_104227_(-1.9F, 12.0F, 0.0F);
      this.f_102813_.m_171327_(0.0F, 0.0F, 0.0F);
      this.scythe.m_171322_(this.scytheTransform);
      this.reaperState = entity.getAttackState();
      ModelTransformSet set = POSES.get(this.reaperState);
      if (set != null) {
         set.get("head").applyTo(this.f_102808_);
         set.get("body").applyTo(this.f_102810_);
         set.get("left_arm").applyTo(this.f_102812_);
         set.get("right_arm").applyTo(this.f_102811_);
         set.get("left_leg").applyTo(this.f_102814_);
         set.get("right_leg").applyTo(this.f_102813_);
         set.get("scythe_handle").applyTo(this.scythe);
      }

      this.f_102809_.m_104315_(this.f_102808_);
   }

   protected Iterable<ModelPart> m_5607_() {
      return ImmutableList.of(this.f_102808_, this.f_102809_);
   }

   protected Iterable<ModelPart> m_5608_() {
      return ImmutableList.of(this.f_102810_, this.f_102811_, this.f_102812_, this.f_102813_, this.f_102814_);
   }
}
