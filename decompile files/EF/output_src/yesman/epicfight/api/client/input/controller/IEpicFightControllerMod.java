package yesman.epicfight.api.client.input.controller;

import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.InputMode;
import yesman.epicfight.api.client.input.PlayerInputState;

public interface IEpicFightControllerMod {
   String getModName();

   @NotNull
   InputMode getInputMode();

   @NotNull
   PlayerInputState getInputState();
}
