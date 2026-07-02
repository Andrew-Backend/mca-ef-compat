package quilt.net.mca.mixin;

import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1588;
import net.minecraft.class_1642;
import net.minecraft.class_1937;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quilt.net.mca.entity.VillagerEntityMCA;

@Mixin(class_1642.class)
public abstract class MixinZombieEntity extends class_1588 {
   protected MixinZombieEntity(class_1299<? extends class_1588> entityType, class_1937 world) {
      super(entityType, world);
   }

   @Inject(method = "method_5874(Lnet/minecraft/class_3218;Lnet/minecraft/class_1309;)Z", at = @At("HEAD"), cancellable = true)
   public void mca$onKilledOther(class_3218 world, class_1309 other, CallbackInfoReturnable<Boolean> cir) {
      if (other instanceof VillagerEntityMCA) {
         cir.setReturnValue(super.method_5874(world, other));
      }
   }
}
