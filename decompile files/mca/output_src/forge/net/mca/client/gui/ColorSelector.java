package forge.net.mca.client.gui;

import forge.net.mca.client.gui.widget.HorizontalColorPickerWidget;
import forge.net.mca.client.resources.ClientUtils;

class ColorSelector {
   double red;
   double green;
   double blue;
   double hue;
   double saturation;
   double brightness;
   public HorizontalColorPickerWidget hueWidget;
   public HorizontalColorPickerWidget saturationWidget;
   public HorizontalColorPickerWidget brightnessWidget;

   public ColorSelector() {
      this.setHSV(0.5, 0.5, 0.5);
   }

   public void setRGB(double red, double green, double blue) {
      this.red = red;
      this.green = green;
      this.blue = blue;
      this.updateHSV();
   }

   public void setHSV(double hue, double saturation, double brightness) {
      this.hue = hue;
      this.saturation = saturation;
      this.brightness = brightness;
      this.updateRGB();
      if (this.hueWidget != null) {
         this.hueWidget.setValueX(hue / 360.0);
         this.saturationWidget.setValueX(saturation);
      }

      if (this.brightnessWidget != null) {
         this.brightnessWidget.setValueX(brightness);
      }
   }

   private void updateRGB() {
      double[] doubles = ClientUtils.HSV2RGB(this.hue, this.saturation, this.brightness);
      this.red = doubles[0];
      this.green = doubles[1];
      this.blue = doubles[2];
   }

   private void updateHSV() {
      double[] doubles = ClientUtils.RGB2HSV(this.red, this.green, this.blue);
      this.hue = doubles[0];
      this.saturation = doubles[1];
      this.brightness = doubles[2];
      if (this.hueWidget != null) {
         this.hueWidget.setValueX(this.hue / 360.0);
         this.saturationWidget.setValueX(this.saturation);
      }

      if (this.brightnessWidget != null) {
         this.brightnessWidget.setValueX(this.brightness);
      }
   }

   public int getRed() {
      return (int)(this.red * 255.0);
   }

   public int getGreen() {
      return (int)(this.green * 255.0);
   }

   public int getBlue() {
      return (int)(this.blue * 255.0);
   }

   public int getColor() {
      return 0xFF000000 | this.getBlue() << 16 | this.getGreen() << 8 | this.getRed();
   }
}
