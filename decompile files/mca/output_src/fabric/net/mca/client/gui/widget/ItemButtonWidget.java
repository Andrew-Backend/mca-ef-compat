package fabric.net.mca.client.gui.widget;

import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_4185.class_4241;

public class ItemButtonWidget extends TooltipButtonWidget {
   final class_1799 item;

   public ItemButtonWidget(int x, int y, int size, class_5250 message, class_1799 item, class_4241 onPress) {
      super(x, y, size, size, class_2561.method_43470(""), message, onPress);
      this.item = item;
   }

   public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_48579(context, mouseX, mouseY, delta);
      int size = 16;
      context.method_51427(this.item, this.method_46426() + (this.field_22758 - size) / 2, this.method_46427() + (this.field_22759 - size) / 2);
   }
}
