package quilt.net.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_1309;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_308;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_898;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class WidgetUtils {
   public static void drawRectangle(class_332 context, int x0, int y0, int x1, int y1, int color) {
      context.method_25294(x0 + 1, y0, x1, y0 + 1, color);
      context.method_25294(x1 - 1, y0 + 1, x1, y1, color);
      context.method_25294(x0, y1 - 1, x1 - 1, y1, color);
      context.method_25294(x0, y0, x0 + 1, y1 - 1, color);
   }

   public static void drawTexturedQuad(Matrix4f matrix, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
      RenderSystem.setShader(class_757::method_34542);
      class_287 bufferBuilder = class_289.method_1348().method_1349();
      bufferBuilder.method_1328(class_5596.field_27382, class_290.field_1585);
      bufferBuilder.method_22918(matrix, x0, y1, z).method_22913(u0, v1).method_1344();
      bufferBuilder.method_22918(matrix, x1, y1, z).method_22913(u1, v1).method_1344();
      bufferBuilder.method_22918(matrix, x1, y0, z).method_22913(u1, v0).method_1344();
      bufferBuilder.method_22918(matrix, x0, y0, z).method_22913(u0, v0).method_1344();
      class_286.method_43433(bufferBuilder.method_1326());
   }

   public static void drawBackgroundEntity(int x, int y, int size, float mouseX, float mouseY, class_1309 entity) {
      float f = (float)Math.atan(mouseX / 40.0F);
      float g = (float)Math.atan(mouseY / 40.0F);
      class_4587 matrixStack = RenderSystem.getModelViewStack();
      matrixStack.method_22903();
      matrixStack.method_46416(x, y, 50.0F);
      matrixStack.method_22905(1.0F, 1.0F, -1.0F);
      RenderSystem.applyModelViewMatrix();
      class_4587 matrixStack2 = new class_4587();
      matrixStack2.method_46416(0.0F, 0.0F, 1000.0F);
      matrixStack2.method_22905(size, size, size);
      Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf quaternionf2 = new Quaternionf().rotateX(g * 20.0F * (float) (Math.PI / 180.0));
      quaternionf.mul(quaternionf2);
      matrixStack2.method_22907(quaternionf);
      float h = entity.field_6283;
      float i = entity.method_36454();
      float j = entity.method_36455();
      float k = entity.field_6259;
      float l = entity.field_6241;
      entity.field_6283 = 180.0F + f * 20.0F;
      entity.method_36456(180.0F + f * 40.0F);
      entity.method_36457(-g * 20.0F);
      entity.field_6241 = entity.method_36454();
      entity.field_6259 = entity.method_36454();
      class_308.method_34742();
      class_898 entityRenderDispatcher = class_310.method_1551().method_1561();
      quaternionf2.conjugate();
      entityRenderDispatcher.method_24196(quaternionf2);
      entityRenderDispatcher.method_3948(false);
      class_4598 immediate = class_310.method_1551().method_22940().method_23000();
      RenderSystem.runAsFancy(() -> entityRenderDispatcher.method_3954(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrixStack2, immediate, 15728880));
      immediate.method_22993();
      entityRenderDispatcher.method_3948(true);
      entity.field_6283 = h;
      entity.method_36456(i);
      entity.method_36457(j);
      entity.field_6259 = k;
      entity.field_6241 = l;
      matrixStack.method_22909();
      RenderSystem.applyModelViewMatrix();
      class_308.method_24211();
   }
}
