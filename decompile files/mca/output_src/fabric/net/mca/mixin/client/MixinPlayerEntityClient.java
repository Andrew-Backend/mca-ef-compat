package fabric.net.mca.mixin.client;

import fabric.net.mca.Config;
import fabric.net.mca.MCAClient;
import fabric.net.mca.entity.VillagerLike;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1657.class)
abstract class MixinPlayerEntityClient extends class_1309 {
   protected MixinPlayerEntityClient(class_1299<? extends class_1309> entityType, class_1937 world) {
      super(entityType, world);
   }

   @Inject(method = "method_18394(Lnet/minecraft/class_4050;Lnet/minecraft/class_4048;)F", at = @At("RETURN"), cancellable = true)
   public void mca$getActiveEyeHeight(class_4050 pose, class_4048 dimensions, CallbackInfoReturnable<Float> cir) {
      if (Config.getInstance().scaleEyeHeightWithPlayerHeight && !this.method_41328(class_4050.field_18078)) {
         MCAClient.getPlayerData(this.method_5667())
            .filter(data -> data.getPlayerModel() != VillagerLike.PlayerModel.VANILLA)
            .ifPresent(data -> cir.setReturnValue(Math.min(this.method_17682() - 0.0625F, (Float)cir.getReturnValue() * data.getRawScaleFactor())));
      }
   }
}
