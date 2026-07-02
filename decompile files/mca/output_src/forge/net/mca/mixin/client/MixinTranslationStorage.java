package forge.net.mca.mixin.client;

import forge.net.mca.MCA;
import forge.net.mca.entity.CommonSpeechManager;
import forge.net.mca.entity.ai.DialogueType;
import forge.net.mca.util.localization.PooledTranslationStorage;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.util.Tuple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientLanguage.class, priority = 990)
abstract class MixinTranslationStorage extends Language {
   @Shadow
   @Final
   private Map<String, String> f_118910_;
   @Unique
   private PooledTranslationStorage mca$pool;

   @Shadow
   public abstract String m_118919_(String var1, String var2);

   @Unique
   private PooledTranslationStorage mca$getPool() {
      if (this.mca$pool == null) {
         this.mca$pool = new PooledTranslationStorage(this.f_118910_);
         MCA.translations = this.f_118910_;
         MCA.language = Minecraft.m_91087_().f_91066_.f_92075_;
      }

      return this.mca$pool;
   }

   @Inject(method = "m_118919_", at = @At("HEAD"), cancellable = true)
   private void mca$onGet(String key, String fallback, CallbackInfoReturnable<String> info) {
      String modifiedKey = DialogueType.applyFallback(key);
      Tuple<String, String> unpooled = this.mca$getPool().get(modifiedKey);
      if (unpooled != null) {
         CommonSpeechManager.INSTANCE.lastResolvedKey = (String)unpooled.m_14418_();
         if (this.f_118910_.containsKey(unpooled.m_14418_()) && !this.f_118910_.get(unpooled.m_14418_()).equals(unpooled.m_14419_())) {
            info.setReturnValue((String)unpooled.m_14419_());
         } else {
            info.setReturnValue(this.m_118919_((String)unpooled.m_14418_(), fallback));
         }
      } else if (!key.equals(modifiedKey)) {
         info.setReturnValue(this.m_118919_(modifiedKey, fallback));
      }
   }

   @Inject(method = "m_6722_(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
   public void mca$onHasTranslation(String key, CallbackInfoReturnable<Boolean> info) {
      if (this.mca$getPool().contains(key)) {
         info.setReturnValue(true);
      }
   }
}
