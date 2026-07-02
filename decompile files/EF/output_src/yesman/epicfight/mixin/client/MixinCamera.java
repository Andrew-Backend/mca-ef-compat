package yesman.epicfight.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.event.types.BuildCameraTransform;

@Mixin(Camera.class)
public abstract class MixinCamera {
   @Shadow
   private boolean f_90549_;
   @Shadow
   private BlockGetter f_90550_;
   @Shadow
   private Entity f_90551_;
   @Shadow
   private boolean f_90560_;

   @Inject(at = @At("HEAD"), method = "setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", cancellable = true)
   public void epicfight$setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo callbackInfo) {
      this.f_90549_ = true;
      this.f_90550_ = level;
      this.f_90551_ = entity;
      this.f_90560_ = detached;
      EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
      Camera camera = (Camera)this;
      BuildCameraTransform.Pre buildEvent = cameraApi.setupCamera(camera, partialTick);
      if (!buildEvent.hasCanceled()) {
         if (buildEvent.isVanillaCameraSetupCanceled()) {
            callbackInfo.cancel();
         } else {
            cameraApi.fireCameraBuildPost(camera, partialTick);
         }
      }
   }
}
