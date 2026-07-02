package quilt.net.mca.mixin;

import net.minecraft.class_2400;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_2400.class)
public interface MixinDefaultParticleType {
   @Invoker("<init>")
   static class_2400 init(boolean alwaysShow) {
      return null;
   }
}
