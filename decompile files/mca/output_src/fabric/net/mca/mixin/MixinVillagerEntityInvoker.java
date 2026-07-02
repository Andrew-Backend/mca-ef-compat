package fabric.net.mca.mixin;

import net.minecraft.class_1646;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_1646.class)
public interface MixinVillagerEntityInvoker {
   @Invoker("method_19191")
   void invokeBeginTradeWith(class_1657 var1);
}
