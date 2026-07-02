package forge.net.mca.mixin;

import forge.net.mca.item.BabyItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
abstract class MixinScreenHandler {
   @Shadow
   @Final
   public NonNullList<Slot> f_38839_;

   @Shadow
   public abstract ItemStack m_142621_();

   @Inject(method = "m_150399_", at = @At("HEAD"), cancellable = true)
   private void onSlotClick(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo info) {
      ItemStack stack = this.mca$getDroppedStack(slotIndex, actionType, player);
      if (BabyItem.shouldCancelDrop(stack, player)) {
         info.cancel();
      }
   }

   @Unique
   private ItemStack mca$getDroppedStack(int slotIndex, ClickType actionType, Player player) {
      if (slotIndex == -999 && actionType == ClickType.PICKUP) {
         return this.m_142621_();
      }

      if (slotIndex >= 0 && slotIndex < this.f_38839_.size() && actionType == ClickType.THROW) {
         Slot slot = (Slot)this.f_38839_.get(slotIndex);
         if (slot.m_8010_(player)) {
            return slot.m_7993_();
         }
      }

      return ItemStack.f_41583_;
   }
}
