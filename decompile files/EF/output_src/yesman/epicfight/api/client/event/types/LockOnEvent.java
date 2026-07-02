package yesman.epicfight.api.client.event.types;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.CancelableEvent;

public abstract class LockOnEvent extends CameraAPIEvent {
   private final LivingEntity target;

   public LockOnEvent(EpicFightCameraAPI cameraApi, LivingEntity target) {
      super(cameraApi);
      this.target = target;
   }

   public final LivingEntity getLockOnTarget() {
      return this.target;
   }

   public static final class Release extends LockOnEvent implements CancelableEvent {
      public Release(EpicFightCameraAPI cameraApi, LivingEntity target) {
         super(cameraApi, target);
      }
   }

   public static final class Start extends LockOnEvent implements CancelableEvent {
      public Start(EpicFightCameraAPI cameraApi, LivingEntity target) {
         super(cameraApi, target);
      }
   }

   public static final class Tick extends LockOnEvent {
      private final float xRot;
      private final float yRot;
      private float modifiedXRot;
      private float modifiedYRot;

      public Tick(EpicFightCameraAPI cameraApi, LivingEntity target, float xRot, float yRot) {
         super(cameraApi, target);
         this.xRot = xRot;
         this.yRot = yRot;
         this.modifiedXRot = xRot;
         this.modifiedYRot = yRot;
      }

      public void setXRot(float xRot) {
         this.modifiedXRot = xRot;
      }

      public void setYRot(float yRot) {
         this.modifiedYRot = yRot;
      }

      public float getOriginalXRot() {
         return this.xRot;
      }

      public float getOriginalYRot() {
         return this.yRot;
      }

      public float getModifiedXRot() {
         return this.modifiedXRot;
      }

      public float getModifiedYRot() {
         return this.modifiedYRot;
      }
   }
}
