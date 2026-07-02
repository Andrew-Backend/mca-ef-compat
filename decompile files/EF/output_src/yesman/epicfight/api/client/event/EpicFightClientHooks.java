package yesman.epicfight.api.client.event;

import yesman.epicfight.api.client.event.types.ActivateTPSCamera;
import yesman.epicfight.api.client.event.types.BuildCameraTransform;
import yesman.epicfight.api.client.event.types.CoupleTPSCamera;
import yesman.epicfight.api.client.event.types.ItemUsedInDecoupledCamera;
import yesman.epicfight.api.client.event.types.LockOnEvent;
import yesman.epicfight.api.event.CancelableEventHook;
import yesman.epicfight.api.event.EventHook;

public final class EpicFightClientHooks {
   private EpicFightClientHooks() {
   }

   public static final class Camera {
      public static final CancelableEventHook<BuildCameraTransform.Pre> BUILD_TRANSFORM_PRE = CancelableEventHook.createCancelableEventHook();
      public static final EventHook<BuildCameraTransform.Post> BUILD_TRANSFORM_POST = EventHook.createEventHook();
      public static final EventHook<ItemUsedInDecoupledCamera> ITEM_USED_WHEN_DECOUPLED = EventHook.createEventHook();
      public static final EventHook<ActivateTPSCamera> ACTIVATE_TPS_CAMERA = CancelableEventHook.createCancelableEventHook();
      public static final EventHook<CoupleTPSCamera> COUPLE_CAMERA = EventHook.createEventHook();
      public static final EventHook<LockOnEvent.Start> LOCK_ON_START = CancelableEventHook.createCancelableEventHook();
      public static final EventHook<LockOnEvent.Tick> LOCK_ON_TICK = EventHook.createEventHook();
      public static final EventHook<LockOnEvent.Release> LOCK_ON_RELEASED = CancelableEventHook.createCancelableEventHook();
   }
}
