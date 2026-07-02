package forge.net.mca.client.gui.widget;

import forge.net.mca.util.compat.ButtonWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TooltipButtonWidget extends ButtonWidget {
   public TooltipButtonWidget(int x, int y, int width, int height, String message, OnPress onPress) {
      super(x, y, width, height, Component.m_237115_(message), onPress, Component.m_237115_(message + ".tooltip"));
   }

   public TooltipButtonWidget(int x, int y, int width, int height, MutableComponent message, MutableComponent tooltip, OnPress onPress) {
      super(x, y, width, height, message, onPress, tooltip);
   }

   public void setMessage(String message) {
      super.m_93666_(Component.m_237115_(message));
      super.m_257544_(Tooltip.m_257550_(Component.m_237115_(message + ".tooltip")));
   }
}
