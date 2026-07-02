package fabric.net.mca.mixin.client;

import fabric.net.mca.MCAClient;
import fabric.net.mca.client.model.PlayerArmorExtendedModel;
import fabric.net.mca.client.model.VillagerEntityModelMCA;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_572;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_970.class)
public abstract class MixinArmorFeatureRenderer<T extends class_1309, A extends class_572<T>> {
   protected boolean mca$injectionActive;
   protected final A mca$leggingsModel = this.createModel(0.5F);
   protected final A mca$bodyModel = this.createModel(1.0F);

   @Shadow
   protected abstract boolean method_4173(class_1304 var1);

   private A createModel(float dilation) {
      return (A)(new PlayerArmorExtendedModel(class_5607.method_32110(VillagerEntityModelMCA.armorData(new class_5605(dilation)), 64, 32).method_32109()));
   }

   @Inject(method = "method_17157(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_1309;FFFFFF)V", at = @At("HEAD"))
   public void render(
      class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci
   ) {
      this.mca$injectionActive = livingEntity instanceof class_1657 && MCAClient.useGeneticsRenderer(livingEntity.method_5667());
   }

   @Inject(method = "method_4172", at = @At("HEAD"), cancellable = true)
   private void getArmor(class_1304 slot, CallbackInfoReturnable<A> cir) {
      if (this.mca$injectionActive) {
         cir.setReturnValue(this.method_4173(slot) ? this.mca$leggingsModel : this.mca$bodyModel);
      }
   }
}
