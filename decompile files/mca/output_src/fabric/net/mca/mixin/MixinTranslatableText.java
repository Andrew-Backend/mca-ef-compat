package fabric.net.mca.mixin;

import fabric.net.mca.entity.CommonSpeechManager;
import net.minecraft.class_2588;
import net.minecraft.class_7417;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2588.class)
public class MixinTranslatableText {
   @Inject(method = "method_11025()V", at = @At("TAIL"))
   private void mca$updateTranslations(CallbackInfo ci) {
      if (CommonSpeechManager.INSTANCE.lastResolvedKey != null) {
         CommonSpeechManager.INSTANCE.translations.put((class_7417)this, CommonSpeechManager.INSTANCE.lastResolvedKey);
      }
   }
}
