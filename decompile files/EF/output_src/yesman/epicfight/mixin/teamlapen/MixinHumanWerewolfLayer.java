package yesman.epicfight.mixin.teamlapen;

import de.teamlapen.werewolves.client.render.layer.HumanWerewolfLayer;
import java.util.List;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = HumanWerewolfLayer.class, remap = false)
public interface MixinHumanWerewolfLayer<T extends LivingEntity, A extends HumanoidModel<T>> {
   @Accessor(remap = false)
   List<ResourceLocation> getTextures();

   @Accessor(remap = false)
   A getModel();
}
