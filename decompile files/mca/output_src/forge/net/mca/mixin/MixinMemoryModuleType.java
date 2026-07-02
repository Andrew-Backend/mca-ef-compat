package forge.net.mca.mixin;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MemoryModuleType.class)
public interface MixinMemoryModuleType {
   @Invoker("<init>")
   static <U> MemoryModuleType<U> init(Optional<Codec<U>> codec) {
      return null;
   }
}
