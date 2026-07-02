package quilt.net.mca.client.resources;

import java.util.function.BiConsumer;

public class ClientUtils {
   public static void bethlehemLine(int x0, int y0, int x1, int y1, BiConsumer<Integer, Integer> callback) {
      int dx = Math.abs(x1 - x0);
      int dy = Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx - dy;
      int x = x0;
      int y = y0;

      while (true) {
         callback.accept(x, y);
         if (x == x1 && y == y1) {
            return;
         }

         int e2 = 2 * err;
         if (e2 > -dy) {
            err -= dy;
            x += sx;
         }

         if (e2 < dx) {
            err += dx;
            y += sy;
         }
      }
   }

   public static double[] RGB2HSV(double red, double green, double blue) {
      double cmax = Math.max(red, Math.max(green, blue));
      double cmin = Math.min(red, Math.min(green, blue));
      double delta = cmax - cmin;
      double h;
      if (delta == 0.0) {
         h = 0.0;
      } else if (cmax == red) {
         h = (green - blue) / delta % 6.0;
      } else if (cmax == green) {
         h = (blue - red) / delta + 2.0;
      } else {
         h = (red - green) / delta + 4.0;
      }

      h *= 60.0;
      if (h < 0.0) {
         h += 360.0;
      }

      double s;
      if (cmax == 0.0) {
         s = 0.0;
      } else {
         s = delta / cmax;
      }

      double v = cmax;
      return new double[]{h, s, v};
   }

   public static double[] HSV2RGB(double hue, double saturation, double brightness) {
      double c = brightness * saturation;
      double x = c * (1.0 - Math.abs(hue / 60.0 % 2.0 - 1.0));
      double m = brightness - c;
      double r;
      double g;
      double b;
      if (hue >= 0.0 && hue < 60.0) {
         r = c;
         g = x;
         b = 0.0;
      } else if (hue >= 60.0 && hue < 120.0) {
         r = x;
         g = c;
         b = 0.0;
      } else if (hue >= 120.0 && hue < 180.0) {
         r = 0.0;
         g = c;
         b = x;
      } else if (hue >= 180.0 && hue < 240.0) {
         r = 0.0;
         g = x;
         b = c;
      } else if (hue >= 240.0 && hue < 300.0) {
         r = x;
         g = 0.0;
         b = c;
      } else {
         r = c;
         g = 0.0;
         b = x;
      }

      return new double[]{r + m, g + m, b + m};
   }
}
