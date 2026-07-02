package quilt.net.mca.client.gui.widget;

import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_474;
import net.minecraft.class_4185.class_4241;

public class ExtendedPageTurnWidget extends class_474 {
   private final class_2960 texture;
   private final boolean isNextPageButton;

   public ExtendedPageTurnWidget(int x, int y, boolean isNextPageButton, class_4241 action, boolean playPageTurnSound, class_2960 texture) {
      super(x, y, isNextPageButton, action, playPageTurnSound);
      this.isNextPageButton = isNextPageButton;
      this.texture = texture;
   }

   public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      int i = 0;
      int j = 192;
      if (this.method_49606()) {
         i += 23;
      }

      if (!this.isNextPageButton) {
         j += 13;
      }

      context.method_25302(this.texture, this.method_46426(), this.method_46427(), i, j, 23, 13);
   }
}
