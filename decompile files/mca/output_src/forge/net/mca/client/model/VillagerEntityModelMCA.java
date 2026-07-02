package forge.net.mca.client.model;

import com.google.common.collect.ImmutableList;
import forge.net.mca.entity.VillagerLike;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class VillagerEntityModelMCA<T extends LivingEntity & VillagerLike<T>> extends VillagerEntityBaseModelMCA<T> {
   protected static final String BREASTPLATE = "breastplate";
   public final ModelPart breastsWear;
   public final ModelPart leftArmwear;
   public final ModelPart rightArmwear;
   public final ModelPart leftLegwear;
   public final ModelPart rightLegwear;
   public final ModelPart bodyWear;
   private boolean wearsHidden;

   public VillagerEntityModelMCA(ModelPart tree) {
      super(tree);
      this.bodyWear = tree.m_171324_("jacket");
      this.leftArmwear = tree.m_171324_("left_sleeve");
      this.rightArmwear = tree.m_171324_("right_sleeve");
      this.leftLegwear = tree.m_171324_("left_pants");
      this.rightLegwear = tree.m_171324_("right_pants");
      this.breastsWear = tree.m_171324_("breastplate");
   }

   public static MeshDefinition hairData(CubeDeformation dilation) {
      MeshDefinition modelData = bodyData(dilation);
      PartDefinition root = modelData.m_171576_();
      root.m_171599_(
         "hat", CubeListBuilder.m_171558_().m_171514_(32, 0).m_171488_(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, dilation.m_171469_(0.3F)), PartPose.f_171404_
      );
      return modelData;
   }

   public static MeshDefinition bodyData(CubeDeformation dilation) {
      return bodyData(dilation, false);
   }

   public static MeshDefinition bodyData(CubeDeformation dilation, boolean slim) {
      MeshDefinition modelData = PlayerModel.m_170825_(dilation, slim);
      PartDefinition root = modelData.m_171576_();
      root.m_171599_("breasts", newBreasts(dilation, 0), PartPose.f_171404_);
      root.m_171599_("breastplate", newBreasts(dilation.m_171469_(0.1F), 16), PartPose.f_171404_);
      return modelData;
   }

   public static MeshDefinition armorData(CubeDeformation dilation) {
      MeshDefinition modelData = HumanoidModel.m_170681_(dilation, 0.0F);
      PartDefinition root = modelData.m_171576_();
      root.m_171599_("breasts", newBreasts(dilation, 0), PartPose.f_171404_);
      return modelData;
   }

   @Override
   protected Iterable<ModelPart> m_5608_() {
      return ImmutableList.of(
         this.f_102810_,
         this.f_102811_,
         this.f_102812_,
         this.f_102813_,
         this.f_102814_,
         this.bodyWear,
         this.leftLegwear,
         this.rightLegwear,
         this.leftArmwear,
         this.rightArmwear
      );
   }

   @Override
   public Iterable<ModelPart> getBreastParts() {
      return ImmutableList.of(this.breasts, this.breastsWear);
   }

   @Override
   public void m_6973_(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      super.m_6973_(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      this.leftLegwear.m_104315_(this.f_102814_);
      this.rightLegwear.m_104315_(this.f_102813_);
      this.leftArmwear.m_104315_(this.f_102812_);
      this.rightArmwear.m_104315_(this.f_102811_);
      this.bodyWear.m_104315_(this.f_102810_);
      this.breastsWear.m_104315_(this.breasts);
   }

   public void m_8009_(boolean visible) {
      super.m_8009_(visible);
      this.leftArmwear.f_104207_ = !this.wearsHidden && visible;
      this.rightArmwear.f_104207_ = !this.wearsHidden && visible;
      this.leftLegwear.f_104207_ = !this.wearsHidden && visible;
      this.rightLegwear.f_104207_ = !this.wearsHidden && visible;
      this.bodyWear.f_104207_ = !this.wearsHidden && visible;
   }

   public VillagerEntityModelMCA<T> hideWears() {
      this.wearsHidden = true;
      this.breastsWear.f_104207_ = false;
      this.leftArmwear.f_104207_ = false;
      this.rightArmwear.f_104207_ = false;
      this.leftLegwear.f_104207_ = false;
      this.rightLegwear.f_104207_ = false;
      this.bodyWear.f_104207_ = false;
      return this;
   }

   @Override
   public void m_102872_(HumanoidModel<T> target) {
      super.m_102872_(target);
      if (target instanceof VillagerEntityModelMCA) {
         this.copyAttributes((VillagerEntityModelMCA<T>)target);
      }
   }

   private void copyAttributes(VillagerEntityModelMCA<T> target) {
      target.leftLegwear.m_104315_(this.leftLegwear);
      target.rightLegwear.m_104315_(this.rightLegwear);
      target.leftArmwear.m_104315_(this.leftArmwear);
      target.rightArmwear.m_104315_(this.rightArmwear);
      target.bodyWear.m_104315_(this.bodyWear);
      target.breastsWear.m_104315_(this.breastsWear);
   }

   public <M extends HumanoidModel<T>> void copyVisibility(M model) {
      this.f_102808_.f_104207_ = model.f_102808_.f_104207_;
      this.f_102809_.f_104207_ = model.f_102808_.f_104207_;
      this.f_102810_.f_104207_ = model.f_102810_.f_104207_;
      this.bodyWear.f_104207_ = model.f_102810_.f_104207_;
      this.breasts.f_104207_ = model.f_102810_.f_104207_;
      this.breastsWear.f_104207_ = model.f_102810_.f_104207_;
      this.f_102812_.f_104207_ = model.f_102812_.f_104207_;
      this.leftArmwear.f_104207_ = model.f_102812_.f_104207_;
      this.f_102811_.f_104207_ = model.f_102811_.f_104207_;
      this.rightArmwear.f_104207_ = model.f_102811_.f_104207_;
      this.f_102814_.f_104207_ = model.f_102814_.f_104207_;
      this.leftLegwear.f_104207_ = model.f_102814_.f_104207_;
      this.f_102813_.f_104207_ = model.f_102813_.f_104207_;
      this.rightLegwear.f_104207_ = model.f_102813_.f_104207_;
   }
}
