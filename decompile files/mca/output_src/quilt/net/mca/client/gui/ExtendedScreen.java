package quilt.net.mca.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.minecraft.class_5481;

public class ExtendedScreen extends class_437 {
   protected ExtendedScreen(class_2561 title) {
      super(title);
   }

   public int getTooltipWidth(List<class_2561> lines_) {
      List<? extends class_5481> lines = Lists.transform(lines_, class_2561::method_30937);
      int w = 0;
      if (!lines.isEmpty()) {
         for (class_5481 orderedText : lines) {
            int j = this.field_22793.method_30880(orderedText);
            if (j > w) {
               w = j;
            }
         }
      }

      return w;
   }

   public int getTooltipHeight(List<class_2561> lines_) {
      List<? extends class_5481> lines = Lists.transform(lines_, class_2561::method_30937);
      int h = 8;
      if (!lines.isEmpty() && lines.size() > 1) {
         h += 2 + (lines.size() - 1) * 10;
      }

      return h;
   }
}
