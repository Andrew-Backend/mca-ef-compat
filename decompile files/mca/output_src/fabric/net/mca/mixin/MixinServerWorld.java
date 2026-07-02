package fabric.net.mca.mixin;

import fabric.net.mca.server.SpawnQueue;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_3218;
import net.minecraft.class_5281;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_3218.class)
abstract class MixinServerWorld extends class_1937 implements class_5281 {
   MixinServerWorld() {
      super(null, null, null, null, null, true, false, 0L, 0);
   }

   @Inject(method = "method_14175(Lnet/minecraft/class_1297;)Z", at = @At("HEAD"), cancellable = true)
   private void onAddEntity(class_1297 entity, CallbackInfoReturnable<Boolean> info) {
      if (SpawnQueue.getInstance().addVillager(entity)) {
         info.setReturnValue(false);
      }
   }
}
