package yesman.epicfight.world.entity.eventlistener;

import net.minecraft.client.player.Input;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

public class MovementInputEvent extends AbstractPlayerEvent<LocalPlayerPatch> {
   @Deprecated
   @NotNull
   private final Input movementInput;
   @Nullable
   private final PlayerInputState inputState;

   @Deprecated
   @Internal
   public MovementInputEvent(LocalPlayerPatch playerPatch, @NotNull Input input) {
      super(playerPatch, false);
      this.movementInput = input;
      this.inputState = null;
   }

   @Internal
   public MovementInputEvent(LocalPlayerPatch playerPatch, @NotNull PlayerInputState inputState) {
      super(playerPatch, false);
      this.inputState = inputState;
      this.movementInput = playerPatch.getOriginal().f_108618_;
   }

   @Deprecated
   @NotNull
   public Input getMovementInput() {
      return this.movementInput;
   }

   @NotNull
   public PlayerInputState getInputState() {
      return this.inputState == null ? InputManager.getInputState(this.movementInput) : this.inputState;
   }

   @Experimental
   public void sneakingTick(boolean isSneaking, float sneakingSpeedMultiplier) {
      InputUtils.sneakingTick(isSneaking, sneakingSpeedMultiplier);
   }
}
