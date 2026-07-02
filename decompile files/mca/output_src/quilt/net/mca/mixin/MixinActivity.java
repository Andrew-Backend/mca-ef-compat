package quilt.net.mca.mixin;

import net.minecraft.class_4168;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_4168.class)
public interface MixinActivity {
   @Invoker("<init>")
   static class_4168 init(String string) {
      return null;
   }
}
