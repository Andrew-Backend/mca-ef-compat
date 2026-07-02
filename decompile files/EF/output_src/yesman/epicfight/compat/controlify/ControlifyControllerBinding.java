package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.bind.InputBinding;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;
import yesman.epicfight.api.client.input.controller.ControllerBinding;

@Internal
public record ControlifyControllerBinding(@NotNull InputBinding inputBinding) implements ControllerBinding {
   @NotNull
   @Override
   public ResourceLocation id() {
      return this.inputBinding.id();
   }

   @Override
   public boolean isDigitalActiveNow() {
      return this.inputBinding.digitalNow();
   }

   @Override
   public boolean wasDigitalActivePreviously() {
      return this.inputBinding.digitalPrev();
   }

   @Override
   public boolean isDigitalJustPressed() {
      return this.inputBinding.justPressed();
   }

   @Override
   public boolean isDigitalJustReleased() {
      return this.inputBinding.justReleased();
   }

   @Override
   public float getAnalogueNow() {
      return this.inputBinding.analogueNow();
   }

   @Override
   public void emulatePress() {
      this.inputBinding.fakePress();
   }

   @NotNull
   @Override
   public Object physicalInputId() {
      return this.inputBinding.boundInput().getRelevantInputs();
   }
}
