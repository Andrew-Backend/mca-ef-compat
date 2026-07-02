package fabric.net.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import fabric.net.mca.MCA;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_3532;
import net.minecraft.class_6382;

public class ColorPickerWidget extends class_339 {
   public static final class_2960 MCA_GUI_ICONS_TEXTURE = MCA.locate("textures/gui.png");
   private final ColorPickerWidget.DualConsumer<Double, Double> consumer;
   private final class_2960 texture;
   double valueX;
   double valueY;

   public ColorPickerWidget(
      int x, int y, int width, int height, double valueX, double valueY, class_2960 texture, ColorPickerWidget.DualConsumer<Double, Double> consumer
   ) {
      super(x, y, width, height, class_2561.method_43470(""));
      this.consumer = consumer;
      this.texture = texture;
      this.valueX = valueX;
      this.valueY = valueY;
   }

   public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.field_22765);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      context.method_25290(
         this.texture, this.method_46426(), this.method_46427(), 0.0F, 0.0F, this.field_22758, this.field_22759, this.field_22758, this.field_22759
      );
      WidgetUtils.drawRectangle(
         context, this.method_46426(), this.method_46427(), this.method_46426() + this.field_22758, this.method_46427() + this.field_22759, -1426063361
      );
      context.method_25290(
         MCA_GUI_ICONS_TEXTURE,
         (int)(this.method_46426() + this.valueX * this.field_22758) - 8,
         (int)(this.method_46427() + this.valueY * this.field_22759) - 8,
         240.0F,
         0.0F,
         16,
         16,
         256,
         256
      );
      WidgetUtils.drawRectangle(
         context, this.method_46426(), this.method_46427(), this.method_46426() + this.field_22758, this.method_46427() + this.field_22759, -1426063361
      );
   }

   protected void method_25349(double mouseX, double mouseY, double deltaX, double deltaY) {
      this.update(mouseX, mouseY);
      super.method_25349(mouseX, mouseY, deltaX, deltaY);
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (this.isInArea(mouseX, mouseY)) {
         this.update(mouseX, mouseY);
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   private boolean isInArea(double mouseX, double mouseY) {
      return mouseX >= this.method_46426()
         && mouseX <= this.method_46426() + this.field_22758
         && mouseY >= this.method_46427()
         && mouseY <= this.method_46427() + this.field_22759;
   }

   void update(double mouseX, double mouseY) {
      this.valueX = class_3532.method_15350((mouseX - this.method_46426()) / this.field_22758, 0.0, 1.0);
      this.valueY = class_3532.method_15350((mouseY - this.method_46427()) / this.field_22759, 0.0, 1.0);
      this.consumer.apply(this.valueX, this.valueY);
   }

   protected void method_47399(class_6382 builder) {
      this.method_37021(builder);
   }

   public double getValueX() {
      return this.valueX;
   }

   public void setValueX(double valueX) {
      this.valueX = valueX;
   }

   public double getValueY() {
      return this.valueY;
   }

   public void setValueY(double valueY) {
      this.valueY = valueY;
   }

   @FunctionalInterface
   public interface DualConsumer<A, B> {
      void apply(A var1, B var2);
   }
}
