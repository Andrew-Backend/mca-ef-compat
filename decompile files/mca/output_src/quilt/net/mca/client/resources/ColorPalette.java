package quilt.net.mca.client.resources;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_5253.class_8045;

public class ColorPalette {
   static final Map<class_2960, ColorPalette> REGISTRY = new HashMap<>();
   public static final ColorPalette SKIN = new ColorPalette(new class_2960("mca", "textures/colormap/villager_skin.png"));
   public static final ColorPalette HAIR = new ColorPalette(new class_2960("mca", "textures/colormap/villager_hair.png"));
   static final ColorPalette.Data EMPTY = new ColorPalette.Data(1, 1, new int[]{16777215});
   private final class_2960 id;
   ColorPalette.Data data = EMPTY;

   public ColorPalette(class_2960 id) {
      this.id = id;
      REGISTRY.put(id, this);
   }

   public class_2960 getId() {
      return this.id;
   }

   public float[] getColor(float u, float v, float greenShift) {
      int x = clampFloor(v, this.data.width - 1);
      int y = clampFloor(u, this.data.height - 1);
      int color = this.data.colors[y * this.data.height + x];
      float[] result = new float[]{class_8045.method_48347(color) / 255.0F, class_8045.method_48346(color) / 255.0F, class_8045.method_48345(color) / 255.0F};
      if (greenShift > 0.0F) {
         applyGreenShift(result, greenShift);
      }

      return result;
   }

   private static void applyGreenShift(float[] color, float greenShift) {
      float percentDown = 1.0F - greenShift / 1.8F;
      color[0] = class_3532.method_15363(color[0] * percentDown, 0.0F, 1.0F);
      color[1] = class_3532.method_15363(color[1] * percentDown, 0.0F, 1.0F);
      color[2] = class_3532.method_15363(color[2] * percentDown, 0.0F, 1.0F);
   }

   private static int clampFloor(float v, int max) {
      return (int)Math.floor(class_3532.method_15363(v * max, 0.0F, max));
   }

   static class Data {
      final int width;
      final int height;
      final int[] colors;

      public Data(int width, int height, int[] colors) {
         this.width = width;
         this.height = height;
         this.colors = colors;
      }
   }
}
