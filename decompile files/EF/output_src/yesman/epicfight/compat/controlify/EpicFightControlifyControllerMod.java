package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.controller.ControllerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;
import yesman.epicfight.api.client.input.InputMode;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;

@Internal
public class EpicFightControlifyControllerMod implements IEpicFightControllerMod {
   @Override
   public String getModName() {
      return "Controlify";
   }

   @NotNull
   @Override
   public InputMode getInputMode() {
      return switch (EpicFightControlifyEntrypoint.getApi().currentInputMode()) {
         case KEYBOARD_MOUSE -> InputMode.KEYBOARD_MOUSE;
         case CONTROLLER -> InputMode.CONTROLLER;
         case MIXED -> InputMode.MIXED;
         default -> throw new IncompatibleClassChangeError();
      };
   }

   @NotNull
   public static ControllerBinding getBinding(@NotNull EpicFightInputAction action) {
      return new ControlifyControllerBinding(EpicFightControlifyEntrypoint.getControlifyBinding(action));
   }

   @NotNull
   public static ControllerBinding getBinding(@NotNull MinecraftInputAction action) {
      return new ControlifyControllerBinding(EpicFightControlifyEntrypoint.getControlifyBinding(action));
   }

   @NotNull
   @Override
   public PlayerInputState getInputState() {
      ControllerEntity controller = EpicFightControlifyEntrypoint.requireControllerEntity();
      InputBinding forwardBind = ControlifyBindings.WALK_FORWARD.on(controller);
      InputBinding backwardBind = ControlifyBindings.WALK_BACKWARD.on(controller);
      InputBinding leftBind = ControlifyBindings.WALK_LEFT.on(controller);
      InputBinding rightBind = ControlifyBindings.WALK_RIGHT.on(controller);
      InputBinding jumpBind = ControlifyBindings.JUMP.on(controller);
      InputBinding sneakBind = ControlifyBindings.SNEAK.on(controller);
      float forwardImpulse = forwardBind.analogueNow() - backwardBind.analogueNow();
      float leftImpulse = leftBind.analogueNow() - rightBind.analogueNow();
      return new PlayerInputState(
         leftImpulse,
         forwardImpulse,
         forwardBind.digitalNow(),
         backwardBind.digitalNow(),
         leftBind.digitalNow(),
         rightBind.digitalNow(),
         jumpBind.digitalNow(),
         sneakBind.digitalNow()
      );
   }
}
