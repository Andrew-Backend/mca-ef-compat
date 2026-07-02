package fabric.net.mca.mixin;

import fabric.net.mca.item.BabyItem;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2371;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1703.class)
abstract class MixinScreenHandler {
   @Shadow
   @Final
   public class_2371<class_1735> field_7761;

   @Shadow
   public abstract class_1799 method_34255();

   @Inject(method = "method_7593", at = @At("HEAD"), cancellable = true)
   private void onSlotClick(int slotIndex, int button, class_1713 actionType, class_1657 player, CallbackInfo info) {
      class_1799 stack = this.mca$getDroppedStack(slotIndex, actionType, player);
      if (BabyItem.shouldCancelDrop(stack, player)) {
         info.cancel();
      }
   }

   @Unique
   private class_1799 mca$getDroppedStack(int slotIndex, class_1713 actionType, class_1657 player) {
      if (slotIndex == -999 && actionType == class_1713.field_7790) {
         return this.method_34255();
      }

      if (slotIndex >= 0 && slotIndex < this.field_7761.size() && actionType == class_1713.field_7795) {
         class_1735 slot = (class_1735)this.field_7761.get(slotIndex);
         if (slot.method_7674(player)) {
            return slot.method_7677();
         }
      }

      return class_1799.field_8037;
   }
}
