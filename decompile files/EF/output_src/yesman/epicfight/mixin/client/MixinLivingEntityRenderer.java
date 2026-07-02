package yesman.epicfight.mixin.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface MixinLivingEntityRenderer {
   @Invoker("isBodyVisible")
   boolean invokeIsBodyVisible(LivingEntity var1);

   @Invoker("getRenderType")
   RenderType invokeGetRenderType(LivingEntity var1, boolean var2, boolean var3, boolean var4);

   @Invoker("getBob")
   float invokeGetBob(LivingEntity var1, float var2);
}
