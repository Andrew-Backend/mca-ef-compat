package forge.net.mca.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class ExtendedScreen extends Screen {
   protected ExtendedScreen(Component title) {
      super(title);
   }

   public int getTooltipWidth(List<Component> lines_) {
      List<? extends FormattedCharSequence> lines = Lists.transform(lines_, Component::m_7532_);
      int w = 0;
      if (!lines.isEmpty()) {
         for (FormattedCharSequence orderedText : lines) {
            int j = this.f_96547_.m_92724_(orderedText);
            if (j > w) {
               w = j;
            }
         }
      }

      return w;
   }

   public int getTooltipHeight(List<Component> lines_) {
      List<? extends FormattedCharSequence> lines = Lists.transform(lines_, Component::m_7532_);
      int h = 8;
      if (!lines.isEmpty() && lines.size() > 1) {
         h += 2 + (lines.size() - 1) * 10;
      }

      return h;
   }
}
