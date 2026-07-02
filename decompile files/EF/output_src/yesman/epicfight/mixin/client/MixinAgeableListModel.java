package yesman.epicfight.mixin.client;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AgeableListModel.class)
public interface MixinAgeableListModel {
   @Invoker("headParts")
   Iterable<ModelPart> invoke_headParts();

   @Invoker("bodyParts")
   Iterable<ModelPart> invoke_bodyParts();
}
