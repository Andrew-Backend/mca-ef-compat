package yesman.epicfight.api.client.input.action;

import java.util.Optional;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.compat.controlify.EpicFightControlifyControllerMod;

public enum MinecraftInputAction implements InputAction {
   JUMP,
   ATTACK_DESTROY,
   USE,
   SWAP_OFF_HAND,
   DROP,
   TOGGLE_PERSPECTIVE,
   MOVE_FORWARD,
   MOVE_BACKWARD,
   MOVE_LEFT,
   MOVE_RIGHT,
   SPRINT,
   SNEAK;

   private final int id = InputAction.ENUM_MANAGER.assign(this);

   @NotNull
   @Override
   public KeyMapping keyMapping() {
      Options options = Minecraft.m_91087_().f_91066_;

      return switch (this) {
         case USE -> options.f_92095_;
         case ATTACK_DESTROY -> options.f_92096_;
         case SWAP_OFF_HAND -> options.f_92093_;
         case DROP -> options.f_92094_;
         case TOGGLE_PERSPECTIVE -> options.f_92103_;
         case JUMP -> options.f_92089_;
         case MOVE_FORWARD -> options.f_92085_;
         case MOVE_BACKWARD -> options.f_92087_;
         case MOVE_LEFT -> options.f_92086_;
         case MOVE_RIGHT -> options.f_92088_;
         case SPRINT -> options.f_92091_;
         case SNEAK -> options.f_92090_;
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

   @Override
   public boolean isVanilla() {
      return true;
   }

   @Override
   public int universalOrdinal() {
      return this.id;
   }
}
