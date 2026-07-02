package yesman.epicfight.api.client.input.controller;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface ControllerBinding {
   @NotNull
   ResourceLocation id();

   boolean isDigitalActiveNow();

   boolean wasDigitalActivePreviously();

   boolean isDigitalJustPressed();

   boolean isDigitalJustReleased();

   float getAnalogueNow();

   void emulatePress();

   @NotNull
   Object physicalInputId();

   default boolean isBoundToSamePhysicalInput(@NotNull ControllerBinding other) {
      return this.physicalInputId().equals(other.physicalInputId());
   }
}
