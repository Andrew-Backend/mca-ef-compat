package fabric.net.mca.client.gui.widget;

import fabric.net.mca.util.compat.ButtonWidget;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_7919;
import net.minecraft.class_4185.class_4241;

public class TooltipButtonWidget extends ButtonWidget {
   public TooltipButtonWidget(int x, int y, int width, int height, String message, class_4241 onPress) {
      super(x, y, width, height, class_2561.method_43471(message), onPress, class_2561.method_43471(message + ".tooltip"));
   }

   public TooltipButtonWidget(int x, int y, int width, int height, class_5250 message, class_5250 tooltip, class_4241 onPress) {
      super(x, y, width, height, message, onPress, tooltip);
   }

   public void setMessage(String message) {
      super.method_25355(class_2561.method_43471(message));
      super.method_47400(class_7919.method_47407(class_2561.method_43471(message + ".tooltip")));
   }
}
