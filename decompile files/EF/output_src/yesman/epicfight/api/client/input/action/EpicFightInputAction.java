package yesman.epicfight.api.client.input.action;

import java.util.Optional;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.compat.controlify.EpicFightControlifyControllerMod;

public enum EpicFightInputAction implements InputAction {
   ATTACK,
   MOBILITY,
   GUARD,
   DODGE,
   LOCK_ON,
   LOCK_ON_SHIFT_LEFT,
   LOCK_ON_SHIFT_RIGHT,
   LOCK_ON_SHIFT_FREELY,
   SWITCH_MODE,
   WEAPON_INNATE_SKILL,
   WEAPON_INNATE_SKILL_TOOLTIP,
   OPEN_SKILL_SCREEN,
   OPEN_CONFIG_SCREEN,
   SWITCH_VANILLA_MODEL_DEBUGGING;

   private final int id = InputAction.ENUM_MANAGER.assign(this);

   @Override
   public int universalOrdinal() {
      return this.id;
   }

   @NotNull
   @Override
   public KeyMapping keyMapping() {
      return switch (this) {
         case ATTACK -> EpicFightKeyMappings.ATTACK;
         case MOBILITY -> EpicFightKeyMappings.MOVER_SKILL;
         case GUARD -> EpicFightKeyMappings.GUARD;
         case DODGE -> EpicFightKeyMappings.DODGE;
         case LOCK_ON -> EpicFightKeyMappings.LOCK_ON;
         case LOCK_ON_SHIFT_LEFT -> EpicFightKeyMappings.LOCK_ON_SHIFT_LEFT;
         case LOCK_ON_SHIFT_RIGHT -> EpicFightKeyMappings.LOCK_ON_SHIFT_RIGHT;
         case LOCK_ON_SHIFT_FREELY -> EpicFightKeyMappings.LOCK_ON_SHIFT_FREELY;
         case SWITCH_MODE -> EpicFightKeyMappings.SWITCH_MODE;
         case WEAPON_INNATE_SKILL -> EpicFightKeyMappings.WEAPON_INNATE_SKILL;
         case WEAPON_INNATE_SKILL_TOOLTIP -> EpicFightKeyMappings.WEAPON_INNATE_SKILL_TOOLTIP;
         case OPEN_SKILL_SCREEN -> EpicFightKeyMappings.SKILL_EDIT;
         case OPEN_CONFIG_SCREEN -> EpicFightKeyMappings.OPEN_CONFIG_SCREEN;
         case SWITCH_VANILLA_MODEL_DEBUGGING -> EpicFightKeyMappings.SWITCH_VANILLA_MODEL_DEBUGGING;
      };
   }

   @NotNull
   @Override
   public Optional<ControllerBinding> controllerBinding() {
      if (EpicFightControllerModProvider.get() == null) {
         throw new IllegalStateException("controllerBinding() must not be called when the controller mod is not installed");
      } else {
         return Optional.of(EpicFightControlifyControllerMod.getBinding(this));
      }
   }
}
