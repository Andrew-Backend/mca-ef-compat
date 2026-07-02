package yesman.epicfight.api.client.input;

import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;
import yesman.epicfight.client.input.DiscreteInputActionTrigger;

@Experimental
public final class InputManager {
   private InputManager() {
   }

   @Nullable
   private static IEpicFightControllerMod getControllerModApi() {
      return EpicFightControllerModProvider.get();
   }

   @NotNull
   public static InputMode getInputMode() {
      IEpicFightControllerMod controllerMod = getControllerModApi();
      return controllerMod == null ? InputMode.KEYBOARD_MOUSE : controllerMod.getInputMode();
   }

   public static boolean supportsControllerInput() {
      return getInputMode().supportsController();
   }

   public static boolean isActionActive(@NotNull InputAction action) {
      return checkAction(action, InputManager::isKeyDown);
   }

   public static boolean isActionPhysicallyActive(@NotNull InputAction action) {
      return checkAction(action, InputManager::isPhysicalKeyDown);
   }

   private static boolean checkAction(@NotNull InputAction action, @NotNull Function<KeyMapping, Boolean> keyboardCheck) {
      IEpicFightControllerMod controllerMod = getControllerModApi();
      if (controllerMod == null) {
         return keyboardCheck.apply(action.keyMapping());
      }

      return switch (controllerMod.getInputMode()) {
         case KEYBOARD_MOUSE -> keyboardCheck.apply(action.keyMapping());
         case CONTROLLER -> action.controllerBinding().map(ControllerBinding::isDigitalActiveNow).orElse(keyboardCheck.apply(action.keyMapping()));
         case MIXED -> keyboardCheck.apply(action.keyMapping()) || action.controllerBinding().map(ControllerBinding::isDigitalActiveNow).orElse(false);
      };
   }

   public static void triggerOnPress(@NotNull InputAction action, @NotNull DiscreteActionHandler handler) {
      DiscreteInputActionTrigger.triggerOnPress(action, handler);
   }

   public static void triggerOnPress(@NotNull InputAction action, @NotNull Runnable runnable) {
      triggerOnPress(action, context -> runnable.run());
   }

   public static boolean isBoundToSamePhysicalInput(@NotNull InputAction action, @NotNull InputAction action2) {
      IEpicFightControllerMod controllerMod = getControllerModApi();
      if (controllerMod != null && controllerMod.getInputMode() == InputMode.CONTROLLER) {
         Optional<ControllerBinding> optionalControllerBinding = action.controllerBinding();
         Optional<ControllerBinding> optionalControllerBinding2 = action2.controllerBinding();
         if (optionalControllerBinding.isPresent() && optionalControllerBinding2.isPresent()) {
            return optionalControllerBinding.get().isBoundToSamePhysicalInput(optionalControllerBinding2.get());
         }
      }

      KeyMapping keyMapping1 = action.keyMapping();
      KeyMapping keyMapping2 = action2.keyMapping();
      return keyMapping1.getKey() == keyMapping2.getKey();
   }

   @NotNull
   public static PlayerInputState getInputState(@NotNull Input vanillaInput) {
      IEpicFightControllerMod controllerMod = getControllerModApi();
      return controllerMod != null && controllerMod.getInputMode() == InputMode.CONTROLLER
         ? controllerMod.getInputState()
         : PlayerInputState.fromVanillaInput(vanillaInput);
   }

   @NotNull
   public static PlayerInputState getInputState(@NotNull LocalPlayer localPlayer) {
      return getInputState(localPlayer.f_108618_);
   }

   public static void setInputState(@NotNull PlayerInputState inputState) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         Input input = player.f_108618_;
         PlayerInputState.applyToVanillaInput(inputState, input);
      }
   }

   private static boolean isKeyDown(@NotNull KeyMapping keyMapping) {
      boolean isDown = keyMapping.m_90857_();
      return !isDown && keyMapping.getKey().m_84868_() == Type.MOUSE ? isPhysicalKeyDown(keyMapping) : isDown;
   }

   @Internal
   private static boolean isPhysicalKeyDown(@NotNull KeyMapping keyMapping) {
      Key key = keyMapping.getKey();
      int keyValue = key.m_84873_();
      long windowPointer = Minecraft.m_91087_().m_91268_().m_85439_();
      if (key.m_84868_() == Type.KEYSYM) {
         return GLFW.glfwGetKey(windowPointer, keyValue) > 0;
      } else {
         return key.m_84868_() == Type.MOUSE ? GLFW.glfwGetMouseButton(windowPointer, keyValue) > 0 : false;
      }
   }
}
