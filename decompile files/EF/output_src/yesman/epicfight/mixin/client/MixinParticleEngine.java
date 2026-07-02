package yesman.epicfight.mixin.client;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.ClientEngine;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine {
   @Shadow
   @Final
   private static List<ParticleRenderType> f_107288_;
   @Shadow
   @Mutable
   private Map<ParticleRenderType, Queue<Particle>> f_107289_;

   @Inject(at = @At("TAIL"), method = "<init>")
   private void epicfight$constructor(ClientLevel pLevel, TextureManager pTextureManager, CallbackInfo callbackInfo) {
      this.f_107289_ = Maps.newTreeMap(ClientEngine.makeCustomLowestParticleRenderTypeComparator(f_107288_));
   }
}
