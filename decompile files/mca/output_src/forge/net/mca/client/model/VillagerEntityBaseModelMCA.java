package forge.net.mca.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forge.net.mca.Config;
import forge.net.mca.entity.VillagerLike;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class VillagerEntityBaseModelMCA<T extends LivingEntity & VillagerLike<T>> extends HumanoidModel<T> implements CommonVillagerModel<T> {
   protected static final String BREASTS = "breasts";
   public final ModelPart breasts;
   final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
   float breastSize;

   public VillagerEntityBaseModelMCA(ModelPart root) {
      super(root);
      this.breasts = root.m_171324_("breasts");
   }

   public static MeshDefinition getModelData(CubeDeformation dilation) {
      MeshDefinition modelData = HumanoidModel.m_170681_(dilation, 0.0F);
      PartDefinition data = modelData.m_171576_();
      data.m_171599_("breasts", newBreasts(dilation, 0), PartPose.f_171404_);
      return modelData;
   }

   protected static CubeListBuilder newBreasts(CubeDeformation dilation, int oy) {
      CubeListBuilder builder = CubeListBuilder.m_171558_();
      if (Config.getInstance().enableBoobs) {
         builder.m_171514_(18, 21 + oy).m_171488_(-3.25F, -1.25F, -1.5F, 6.0F, 3.0F, 3.0F, dilation);
      }

      return builder;
   }

   protected Iterable<ModelPart> m_5607_() {
      return ImmutableList.of(this.f_102808_, this.f_102809_);
   }

   protected Iterable<ModelPart> m_5608_() {
      return ImmutableList.of(this.f_102810_, this.f_102811_, this.f_102812_, this.f_102813_, this.f_102814_);
   }

   public void m_6839_(T entity, float limbAngle, float limbDistance, float tickDelta) {
      super.m_6839_(entity, limbDistance, limbAngle, tickDelta);
      this.f_102609_ = this.f_102609_ | entity.getAgeState() == AgeState.BABY;
   }

   public void m_6973_(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      if (villager.getAgeState() == AgeState.BABY && !villager.m_20159_()) {
         limbDistance = (float)Math.sin(villager.f_19797_ / 12.0F);
         limbAngle = (float)Math.cos(villager.f_19797_ / 9.0F) * 3.0F;
         headYaw += (float)Math.sin(villager.f_19797_ / 2.0F);
      }

      if (villager.m_6162_()) {
         limbAngle /= 3.0F;
      }

      limbAngle /= 0.2F + villager.getRawScaleFactor();
      super.m_6973_(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      if (villager.getVillagerBrain().isPanicking()) {
         float toRadians = (float) (Math.PI / 180.0);
         float armRaise = ((float)Math.sin(animationProgress / 5.0F) * 30.0F - 180.0F + (float)Math.sin(animationProgress / 3.0F) * 3.0F) * toRadians;
         float waveSideways = ((float)Math.sin(animationProgress / 2.0F) * 12.0F - 17.0F) * toRadians;
         this.f_102812_.f_104203_ = armRaise;
         this.f_102812_.f_104205_ = -waveSideways;
         this.f_102811_.f_104203_ = -armRaise;
         this.f_102811_.f_104205_ = waveSideways;
      }

      this.applyVillagerDimensions(villager, villager.m_6047_());
   }

   public void m_102872_(HumanoidModel<T> target) {
      super.m_102872_(target);
      if (target instanceof VillagerEntityBaseModelMCA<T> m) {
         this.copyCommonAttributes(m);
         m.breasts.f_104207_ = this.breasts.f_104207_;
         m.breasts.m_104315_(this.breasts);
      }
   }

   public void m_7695_(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.renderCommon(matrices, vertices, light, overlay, red, green, blue, alpha);
   }

   @Override
   public ModelPart getBreastPart() {
      return this.breasts;
   }

   @Override
   public ModelPart getBodyPart() {
      return this.f_102810_;
   }

   @Override
   public Iterable<ModelPart> getCommonHeadParts() {
      return this.m_5607_();
   }

   @Override
   public Iterable<ModelPart> getCommonBodyParts() {
      return this.m_5608_();
   }

   @Override
   public Iterable<ModelPart> getBreastParts() {
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
