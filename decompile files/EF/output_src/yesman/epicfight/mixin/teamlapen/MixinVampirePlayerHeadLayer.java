package yesman.epicfight.mixin.teamlapen;

import de.teamlapen.vampirism.client.renderer.entity.layers.VampirePlayerHeadLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = VampirePlayerHeadLayer.class, remap = false)
public interface MixinVampirePlayerHeadLayer {
   @Accessor(remap = false)
   ResourceLocation[] getEyeOverlays();

   @Accessor(remap = false)
   ResourceLocation[] getFangOverlays();
}
