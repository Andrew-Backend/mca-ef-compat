package quilt.net.mca.client.resources;

import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.class_1011;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4080;
import quilt.net.mca.MCA;

public class ColorPaletteLoader extends class_4080<Map<class_2960, ColorPalette.Data>> {
   protected static final class_2960 ID = new class_2960("mca", "color_palettes");

   protected Map<class_2960, ColorPalette.Data> prepare(class_3300 manager, class_3695 profiler) {
      return ColorPalette.REGISTRY.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> this.loadPalette(entry.getKey(), manager)));
   }

   private ColorPalette.Data loadPalette(class_2960 id, class_3300 manager) {
      try {
         class_1011 img = class_1011.method_4309(((class_3298)manager.method_14486(id).get()).method_14482());

         ColorPalette.Data var4;
         try {
            var4 = new ColorPalette.Data(img.method_4307(), img.method_4323(), img.method_4322());
         } catch (Throwable var7) {
            if (img != null) {
               try {
                  img.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (img != null) {
            img.close();
         }

         return var4;
      } catch (Exception e) {
         MCA.LOGGER.error("Failed to load color palette from `{}`", id, e);
         return ColorPalette.EMPTY;
      }
   }

   protected void apply(Map<class_2960, ColorPalette.Data> palettes, class_3300 manager, class_3695 profiler) {
      palettes.forEach((id, data) -> {
         if (ColorPalette.REGISTRY.containsKey(id)) {
            ColorPalette.REGISTRY.get(id).data = Objects.requireNonNull(data);
         }
      });
   }
}
