package fabric.net.mca.mixin.client;

import fabric.net.mca.client.tts.AudioCache;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_4228;
import net.minecraft.class_4234;
import net.minecraft.class_4237;
import net.minecraft.class_4856;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_4237.class)
public class MixinSoundLoader {
   @Inject(method = "method_19744(Lnet/minecraft/class_2960;Z)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
   void mca$injectLoadStreamed(class_2960 id, boolean repeatInstantly, CallbackInfoReturnable<CompletableFuture<class_4234>> cir) {
      if (id.method_12832().startsWith("sounds/tts_cache/")) {
         cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
            String identifier = id.method_12832().substring(17, id.method_12832().length() - 4);
            if (identifier.endsWith(".ogg")) {
               try {
                  InputStream inputStream = new FileInputStream("tts_cache/" + identifier);
                  return (class_4234)(repeatInstantly ? new class_4856(class_4228::new, inputStream) : new class_4228(inputStream));
               } catch (IOException iOException) {
                  throw new CompletionException(iOException);
               }
            } else {
               return AudioCache.getPCMAudioStream(identifier);
            }
         }, class_156.method_18349()));
      }
   }
}
