package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.config.ClientConfig;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
   @Shadow
   private RenderBuffers f_109464_;
   @Shadow
   private Minecraft f_109461_;

   @Inject(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V", shift = Shift.AFTER),
      method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void epicfight$renderLevel(
      PoseStack pPoseStack,
      float pPartialTick,
      long pFinishNanoTime,
      boolean pRenderBlockOutline,
      Camera pCamera,
      GameRenderer pGameRenderer,
      LightTexture pLightTexture,
      Matrix4f pProjectionMatrix,
      CallbackInfo callbackInfo,
      ProfilerFiller local1,
      Vec3 local2,
      double local3,
      double local4,
      double local5,
      Matrix4f local6,
      boolean local7,
      Frustum local8,
      float local9,
      boolean local10,
      boolean local11,
      BufferSource local12,
      Iterator<?> local13,
      Entity local14,
      BlockPos local15,
      MultiBufferSource local16,
      OutlineBufferSource local17,
      int local18
   ) {
      int color = ClientConfig.packedTargetOutlineColor;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      if (EpicFightCameraAPI.getInstance().shouldHighlightTarget(local14)) {
         this.f_109464_.m_110109_().m_109929_(r, g, b, 255);
      }
   }
}
