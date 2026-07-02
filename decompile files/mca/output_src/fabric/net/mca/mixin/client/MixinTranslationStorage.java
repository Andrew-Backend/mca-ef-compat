package fabric.net.mca.mixin.client;

import fabric.net.mca.MCA;
import fabric.net.mca.entity.CommonSpeechManager;
import fabric.net.mca.entity.ai.DialogueType;
import fabric.net.mca.util.localization.PooledTranslationStorage;
import java.util.Map;
import net.minecraft.class_1078;
import net.minecraft.class_2477;
import net.minecraft.class_310;
import net.minecraft.class_3545;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = class_1078.class, priority = 990)
abstract class MixinTranslationStorage extends class_2477 {
   @Shadow
   @Final
   private Map<String, String> field_5330;
   @Unique
   private PooledTranslationStorage mca$pool;

   @Shadow
   public abstract String method_4679(String var1, String var2);

   @Unique
   private PooledTranslationStorage mca$getPool() {
      if (this.mca$pool == null) {
         this.mca$pool = new PooledTranslationStorage(this.field_5330);
         MCA.translations = this.field_5330;
         MCA.language = class_310.method_1551().field_1690.field_1883;
      }

      return this.mca$pool;
   }

   @Inject(method = "method_4679", at = @At("HEAD"), cancellable = true)
   private void mca$onGet(String key, String fallback, CallbackInfoReturnable<String> info) {
      String modifiedKey = DialogueType.applyFallback(key);
      class_3545<String, String> unpooled = this.mca$getPool().get(modifiedKey);
      if (unpooled != null) {
         CommonSpeechManager.INSTANCE.lastResolvedKey = (String)unpooled.method_15442();
         if (this.field_5330.containsKey(unpooled.method_15442()) && !this.field_5330.get(unpooled.method_15442()).equals(unpooled.method_15441())) {
            info.setReturnValue((String)unpooled.method_15441());
         } else {
            info.setReturnValue(this.method_4679((String)unpooled.method_15442(), fallback));
         }
      } else if (!key.equals(modifiedKey)) {
         info.setReturnValue(this.method_4679(modifiedKey, fallback));
      }
   }

   @Inject(method = "method_4678(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
   public void mca$onHasTranslation(String key, CallbackInfoReturnable<Boolean> info) {
      if (this.mca$getPool().contains(key)) {
         info.setReturnValue(true);
      }
   }
}
