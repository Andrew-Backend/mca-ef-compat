package quilt.net.mca.mixin.client;

import net.minecraft.class_1007;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import net.minecraft.class_922;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quilt.net.mca.MCAClient;
import quilt.net.mca.client.model.CommonVillagerModel;
import quilt.net.mca.client.model.PlayerEntityExtendedModel;
import quilt.net.mca.client.model.VillagerEntityModelMCA;
import quilt.net.mca.client.render.layer.ClothingLayer;
import quilt.net.mca.client.render.layer.FaceLayer;
import quilt.net.mca.client.render.layer.HairLayer;
import quilt.net.mca.client.render.layer.SkinLayer;
import quilt.net.mca.client.render.layer.VillagerLayer;
import quilt.net.mca.entity.ai.relationship.AgeState;

@Mixin(class_1007.class)
public abstract class MixinPlayerEntityRenderer extends class_922<class_742, class_591<class_742>> {
   @Unique
   private class_591<class_742> mca$villagerModel;
   @Unique
   private class_591<class_742> mca$vanillaModel;
   @Unique
   SkinLayer<class_742, class_591<class_742>> mca$skinLayer;
   @Unique
   ClothingLayer<class_742, class_591<class_742>> mca$clothingLayer;

   @Shadow
   protected abstract void method_4218(class_742 var1);

   public MixinPlayerEntityRenderer(class_5618 ctx, class_591<class_742> model, float shadowRadius) {
      super(ctx, model, shadowRadius);
   }

   @Inject(method = "<init>(Lnet/minecraft/class_5617$class_5618;Z)V", at = @At("TAIL"))
   private void init(class_5618 ctx, boolean slim, CallbackInfo ci) {
      if (MCAClient.isPlayerRendererAllowed()) {
         this.mca$villagerModel = mca$createModel(VillagerEntityModelMCA.bodyData(new class_5605(0.0F), slim));
         this.mca$vanillaModel = (class_591<class_742>)this.field_4737;
         this.mca$skinLayer = new SkinLayer(this, mca$createModel(VillagerEntityModelMCA.bodyData(new class_5605(0.0F))));
         this.method_4046(this.mca$skinLayer);
         this.method_4046(new FaceLayer(this, mca$createModel(VillagerEntityModelMCA.bodyData(new class_5605(0.01F))), "normal"));
         this.mca$clothingLayer = new ClothingLayer(this, mca$createModel(VillagerEntityModelMCA.bodyData(new class_5605(0.0625F))), "normal");
         this.method_4046(this.mca$clothingLayer);
         this.method_4046(new HairLayer(this, mca$createModel(VillagerEntityModelMCA.hairData(new class_5605(0.125F)))));
      }
   }

   @Unique
   private static PlayerEntityExtendedModel<class_742> mca$createModel(class_5609 data) {
      return new PlayerEntityExtendedModel(class_5607.method_32110(data, 64, 64).method_32109());
   }

   @Inject(method = "method_4217(Lnet/minecraft/class_742;Lnet/minecraft/class_4587;F)V", at = @At("TAIL"), cancellable = true)
   private void injectScale(class_742 player, class_4587 matrices, float f, CallbackInfo ci) {
      if (MCAClient.useGeneticsRenderer(player.method_5667())) {
         float height = CommonVillagerModel.getVillager(player).getRawScaleFactor();
         float width = CommonVillagerModel.getVillager(player).getHorizontalScaleFactor();
         matrices.method_22905(width, height, width);
         if (CommonVillagerModel.getVillager(player).getAgeState() == AgeState.BABY && !player.method_5765()) {
            matrices.method_46416(0.0F, 0.6F, 0.0F);
         }

         ci.cancel();
         this.field_4737 = this.mca$villagerModel;
      } else if (MCAClient.isPlayerRendererAllowed()) {
         this.field_4737 = this.mca$vanillaModel;
      }
   }

   @Inject(method = "method_4220(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;)V", at = @At("HEAD"), cancellable = true)
   public void injectRenderRightArm(class_4587 matrices, class_4597 vertexConsumers, int light, class_742 player, CallbackInfo ci) {
      if (MCAClient.renderArms(player.method_5667(), "right_arm")) {
         this.mca$renderCustomArm(
            matrices, vertexConsumers, light, player, this.mca$skinLayer.model.field_3401, this.mca$skinLayer.model.field_3486, this.mca$skinLayer
         );
         this.mca$renderCustomArm(
            matrices, vertexConsumers, light, player, this.mca$clothingLayer.model.field_3401, this.mca$clothingLayer.model.field_3486, this.mca$clothingLayer
         );
         ci.cancel();
      }
   }

   @Inject(method = "method_4221(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;)V", at = @At("HEAD"), cancellable = true)
   public void injectRenderLeftArm(class_4587 matrices, class_4597 vertexConsumers, int light, class_742 player, CallbackInfo ci) {
      if (MCAClient.renderArms(player.method_5667(), "left_arm")) {
         this.mca$renderCustomArm(
            matrices, vertexConsumers, light, player, this.mca$skinLayer.model.field_27433, this.mca$skinLayer.model.field_3484, this.mca$skinLayer
         );
         this.mca$renderCustomArm(
            matrices, vertexConsumers, light, player, this.mca$clothingLayer.model.field_27433, this.mca$clothingLayer.model.field_3484, this.mca$clothingLayer
         );
         ci.cancel();
      }
   }

   @Unique
   private void mca$renderCustomArm(
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light,
      class_742 player,
      class_630 arm,
      class_630 sleeve,
      VillagerLayer<class_742, class_591<class_742>> layer
   ) {
      PlayerEntityExtendedModel<class_742> model = (PlayerEntityExtendedModel<class_742>)layer.model;
      this.method_4218(player);
      model.field_3447 = 0.0F;
      model.field_3400 = false;
      model.field_3396 = 0.0F;
      model.method_17087(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      model.applyVillagerDimensions(CommonVillagerModel.getVillager(player), player.method_18276());
      class_2960 skin = layer.getSkin(player);
      if (layer.canUse(skin)) {
         class_4588 buffer = vertexConsumers.getBuffer(class_1921.method_23578(skin));
         float[] color = layer.getColor(player, 0.0F);
         arm.field_3654 = 0.0F;
         arm.method_22699(matrices, buffer, light, class_4608.field_21444, color[0], color[1], color[2], 1.0F);
         sleeve.field_3654 = 0.0F;
         sleeve.method_22699(matrices, buffer, light, class_4608.field_21444, color[0], color[1], color[2], 1.0F);
      }
   }
}
