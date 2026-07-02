package forge.net.mca.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.MCAClient;
import forge.net.mca.client.model.PlayerArmorExtendedModel;
import forge.net.mca.client.model.VillagerEntityModelMCA;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HumanoidArmorLayer.class)
public abstract class MixinArmorFeatureRenderer<T extends LivingEntity, A extends HumanoidModel<T>> {
   protected boolean mca$injectionActive;
   protected final A mca$leggingsModel = this.createModel(0.5F);
   protected final A mca$bodyModel = this.createModel(1.0F);

   @Shadow
   protected abstract boolean m_117128_(EquipmentSlot var1);

   private A createModel(float dilation) {
      return (A)(new PlayerArmorExtendedModel(LayerDefinition.m_171565_(VillagerEntityModelMCA.armorData(new CubeDeformation(dilation)), 64, 32).m_171564_()));
   }

   @Inject(
      method = "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
      at = @At("HEAD")
   )
   public void render(
      PoseStack matrixStack,
      MultiBufferSource vertexConsumerProvider,
      int i,
      T livingEntity,
      float f,
      float g,
      float h,
      float j,
      float k,
      float l,
      CallbackInfo ci
   ) {
      this.mca$injectionActive = livingEntity instanceof Player && MCAClient.useGeneticsRenderer(livingEntity.m_20148_());
   }

   @Inject(method = "m_117078_", at = @At("HEAD"), cancellable = true)
   private void getArmor(EquipmentSlot slot, CallbackInfoReturnable<A> cir) {
      if (this.mca$injectionActive) {
         cir.setReturnValue(this.m_117128_(slot) ? this.mca$leggingsModel : this.mca$bodyModel);
      }
   }
}
