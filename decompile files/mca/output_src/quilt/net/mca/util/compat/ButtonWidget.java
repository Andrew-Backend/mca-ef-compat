package quilt.net.mca.util.compat;

import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_7919;
import net.minecraft.class_4185.class_4241;

public class ButtonWidget extends class_4185 {
   public ButtonWidget(int x, int y, int width, int height, class_2561 message, class_4241 onPress) {
      super(x, y, width, height, message, onPress, field_40754);
   }

   public ButtonWidget(int x, int y, int width, int height, class_2561 message, class_4241 onPress, class_2561 tooltip) {
      this(x, y, width, height, message, onPress);
      this.method_47400(class_7919.method_47407(tooltip));
   }
}
