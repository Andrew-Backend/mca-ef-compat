package quilt.net.mca.mixin;

import net.minecraft.class_1269;
import net.minecraft.class_1786;
import net.minecraft.class_1838;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quilt.net.mca.server.world.data.VillageManager;

@Mixin(class_1786.class)
public class MixinFlintAndSteelItem {
   @Inject(method = "method_7884(Lnet/minecraft/class_1838;)Lnet/minecraft/class_1269;", at = @At("RETURN"))
   private void mca$onUseOnBlock(class_1838 context, CallbackInfoReturnable<class_1269> cir) {
      if (((class_1269)cir.getReturnValue()).method_23665() && context.method_8045() instanceof class_3218 serverWorld) {
         serverWorld.method_8503().execute(() -> VillageManager.get(serverWorld).getReaperSpawner().trySpawnReaper(serverWorld, context.method_8037()));
      }
   }
}
