package quilt.net.mca.client.gui.widget;

import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_5481;

public class NamedTextFieldWidget extends class_342 {
   private final class_327 textRenderer;

   public NamedTextFieldWidget(class_327 textRenderer, int x, int y, int width, int height, class_2561 text) {
      super(textRenderer, x + width / 2, y, width / 2, height, text);
      this.textRenderer = textRenderer;
   }

   public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_48579(context, mouseX, mouseY, delta);
      class_5481 orderedText = this.method_25369().method_30937();
      context.method_35720(
         this.textRenderer,
         orderedText,
         this.method_46426() - this.textRenderer.method_30880(orderedText) - 4,
         this.method_46427() + (this.field_22759 - 8) / 2,
         16777215
      );
   }
}
