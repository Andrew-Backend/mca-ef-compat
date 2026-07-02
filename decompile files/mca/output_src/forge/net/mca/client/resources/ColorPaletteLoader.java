package forge.net.mca.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import forge.net.mca.MCA;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class ColorPaletteLoader extends SimplePreparableReloadListener<Map<ResourceLocation, ColorPalette.Data>> {
   protected static final ResourceLocation ID = new ResourceLocation("mca", "color_palettes");

   protected Map<ResourceLocation, ColorPalette.Data> prepare(ResourceManager manager, ProfilerFiller profiler) {
      return ColorPalette.REGISTRY.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> this.loadPalette(entry.getKey(), manager)));
   }

   private ColorPalette.Data loadPalette(ResourceLocation id, ResourceManager manager) {
      try {
         NativeImage img = NativeImage.m_85058_(((Resource)manager.m_213713_(id).get()).m_215507_());

         ColorPalette.Data var4;
         try {
            var4 = new ColorPalette.Data(img.m_84982_(), img.m_85084_(), img.m_85118_());
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

   protected void apply(Map<ResourceLocation, ColorPalette.Data> palettes, ResourceManager manager, ProfilerFiller profiler) {
      palettes.forEach((id, data) -> {
         if (ColorPalette.REGISTRY.containsKey(id)) {
            ColorPalette.REGISTRY.get(id).data = Objects.requireNonNull(data);
         }
      });
   }
}
