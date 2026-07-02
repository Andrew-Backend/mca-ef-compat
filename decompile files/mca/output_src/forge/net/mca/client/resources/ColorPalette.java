package forge.net.mca.client.resources;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ABGR32;

public class ColorPalette {
   static final Map<ResourceLocation, ColorPalette> REGISTRY = new HashMap<>();
   public static final ColorPalette SKIN = new ColorPalette(new ResourceLocation("mca", "textures/colormap/villager_skin.png"));
   public static final ColorPalette HAIR = new ColorPalette(new ResourceLocation("mca", "textures/colormap/villager_hair.png"));
   static final ColorPalette.Data EMPTY = new ColorPalette.Data(1, 1, new int[]{16777215});
   private final ResourceLocation id;
   ColorPalette.Data data = EMPTY;

   public ColorPalette(ResourceLocation id) {
      this.id = id;
      REGISTRY.put(id, this);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public float[] getColor(float u, float v, float greenShift) {
      int x = clampFloor(v, this.data.width - 1);
      int y = clampFloor(u, this.data.height - 1);
      int color = this.data.colors[y * this.data.height + x];
      float[] result = new float[]{ABGR32.m_266247_(color) / 255.0F, ABGR32.m_266446_(color) / 255.0F, ABGR32.m_266313_(color) / 255.0F};
      if (greenShift > 0.0F) {
         applyGreenShift(result, greenShift);
      }

      return result;
   }

   private static void applyGreenShift(float[] color, float greenShift) {
      float percentDown = 1.0F - greenShift / 1.8F;
      color[0] = Mth.m_14036_(color[0] * percentDown, 0.0F, 1.0F);
      color[1] = Mth.m_14036_(color[1] * percentDown, 0.0F, 1.0F);
      color[2] = Mth.m_14036_(color[2] * percentDown, 0.0F, 1.0F);
   }

   private static int clampFloor(float v, int max) {
      return (int)Math.floor(Mth.m_14036_(v * max, 0.0F, max));
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
