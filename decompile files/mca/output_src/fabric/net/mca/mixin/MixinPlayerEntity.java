package fabric.net.mca.mixin;

import fabric.net.mca.item.BabyItem;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1657.class)
abstract class MixinPlayerEntity extends class_1309 {
   private MixinPlayerEntity() {
      super(null, null);
   }

   @Inject(method = "method_7329(Lnet/minecraft/class_1799;ZZ)Lnet/minecraft/class_1542;", at = @At("HEAD"), cancellable = true)
   private void onDropItem(class_1799 stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<class_1542> info) {
      if (BabyItem.shouldCancelDrop(stack, (class_1657)this)) {
         info.setReturnValue(null);
      }
   }
}
