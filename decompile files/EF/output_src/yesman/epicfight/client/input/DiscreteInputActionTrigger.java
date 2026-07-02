package yesman.epicfight.client.input;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import yesman.epicfight.api.client.input.DiscreteActionHandler;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;

@Internal
public final class DiscreteInputActionTrigger {
   private DiscreteInputActionTrigger() {
   }

   @Nullable
   private static IEpicFightControllerMod getControllerModApi() {
      return EpicFightControllerModProvider.get();
   }

   public static void triggerOnPress(@NotNull InputAction action, @NotNull DiscreteActionHandler handler) {
      IEpicFightControllerMod controllerMod = getControllerModApi();
      KeyMapping keyMapping = action.keyMapping();
      if (controllerMod == null) {
         handleKeyboardAndMouse(keyMapping, handler);
      } else {
         switch (controllerMod.getInputMode()) {
            case MIXED:
               action.controllerBinding().ifPresentOrElse(controllerBinding -> {
                  boolean handled = handleController(controllerBinding, handler);
                  if (!handled) {
                     handleKeyboardAndMouse(keyMapping, handler);
                  }
               }, () -> handleKeyboardAndMouse(keyMapping, handler));
               break;
            case CONTROLLER:
               action.controllerBinding()
                  .ifPresentOrElse(controllerBinding -> handleController(controllerBinding, handler), () -> handleKeyboardAndMouse(keyMapping, handler));
               break;
            case KEYBOARD_MOUSE:
               handleKeyboardAndMouse(keyMapping, handler);
         }
      }
   }

   private static void handleKeyboardAndMouse(@NotNull KeyMapping keyMapping, @NotNull DiscreteActionHandler handler) {
      while (keyMapping.m_90859_()) {
         handler.onAction(createContext(false));
      }
   }

   private static boolean handleController(@NotNull ControllerBinding controllerBinding, @NotNull DiscreteActionHandler handler) {
      if (controllerBinding.isDigitalJustPressed()) {
         handler.onAction(createContext(true));
         return true;
      } else {
         return false;
      }
   }

   @NotNull
   private static DiscreteActionHandler.Context createContext(boolean triggeredByController) {
      return new DiscreteActionHandler.Context(triggeredByController);
   }
}
