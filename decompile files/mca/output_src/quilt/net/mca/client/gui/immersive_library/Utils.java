package quilt.net.mca.client.gui.immersive_library;

import net.minecraft.class_1011;
import net.minecraft.class_3532;
import quilt.net.mca.client.resources.SkinLocations;

public class Utils {
   public static boolean verify(class_1011 image) {
      int errors = 0;

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            if (SkinLocations.SKIN_LOOKUP[x][y] && image.method_4311(x, y) == 0) {
               if (++errors > 6) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean verifyHair(class_1011 image) {
      int errors = 0;
      int pixels = 0;
      double brightness = 0.0;

      for (int x = 0; x < 64; x++) {
         for (int y = 0; y < 64; y++) {
            int r = image.method_35623(x, y) & 255;
            int g = image.method_35625(x, y) & 255;
            int b = image.method_35626(x, y) & 255;
            int a = image.method_4311(x, y) & 255;
            if (a > 0) {
               int l = class_3532.method_15340((int)(0.2126 * r + 0.7152 * g + 0.0722 * b), 0, 255);
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
