package forge.net.mca.util.compat;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

public class ButtonWidget extends Button {
   public ButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress) {
      super(x, y, width, height, message, onPress, f_252438_);
   }

   public ButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress, Component tooltip) {
      this(x, y, width, height, message, onPress);
      this.m_257544_(Tooltip.m_257550_(tooltip));
   }
}
