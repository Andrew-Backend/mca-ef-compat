package yesman.epicfight.mixin.skinlayers;

import dev.tr7zw.skinlayers.versionless.render.CustomModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CustomModelPart.class, remap = false)
public interface MixinCustomModelPart {
   @Accessor(value = "x", remap = false)
   float getX();

   @Accessor(value = "y", remap = false)
   float getY();

   @Accessor(value = "z", remap = false)
   float getZ();

   @Accessor(value = "xRot", remap = false)
   float getXRot();

   @Accessor(value = "yRot", remap = false)
   float getYRot();

   @Accessor(value = "zRot", remap = false)
   float getZRot();
}
