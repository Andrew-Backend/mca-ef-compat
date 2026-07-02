package fabric.net.mca.mixin.client;

import fabric.net.mca.MCAClient;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_583;
import net.minecraft.class_922;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_922.class)
public class MixinLivingEntityRenderer<T extends class_1309, M extends class_583<T>> {
   @Inject(method = "method_24302(Lnet/minecraft/class_1309;ZZZ)Lnet/minecraft/class_1921;", at = @At("HEAD"), cancellable = true)
   public void injectGetRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<@Nullable class_1921> cir) {
      if (entity instanceof class_1657 && MCAClient.useVillagerRenderer(entity.method_5667())) {
         cir.setReturnValue(null);
      }
   }
}
