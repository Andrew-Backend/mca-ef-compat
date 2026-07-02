package yesman.epicfight.mixin.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.config.ClientConfig;

@Mixin(Options.class)
public class MixinOptions {
   @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
   private void onSetCameraType(CameraType requested, CallbackInfo ci) {
      ClientConfig.CameraPerspectiveToggleMode preference = (ClientConfig.CameraPerspectiveToggleMode)ClientConfig.CAMERA_PERSPECTIVE_TOGGLE_MODE.get();
      Options options = Minecraft.m_91087_().f_91066_;
      switch (preference) {
         case SKIP_THIRD_PERSON_FRONT:
            if (requested == CameraType.THIRD_PERSON_FRONT) {
               CameraType current = options.m_92176_();
               CameraType replacement = current == CameraType.FIRST_PERSON ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON;
               options.m_92157_(replacement);
               ci.cancel();
            }
         case VANILLA:
      }
   }
}
