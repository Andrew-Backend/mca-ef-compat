package fabric.net.mca.client.gui.widget;

import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_357;

public class GeneSliderWidget extends class_357 {
   private final Consumer<Double> callback;

   public GeneSliderWidget(int x, int y, int width, int height, class_2561 text, double value, Consumer<Double> callback) {
      super(x, y, width, height, text, value);
      this.method_25346();
      this.callback = callback;
   }

   protected void method_25344() {
      this.callback.accept(this.field_22753);
   }

   protected void method_25346() {
   }
}
