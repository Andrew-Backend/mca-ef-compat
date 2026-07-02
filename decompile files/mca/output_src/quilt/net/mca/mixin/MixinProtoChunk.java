package quilt.net.mca.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_2791;
import net.minecraft.class_2839;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quilt.net.mca.server.SpawnQueue;

@Mixin(class_2839.class)
abstract class MixinProtoChunk extends class_2791 {
   MixinProtoChunk() {
      super(null, null, null, null, 0L, null, null);
   }

   @Inject(method = "method_12002(Lnet/minecraft/class_1297;)V", at = @At("HEAD"), cancellable = true)
   private void onAddEntity(class_1297 entity, CallbackInfo info) {
      if (SpawnQueue.getInstance().addVillager(entity)) {
         info.cancel();
      }
   }
}
