package quilt.net.mca.mixin.client;

import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_811;
import net.minecraft.class_989;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quilt.net.mca.entity.VillagerLike;

@Mixin(class_989.class)
public abstract class MixinHeldItemFeatureRenderer {
   @Shadow
   protected abstract void method_4192(class_1309 var1, class_1799 var2, class_811 var3, class_1306 var4, class_4587 var5, class_4597 var6, int var7);

   @Inject(
      method = "method_17162(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_1309;FFFFFF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void render(
      class_4587 matrixStack,
      class_4597 vertexConsumerProvider,
      int i,
      class_1309 livingEntity,
      float f,
      float g,
      float h,
      float j,
      float k,
      float l,
      CallbackInfo ci
   ) {
      if (livingEntity instanceof VillagerLike) {
         boolean bl = livingEntity.method_6068() == class_1306.field_6183;
         class_1799 itemStack = bl ? livingEntity.method_6079() : livingEntity.method_6047();
         class_1799 itemStack2 = bl ? livingEntity.method_6047() : livingEntity.method_6079();
         if (!itemStack.method_7960() || !itemStack2.method_7960()) {
            matrixStack.method_22903();
            this.method_4192(livingEntity, itemStack2, class_811.field_4320, class_1306.field_6183, matrixStack, vertexConsumerProvider, i);
            this.method_4192(livingEntity, itemStack, class_811.field_4323, class_1306.field_6182, matrixStack, vertexConsumerProvider, i);
            matrixStack.method_22909();
         }

         ci.cancel();
      }
   }
}
