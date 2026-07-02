package forge.net.mca.mixin;

import forge.net.mca.Config;
import forge.net.mca.util.RegistryHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class MixinEntityType {
   @Inject(method = "m_204039_", at = @At("HEAD"), cancellable = true)
   private void mca$injectIsIn(TagKey<EntityType<?>> tag, CallbackInfoReturnable<Boolean> cir) {
      if (Config.getInstance().villagerTagsHacks && RegistryHelper.isObjectInTag(BuiltInRegistries.f_256780_, tag, EntityType.f_20492_)) {
         ResourceLocation id = BuiltInRegistries.f_256780_.m_7981_((EntityType)this);
         if (id != null && "mca".equals(id.m_135827_()) && ("male_villager".equals(id.m_135815_()) || "female_villager".equals(id.m_135815_()))) {
            cir.setReturnValue(true);
         }
      }
   }
}
