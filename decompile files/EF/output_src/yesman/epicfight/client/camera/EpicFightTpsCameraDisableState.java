package yesman.epicfight.client.camera;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.event.EpicFightClientHooks;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightTpsCameraDisableState {
   @Nullable
   private static EpicFightTpsCameraDisabledReason reason = null;
   private static boolean eventRegistered = false;

   private EpicFightTpsCameraDisableState() {
   }

   public static void disable(@NotNull EpicFightTpsCameraDisabledReason reason) {
      Objects.requireNonNull(reason, "reason must not be null");
      EpicFightTpsCameraDisableState.reason = reason;
      EpicFightMod.LOGGER.info("Epic Fight TPS mode has been disabled due to a mod conflict with {}", reason.getModName());
      if (!eventRegistered) {
         EpicFightClientHooks.Camera.ACTIVATE_TPS_CAMERA.registerEvent(e -> {
            if (reason != null) {
               e.cancel();
            }
         });
         eventRegistered = true;
      }
   }

   @Nullable
   public static EpicFightTpsCameraDisabledReason getReason() {
      return reason;
   }
}
