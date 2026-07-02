package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Optional;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;

@Mixin(CompositeRenderType.class)
public class MixinCompositeRenderType {
   @Shadow
   private Optional<RenderType> f_110513_;

   @Inject(at = @At("RETURN"), method = "<init>")
   private void epicfight_renderTypeInit(CallbackInfo info) {
      CompositeRenderType self = (CompositeRenderType)this;
      if (self.m_173186_() == Mode.TRIANGLES && self.f_110511_.f_110576_ instanceof TextureStateShard texStateShard && texStateShard.f_110328_.isPresent()) {
         EpicFightRenderTypes.addRenderType(self.f_110133_, (ResourceLocation)texStateShard.f_110328_.get(), self);
      }
   }
}
