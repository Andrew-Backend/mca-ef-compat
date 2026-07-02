package quilt.net.mca.mixin;

import net.minecraft.class_174;
import net.minecraft.class_179;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_174.class)
public interface MixinCriteria {
   @Invoker("method_767")
   static <T extends class_179<?>> T register(T object) {
      return null;
   }
}
