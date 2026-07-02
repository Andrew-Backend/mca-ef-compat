package forge.net.mca.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forge.net.mca.entity.ai.relationship.AgeState;
import forge.net.mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class PlayerEntityExtendedModel<T extends LivingEntity> extends PlayerModel<T> implements CommonVillagerModel<T> {
   public final ModelPart breasts;
   public final ModelPart breastsWear;
   final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
   float breastSize;

   public PlayerEntityExtendedModel(ModelPart root) {
      super(root, false);
      this.breasts = root.m_171324_("breasts");
      this.breastsWear = root.m_171324_("breastplate");
   }

   public void m_102872_(HumanoidModel<T> target) {
      super.m_102872_(target);
      if (target instanceof PlayerEntityExtendedModel<T> playerTarget) {
         this.copyAttributes(playerTarget);
      }

      if (target instanceof PlayerArmorExtendedModel<T> armorTarget) {
         this.copyAttributes(armorTarget);
      }
   }

   private void copyAttributes(PlayerEntityExtendedModel<T> target) {
      target.f_103376_.m_104315_(this.f_103376_);
      target.f_103377_.m_104315_(this.f_103377_);
      target.f_103374_.m_104315_(this.f_103374_);
      target.f_103375_.m_104315_(this.f_103375_);
      target.f_103378_.m_104315_(this.f_103378_);
      target.breastsWear.m_104315_(this.breastsWear);
      this.copyCommonAttributes(target);
      target.breasts.f_104207_ = this.breasts.f_104207_;
      target.breasts.m_104315_(this.breasts);
   }

   private void copyAttributes(PlayerArmorExtendedModel<T> target) {
      this.copyCommonAttributes(target);
      target.breasts.f_104207_ = this.breasts.f_104207_;
      target.breasts.m_104315_(this.breasts);
   }

   public void m_7695_(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.breastsWear.f_104207_ = this.f_103378_.f_104207_;
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
      return ImmutableList.of(this.breasts, this.breastsWear);
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

   public void m_6973_(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      if (CommonVillagerModel.getVillager(villager).getAgeState() == AgeState.BABY && !villager.m_20159_()) {
         limbDistance = (float)Math.sin(villager.f_19797_ / 12.0F);
         limbAngle = (float)Math.cos(villager.f_19797_ / 9.0F) * 3.0F;
         headYaw += (float)Math.sin(villager.f_19797_ / 2.0F);
      }

      super.m_6973_(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
      this.applyVillagerDimensions(CommonVillagerModel.getVillager(villager), villager.m_6047_());
   }

   public <M extends HumanoidModel<T>> void copyVisibility(M model) {
      this.f_102808_.f_104207_ = model.f_102808_.f_104207_;
      this.f_102809_.f_104207_ = model.f_102808_.f_104207_;
      this.f_102810_.f_104207_ = model.f_102810_.f_104207_;
      this.f_103378_.f_104207_ = model.f_102810_.f_104207_;
      this.breasts.f_104207_ = model.f_102810_.f_104207_;
      this.breastsWear.f_104207_ = model.f_102810_.f_104207_;
      this.f_102812_.f_104207_ = model.f_102812_.f_104207_;
      this.f_103374_.f_104207_ = model.f_102812_.f_104207_;
      this.f_102811_.f_104207_ = model.f_102811_.f_104207_;
      this.f_103375_.f_104207_ = model.f_102811_.f_104207_;
      this.f_102814_.f_104207_ = model.f_102814_.f_104207_;
      this.f_103376_.f_104207_ = model.f_102814_.f_104207_;
      this.f_102813_.f_104207_ = model.f_102813_.f_104207_;
      this.f_103377_.f_104207_ = model.f_102813_.f_104207_;
   }
}
