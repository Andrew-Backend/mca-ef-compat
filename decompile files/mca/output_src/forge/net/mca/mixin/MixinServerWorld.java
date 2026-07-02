package forge.net.mca.mixin;

import forge.net.mca.server.SpawnQueue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
abstract class MixinServerWorld extends Level implements WorldGenLevel {
   MixinServerWorld() {
      super(null, null, null, null, null, true, false, 0L, 0);
   }

   @Inject(method = "m_8872_(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
   private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
      if (SpawnQueue.getInstance().addVillager(entity)) {
         info.setReturnValue(false);
      }
   }
}
