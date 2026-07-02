package yesman.epicfight.api.client.input;

import net.minecraft.client.player.Input;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlayerInputState(float leftImpulse, float forwardImpulse, boolean up, boolean down, boolean left, boolean right, boolean jumping, boolean sneaking) {
   public static PlayerInputState fromVanillaInput(Input input) {
      return new PlayerInputState(
         input.f_108566_, input.f_108567_, input.f_108568_, input.f_108569_, input.f_108570_, input.f_108571_, input.f_108572_, input.f_108573_
      );
   }

   public static Input applyToVanillaInput(@NotNull PlayerInputState updated, @NotNull Input input) {
      if (input.f_108566_ != updated.leftImpulse()) {
         input.f_108566_ = updated.leftImpulse();
      }

      if (input.f_108567_ != updated.forwardImpulse()) {
         input.f_108567_ = updated.forwardImpulse();
      }

      if (input.f_108568_ != updated.up()) {
         input.f_108568_ = updated.up();
      }

      if (input.f_108569_ != updated.down()) {
         input.f_108569_ = updated.down();
      }

      if (input.f_108570_ != updated.left()) {
         input.f_108570_ = updated.left();
      }

      if (input.f_108571_ != updated.right()) {
         input.f_108571_ = updated.right();
      }

      if (input.f_108572_ != updated.jumping()) {
         input.f_108572_ = updated.jumping();
      }

      if (input.f_108573_ != updated.sneaking()) {
         input.f_108573_ = updated.sneaking();
      }

      return input;
   }

   public Vec2 getMoveVector() {
      return new Vec2(this.leftImpulse, this.forwardImpulse);
   }

   public boolean hasForwardImpulse() {
      return this.forwardImpulse > 1.0E-5F;
   }

   @NotNull
   public PlayerInputState copyWith(
      @Nullable Float leftImpulse,
      @Nullable Float forwardImpulse,
      @Nullable Boolean up,
      @Nullable Boolean down,
      @Nullable Boolean left,
      @Nullable Boolean right,
      @Nullable Boolean jumping,
      @Nullable Boolean sneaking
   ) {
      return new PlayerInputState(
         leftImpulse != null ? leftImpulse : this.leftImpulse,
         forwardImpulse != null ? forwardImpulse : this.forwardImpulse,
         up != null ? up : this.up,
         down != null ? down : this.down,
         left != null ? left : this.left,
         right != null ? right : this.right,
         jumping != null ? jumping : this.jumping,
         sneaking != null ? sneaking : this.sneaking
      );
   }

   @NotNull
   public PlayerInputState withLeftImpulse(float leftImpulse) {
      return this.copyWith(leftImpulse, null, null, null, null, null, null, null);
   }

   @NotNull
   public PlayerInputState withForwardImpulse(float forwardImpulse) {
      return this.copyWith(null, forwardImpulse, null, null, null, null, null, null);
   }

   @NotNull
   public PlayerInputState withUp(boolean up) {
      return this.copyWith(null, null, up, null, null, null, null, null);
   }

   @NotNull
   public PlayerInputState withDown(boolean down) {
      return this.copyWith(null, null, null, down, null, null, null, null);
   }

   @NotNull
   public PlayerInputState withLeft(boolean left) {
      return this.copyWith(null, null, null, null, left, null, null, null);
   }

   @NotNull
   public PlayerInputState withRight(boolean right) {
      return this.copyWith(null, null, null, null, null, right, null, null);
   }

   @NotNull
   public PlayerInputState withJumping(boolean jumping) {
      return this.copyWith(null, null, null, null, null, null, jumping, null);
   }

   @NotNull
   public PlayerInputState withSneaking(boolean sneaking) {
      return this.copyWith(null, null, null, null, null, null, null, sneaking);
   }
}
