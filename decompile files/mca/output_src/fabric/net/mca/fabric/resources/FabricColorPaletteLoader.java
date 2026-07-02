package fabric.net.mca.fabric.resources;

import fabric.net.mca.client.resources.ColorPaletteLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.class_2960;

public class FabricColorPaletteLoader extends ColorPaletteLoader implements IdentifiableResourceReloadListener {
   public class_2960 getFabricId() {
      return ID;
   }
}
