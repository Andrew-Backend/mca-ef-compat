package yesman.epicfight.api.client.input;

public record MovementDirection(int forward, int backward, int left, int right) {
   public int vertical() {
      return this.forward + this.backward;
   }

   public int horizontal() {
      return this.left + this.right;
   }

   public static MovementDirection fromBooleans(boolean up, boolean down, boolean left, boolean right) {
      return new MovementDirection(up ? 1 : 0, down ? -1 : 0, left ? 1 : 0, right ? -1 : 0);
   }

   public static MovementDirection fromInputState(PlayerInputState inputState) {
      return fromBooleans(inputState.up(), inputState.down(), inputState.left(), inputState.right());
   }
}
