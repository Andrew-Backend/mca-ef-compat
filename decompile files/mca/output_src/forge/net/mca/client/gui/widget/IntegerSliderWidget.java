package forge.net.mca.client.gui.widget;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public class IntegerSliderWidget extends ExtendedSliderWidget<Integer> {
   private final int min;
   private final int max;
   private final Function<Integer, Component> textFunction;

   public IntegerSliderWidget(
      int x,
      int y,
      int width,
      int height,
      double value,
      int min,
      int max,
      Consumer<Integer> onApplyValue,
      Function<Integer, Component> function,
      Supplier<Component> tooltipSupplier
   ) {
      super(x, y, width, height, Component.m_237113_(""), (value - min) / (max - min), onApplyValue, tooltipSupplier);
      this.min = min;
      this.max = max;
      this.textFunction = function;
      this.m_5695_();
   }

   protected void m_5695_() {
      this.m_93666_(this.textFunction.apply(this.getValue()));
   }

   Integer getValue() {
      return (int)(this.f_93577_ * (this.max - this.min + 1.0) + this.min - 0.5);
   }

   @Override
   protected double getOpticalValue() {
      return ((double)this.getValue().intValue() - this.min) / (this.max - this.min);
   }
}
