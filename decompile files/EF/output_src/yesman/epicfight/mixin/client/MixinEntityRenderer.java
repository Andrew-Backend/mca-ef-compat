package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface MixinEntityRenderer {
   @Invoker("shouldShowName")
   boolean invokeShouldShowName(Entity var1);

   @Invoker("renderNameTag")
   void invokeRenderNameTag(Entity var1, Component var2, PoseStack var3, MultiBufferSource var4, int var5);
}
