package yesman.epicfight.api.client.event.types;

import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.Event;

public abstract class CameraAPIEvent extends Event {
   private final EpicFightCameraAPI cameraApi;

   public CameraAPIEvent(EpicFightCameraAPI cameraApi) {
      this.cameraApi = cameraApi;
   }

   public final EpicFightCameraAPI getCameraApi() {
      return this.cameraApi;
   }
}
