package yesman.epicfight.api.client.input;

public enum InputMode {
   KEYBOARD_MOUSE,
   CONTROLLER,
   MIXED;

   public boolean supportsKeyboardAndMouse() {
      return switch (this) {
         case KEYBOARD_MOUSE, MIXED -> true;
         case CONTROLLER -> false;
      };
   }

   public boolean supportsController() {
      return switch (this) {
         case KEYBOARD_MOUSE -> false;
         case MIXED, CONTROLLER -> true;
      };
   }
}
