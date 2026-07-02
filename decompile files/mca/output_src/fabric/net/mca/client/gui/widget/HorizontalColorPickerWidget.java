package fabric.net.mca.client.gui.widget;

import net.minecraft.class_2960;

public class HorizontalColorPickerWidget extends ColorPickerWidget {
   public HorizontalColorPickerWidget(
      int x, int y, int width, int height, double valueX, class_2960 texture, ColorPickerWidget.DualConsumer<Double, Double> consumer
   ) {
      super(x, y, width, height, valueX, 0.5, texture, consumer);
   }

   @Override
   void update(double mouseX, double mouseY) {
      super.update(mouseX, mouseY);
      this.valueY = 0.5;
   }
}
