package forge.net.mca.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.List;

public class SkinPorter {
   private static final List<SkinPorter.UVMapping> mappings = List.of(
      new SkinPorter.UVMapping(4, 16, 8, 20, 16, 32, true),
      new SkinPorter.UVMapping(8, 16, 12, 20, 16, 32, true),
      new SkinPorter.UVMapping(0, 20, 4, 32, 24, 32, true),
      new SkinPorter.UVMapping(4, 20, 8, 32, 16, 32, true),
      new SkinPorter.UVMapping(8, 20, 12, 32, 8, 32, true),
      new SkinPorter.UVMapping(12, 20, 16, 32, 16, 32, true),
      new SkinPorter.UVMapping(44, 16, 48, 20, -8, 32, true),
      new SkinPorter.UVMapping(48, 16, 52, 20, -8, 32, true),
      new SkinPorter.UVMapping(40, 20, 44, 32, 0, 32, true),
      new SkinPorter.UVMapping(44, 20, 48, 32, -8, 32, true),
      new SkinPorter.UVMapping(48, 20, 52, 32, -16, 32, true),
      new SkinPorter.UVMapping(52, 20, 56, 32, -8, 32, true)
   );

   public static NativeImage portLegacySkin(NativeImage image) {
      NativeImage ported = new NativeImage(64, 64, false);

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 32; y++) {
            ported.m_84988_(x, y, image.m_84985_(x, y));
         }
      }

      for (int x = 0; x < 64; x++) {
         for (int y = 32; y < 64; y++) {
            ported.m_84988_(x, y, 0);
         }
      }

      for (SkinPorter.UVMapping mapping : mappings) {
         for (int x = mapping.x0; x < mapping.x1; x++) {
            for (int y = mapping.y0; y < mapping.y1; y++) {
               ported.m_84988_(x + mapping.offsetX, y + mapping.offsetY, image.m_84985_(mapping.flip ? mapping.x1 - (x - mapping.x0) - 1 : x, y));
            }
         }
      }

      image.close();
      return ported;
   }

   public static boolean isSlimFormat(NativeImage image) {
      return image.m_85087_(54, 25) == 0;
   }

   public static void convertSlimToDefault(NativeImage image) {
      stretch(image, 40, 20);
      stretch(image, 40, 36);
      stretch(image, 32, 52);
      stretch(image, 48, 52);
   }

   private static void stretch(NativeImage image, int offsetX, int offsetY) {
      int target = offsetX + 16 - 1;
      int original = offsetX + 14 - 1;

      for (int p = 0; p < 12; p++) {
         for (int y = 0; y < 12; y++) {
            image.m_84988_(target, offsetY + y, image.m_84985_(original, offsetY + y));
         }

         target--;
         if (p != 6 && p != 9) {
            original--;
         }
      }

      offsetY -= 4;
      target = offsetX + 16 - 1 - 4;
      original = offsetX + 14 - 1 - 4;

      for (int p = 0; p < 8; p++) {
         for (int y = 0; y < 4; y++) {
            image.m_84988_(target, offsetY + y, image.m_84985_(original, offsetY + y));
         }

         target--;
         if (p != 1 && p != 5) {
            original--;
         }
      }
   }

   private record UVMapping(int x0, int y0, int x1, int y1, int offsetX, int offsetY, boolean flip) {
   }
}
