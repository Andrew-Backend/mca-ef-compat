package fabric.net.mca.client.gui.widget;

import net.minecraft.class_5250;
import net.minecraft.class_4185.class_4241;

public class ToggleableTooltipButtonWidget extends TooltipButtonWidget {
   public boolean toggle;

   public ToggleableTooltipButtonWidget(int x, int y, int width, int height, boolean toggle, class_5250 message, class_5250 tooltip, class_4241 onPress) {
      super(x, y, width, height, message, tooltip, onPress);
      this.toggle = toggle;
   }

   protected int getYImage(boolean hovered) {
      int i = 1;
      if (this.toggle) {
         i = 0;
      } else if (hovered) {
         i = 2;
      }

      return i;
   }
}
