package quilt.net.mca.mixin;

import net.minecraft.class_170;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_170.class)
public interface MixinAdvancementRewardsAccessor {
   @Accessor("field_1164")
   class_2960[] getLoot();

   @Accessor("field_1164")
   @Mutable
   void setLoot(class_2960[] var1);
}
