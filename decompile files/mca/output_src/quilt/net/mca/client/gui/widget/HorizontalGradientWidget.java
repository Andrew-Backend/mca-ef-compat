package quilt.net.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Supplier;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;

public class HorizontalGradientWidget extends HorizontalColorPickerWidget {
   private final Supplier<float[]> startColorSupplier;
   private final Supplier<float[]> endColorSupplier;

   public HorizontalGradientWidget(
      int x,
      int y,
      int width,
      int height,
      double valueX,
      Supplier<float[]> startColorSupplier,
      Supplier<float[]> endColorSupplier,
      ColorPickerWidget.DualConsumer<Double, Double> consumer
   ) {
      super(x, y, width, height, valueX, null, consumer);
      this.startColorSupplier = startColorSupplier;
      this.endColorSupplier = endColorSupplier;
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(class_757::method_34540);
      class_289 tessellator = class_289.method_1348();
      class_287 builder = tessellator.method_1349();
      builder.method_1328(class_5596.field_27382, class_290.field_1576);
      float[] startColor = this.startColorSupplier.get();
      float[] endColor = this.endColorSupplier.get();
      float z = 0.0F;
      class_4587 matrices = context.method_51448();
      Matrix4f matrix = matrices.method_23760().method_23761();
      builder.method_22918(matrix, (float)this.method_46426() + this.field_22758, this.method_46427(), z)
         .method_22915(endColor[0], endColor[1], endColor[2], endColor[3])
         .method_1344();
      builder.method_22918(matrix, this.method_46426(), this.method_46427(), z)
         .method_22915(startColor[0], startColor[1], startColor[2], startColor[3])
         .method_1344();
      builder.method_22918(matrix, this.method_46426(), (float)this.method_46427() + this.field_22759, z)
         .method_22915(startColor[0], startColor[1], startColor[2], startColor[3])
         .method_1344();
      builder.method_22918(matrix, (float)this.method_46426() + this.field_22758, (float)this.method_46427() + this.field_22759, z)
         .method_22915(endColor[0], endColor[1], endColor[2], endColor[3])
         .method_1344();
      tessellator.method_1350();
      RenderSystem.disableBlend();
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
   }
}
