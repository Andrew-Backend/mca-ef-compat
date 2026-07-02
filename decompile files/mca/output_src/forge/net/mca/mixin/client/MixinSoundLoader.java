package forge.net.mca.mixin.client;

import com.mojang.blaze3d.audio.OggAudioStream;
import forge.net.mca.client.tts.AudioCache;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundBufferLibrary.class)
public class MixinSoundLoader {
   @Inject(method = "m_120204_(Lnet/minecraft/resources/ResourceLocation;Z)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
   void mca$injectLoadStreamed(ResourceLocation id, boolean repeatInstantly, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
      if (id.m_135815_().startsWith("sounds/tts_cache/")) {
         cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
            String identifier = id.m_135815_().substring(17, id.m_135815_().length() - 4);
            if (identifier.endsWith(".ogg")) {
               try {
                  InputStream inputStream = new FileInputStream("tts_cache/" + identifier);
                  return (AudioStream)(repeatInstantly ? new LoopingAudioStream(OggAudioStream::new, inputStream) : new OggAudioStream(inputStream));
               } catch (IOException iOException) {
                  throw new CompletionException(iOException);
               }
            } else {
               return AudioCache.getPCMAudioStream(identifier);
            }
         }, Util.m_183991_()));
      }
   }
}
