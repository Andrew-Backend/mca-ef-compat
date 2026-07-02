package fabric.net.mca.mixin;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.class_4140;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_4140.class)
public interface MixinMemoryModuleType {
   @Invoker("<init>")
   static <U> class_4140<U> init(Optional<Codec<U>> codec) {
      return null;
   }
}
