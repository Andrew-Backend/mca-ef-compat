package quilt.net.mca.mixin;

import java.util.function.Supplier;
import net.minecraft.class_4148;
import net.minecraft.class_4149;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_4149.class)
public interface MixinSensorType {
   @Invoker("<init>")
   static <U extends class_4148<?>> class_4149<U> init(Supplier<U> supplier) {
      return null;
   }
}
