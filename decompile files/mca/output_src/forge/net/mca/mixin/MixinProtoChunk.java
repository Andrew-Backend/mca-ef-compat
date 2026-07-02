package forge.net.mca.mixin;

import forge.net.mca.server.SpawnQueue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProtoChunk.class)
abstract class MixinProtoChunk extends ChunkAccess {
   MixinProtoChunk() {
      super(null, null, null, null, 0L, null, null);
   }

   @Inject(method = "m_6286_(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void onAddEntity(Entity entity, CallbackInfo info) {
      if (SpawnQueue.getInstance().addVillager(entity)) {
         info.cancel();
      }
   }
}
