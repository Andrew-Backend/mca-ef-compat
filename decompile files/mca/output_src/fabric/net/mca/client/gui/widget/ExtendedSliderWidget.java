package fabric.net.mca.client.gui.widget;

import fabric.net.mca.util.localization.FlowingText;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_357;

public abstract class ExtendedSliderWidget<T> extends class_357 {
   private T oldValue;
   final Consumer<T> onApplyValue;
   protected final Supplier<class_2561> tooltipSupplier;

   public ExtendedSliderWidget(
      int x, int y, int width, int height, class_2561 text, double value, Consumer<T> onApplyValue, Supplier<class_2561> tooltipSupplier
   ) {
      super(x, y, width, height, text, value);
      this.onApplyValue = onApplyValue;
      this.tooltipSupplier = tooltipSupplier;
   }

   protected double getOpticalValue() {
      return this.field_22753;
   }

   abstract T getValue();

   public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      int i = (this.method_49606() ? 2 : 1) * 20;
      context.method_25302(field_22757, this.method_46426() + (int)(this.getOpticalValue() * (this.field_22758 - 8)), this.method_46427(), 0, 46 + i, 4, 20);
      context.method_25302(
         field_22757, this.method_46426() + (int)(this.getOpticalValue() * (this.field_22758 - 8)) + 4, this.method_46427(), 196, 46 + i, 4, 20
      );
      super.method_48579(context, mouseX, mouseY, delta);
      if (this.method_49606()) {
         this.renderTooltip(context, mouseX, mouseY);
      }
   }

   protected void method_25344() {
      T v = this.getValue();
      if (v != this.oldValue) {
         this.oldValue = v;
         this.onApplyValue.accept(v);
      }
   }

   public void renderTooltip(class_332 context, int mouseX, int mouseY) {
      assert class_310.method_1551() != null;
      context.method_51434(class_310.method_1551().field_1772, FlowingText.wrap(this.tooltipSupplier.get(), 160), mouseX, mouseY);
   }
}
