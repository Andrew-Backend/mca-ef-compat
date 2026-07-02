package quilt.net.mca.mixin;

import net.minecraft.class_1263;
import net.minecraft.class_1275;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quilt.net.mca.item.BabyItem;

@Mixin(class_1661.class)
abstract class MixinPlayerInventory implements class_1263, class_1275 {
   @Shadow
   @Final
   public class_1657 field_7546;

   @Inject(method = "method_37417(Z)Lnet/minecraft/class_1799;", at = @At("HEAD"), cancellable = true)
   public void onDropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<class_1799> info) {
      class_1799 stack = ((class_1661)this).method_7391();
      if (BabyItem.shouldCancelDrop(stack, this.field_7546)) {
         info.setReturnValue(class_1799.field_8037);
      }
   }
}
