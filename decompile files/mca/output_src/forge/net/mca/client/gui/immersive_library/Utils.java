package forge.net.mca.client.gui.immersive_library;

import com.mojang.blaze3d.platform.NativeImage;
import forge.net.mca.client.resources.SkinLocations;
import net.minecraft.util.Mth;

public class Utils {
   public static boolean verify(NativeImage image) {
      int errors = 0;

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            if (SkinLocations.SKIN_LOOKUP[x][y] && image.m_85087_(x, y) == 0) {
               if (++errors > 6) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean verifyHair(NativeImage image) {
      int errors = 0;
      int pixels = 0;
      double brightness = 0.0;

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            int r = image.m_166408_(x, y) & 255;
            int g = image.m_166415_(x, y) & 255;
            int b = image.m_166418_(x, y) & 255;
            int a = image.m_85087_(x, y) & 255;
            if (a > 0) {
               int l = Mth.m_14045_((int)(0.2126 * r + 0.7152 * g + 0.0722 * b), 0, 255);
               brightness += l;
               pixels++;
               errors += Math.abs(r - l);
               errors += Math.abs(g - l);
               errors += Math.abs(b - l);
            }
         }
      }

      brightness /= pixels;
      return errors < pixels && brightness > 160.0;
   }
}
