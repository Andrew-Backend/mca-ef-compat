package yesman.epicfight.api.client.input.action;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

@Experimental
public interface InputAction extends ExtendableEnum {
   ExtendableEnumManager<InputAction> ENUM_MANAGER = new ExtendableEnumManager<>("input_action");

   @NotNull
   KeyMapping keyMapping();

   @NotNull
   Optional<ControllerBinding> controllerBinding();

   default boolean isVanilla() {
      return false;
   }

   @NotNull
   static Set<InputAction> nonVanillaActions() {
      Set<InputAction> result = new HashSet<>();

      for (InputAction action : ENUM_MANAGER.universalValues()) {
         if (!action.isVanilla()) {
            result.add(action);
         }
      }

      return result;
   }

   @Nullable
   static InputAction fromKeyMapping(@NotNull KeyMapping keyMapping) {
      return ENUM_MANAGER.universalValues().stream().filter(action -> action.keyMapping() == keyMapping).findFirst().orElse(null);
   }
}
