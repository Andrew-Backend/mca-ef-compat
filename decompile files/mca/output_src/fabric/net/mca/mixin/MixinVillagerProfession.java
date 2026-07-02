package fabric.net.mca.mixin;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import net.minecraft.class_1792;
import net.minecraft.class_2248;
import net.minecraft.class_3414;
import net.minecraft.class_3852;
import net.minecraft.class_4158;
import net.minecraft.class_6880;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_3852.class)
public interface MixinVillagerProfession {
   @Invoker("<init>")
   static class_3852 init(
      String id,
      Predicate<class_6880<class_4158>> predicate,
      Predicate<class_6880<class_4158>> predicate2,
      ImmutableSet<class_1792> immutableSet,
      ImmutableSet<class_2248> immutableSet2,
      @Nullable class_3414 soundEvent
   ) {
      return null;
   }
}
