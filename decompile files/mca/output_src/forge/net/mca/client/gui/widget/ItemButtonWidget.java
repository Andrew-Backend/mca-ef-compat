package forge.net.mca.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ItemButtonWidget extends TooltipButtonWidget {
   final ItemStack item;

   public ItemButtonWidget(int x, int y, int size, MutableComponent message, ItemStack item, OnPress onPress) {
      super(x, y, size, size, Component.m_237113_(""), message, onPress);
      this.item = item;
   }

   public void m_87963_(GuiGraphics context, int mouseX, int mouseY, float delta) {
      super.m_87963_(context, mouseX, mouseY, delta);
      int size = 16;
      context.m_280480_(this.item, this.m_252754_() + (this.f_93618_ - size) / 2, this.m_252907_() + (this.f_93619_ - size) / 2);
   }
}
