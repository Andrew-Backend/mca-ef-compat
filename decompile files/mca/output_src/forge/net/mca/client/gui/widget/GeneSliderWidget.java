package forge.net.mca.client.gui.widget;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class GeneSliderWidget extends AbstractSliderButton {
   private final Consumer<Double> callback;

   public GeneSliderWidget(int x, int y, int width, int height, Component text, double value, Consumer<Double> callback) {
      super(x, y, width, height, text, value);
      this.m_5695_();
      this.callback = callback;
   }

   protected void m_5697_() {
      this.callback.accept(this.f_93577_);
   }

   protected void m_5695_() {
   }
}
