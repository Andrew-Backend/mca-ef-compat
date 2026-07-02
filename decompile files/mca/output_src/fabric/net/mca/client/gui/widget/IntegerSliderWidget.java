package fabric.net.mca.client.gui.widget;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.class_2561;

public class IntegerSliderWidget extends ExtendedSliderWidget<Integer> {
   private final int min;
   private final int max;
   private final Function<Integer, class_2561> textFunction;

   public IntegerSliderWidget(
      int x,
      int y,
      int width,
      int height,
      double value,
      int min,
      int max,
      Consumer<Integer> onApplyValue,
      Function<Integer, class_2561> function,
      Supplier<class_2561> tooltipSupplier
   ) {
      super(x, y, width, height, class_2561.method_43470(""), (value - min) / (max - min), onApplyValue, tooltipSupplier);
      this.min = min;
      this.max = max;
      this.textFunction = function;
      this.method_25346();
   }

   protected void method_25346() {
      this.method_25355(this.textFunction.apply(this.getValue()));
   }

   Integer getValue() {
      return (int)(this.field_22753 * (this.max - this.min + 1.0) + this.min - 0.5);
   }

   @Override
   protected double getOpticalValue() {
      return ((double)this.getValue().intValue() - this.min) / (this.max - this.min);
   }
}
