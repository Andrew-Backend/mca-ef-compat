package yesman.epicfight.mixin.skinlayers;

import dev.tr7zw.skinlayers.versionless.render.CustomizableCube;
import dev.tr7zw.skinlayers.versionless.render.CustomizableCube.Polygon;
import dev.tr7zw.skinlayers.versionless.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public abstract class MixinCustomizableCubeWrapper extends CustomizableCube {
   private MixinCustomizableCubeWrapper(
      int u,
      int v,
      float x,
      float y,
      float z,
      float sizeX,
      float sizeY,
      float sizeZ,
      float extraX,
      float extraY,
      float extraZ,
      boolean mirror,
      float textureWidth,
      float textureHeight,
      Direction[] hide,
      Direction[][] hideCorners
   ) {
      super(u, v, x, y, z, sizeX, sizeY, sizeZ, extraX, extraY, extraZ, mirror, textureWidth, textureHeight, hide, hideCorners);
   }

   @Mixin(value = CustomizableCube.class, remap = false)
   public interface SkinLayer3DMixinCustomModelCube {
      @Accessor(value = "polygons", remap = false)
      Polygon[] getPolygons();
   }
}
