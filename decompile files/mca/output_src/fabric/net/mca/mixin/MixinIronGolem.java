package fabric.net.mca.mixin;

import fabric.net.mca.entity.VillagerEntityMCA;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1439;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_1439.class)
public abstract class MixinIronGolem extends class_1309 {
   protected MixinIronGolem(class_1299<? extends class_1309> type, class_1937 world) {
      super(type, world);
   }

   public boolean method_18395(class_1309 target) {
      return target instanceof VillagerEntityMCA ? false : super.method_18395(target);
   }
}
