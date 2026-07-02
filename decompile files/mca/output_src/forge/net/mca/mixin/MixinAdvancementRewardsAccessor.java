package forge.net.mca.mixin;

import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementRewards.class)
public interface MixinAdvancementRewardsAccessor {
   @Accessor("f_9980_")
   ResourceLocation[] getLoot();

   @Accessor("f_9980_")
   @Mutable
   void setLoot(ResourceLocation[] var1);
}
