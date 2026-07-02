package yesman.epicfight.client.camera;

import org.jetbrains.annotations.NotNull;

public enum EpicFightTpsCameraDisabledReason {
   ShoulderSurfing("Shoulder Surfing"),
   BetterThirdPerson("Better Third Person");

   @NotNull
   private final String modName;

   EpicFightTpsCameraDisabledReason(@NotNull String modName) {
      this.modName = modName;
   }

   @NotNull
   public String getModName() {
      return this.modName;
   }
}
