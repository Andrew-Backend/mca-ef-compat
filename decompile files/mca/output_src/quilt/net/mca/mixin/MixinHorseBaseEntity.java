package quilt.net.mca.mixin;

import net.minecraft.class_1265;
import net.minecraft.class_1309;
import net.minecraft.class_1316;
import net.minecraft.class_1429;
import net.minecraft.class_1496;
import net.minecraft.class_5146;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quilt.net.mca.entity.VillagerEntityMCA;

@Mixin(class_1496.class)
abstract class MixinHorseBaseEntity extends class_1429 implements class_1265, class_1316, class_5146 {
   @Shadow
   @Nullable
   public abstract class_1309 method_5642();

   MixinHorseBaseEntity() {
      super(null, null);
   }

   @Inject(method = "method_6062()Z", at = @At("HEAD"), cancellable = true)
   private void onIsImmobile(CallbackInfoReturnable<Boolean> info) {
      if (this.method_5642() instanceof VillagerEntityMCA) {
         info.setReturnValue(false);
      }
   }
}
