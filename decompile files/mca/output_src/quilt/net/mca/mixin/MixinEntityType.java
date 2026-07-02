package quilt.net.mca.mixin;

import net.minecraft.class_1299;
import net.minecraft.class_2960;
import net.minecraft.class_6862;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quilt.net.mca.Config;
import quilt.net.mca.util.RegistryHelper;

@Mixin(class_1299.class)
public class MixinEntityType {
   @Inject(method = "method_20210", at = @At("HEAD"), cancellable = true)
   private void mca$injectIsIn(class_6862<class_1299<?>> tag, CallbackInfoReturnable<Boolean> cir) {
      if (Config.getInstance().villagerTagsHacks && RegistryHelper.isObjectInTag(class_7923.field_41177, tag, class_1299.field_6077)) {
         class_2960 id = class_7923.field_41177.method_10221((class_1299)this);
         if (id != null && "mca".equals(id.method_12836()) && ("male_villager".equals(id.method_12832()) || "female_villager".equals(id.method_12832()))) {
            cir.setReturnValue(true);
         }
      }
   }
}
