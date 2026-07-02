package quilt.net.mca.item;

import java.util.List;
import net.minecraft.class_124;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_1792.class_1793;
import org.jetbrains.annotations.Nullable;
import quilt.net.mca.util.localization.FlowingText;

public class TooltippedItem extends class_1792 {
   public TooltippedItem(class_1793 properties) {
      super(properties);
   }

   public void method_7851(class_1799 stack, @Nullable class_1937 world, List<class_2561> tooltip, class_1836 context) {
      tooltip.addAll(FlowingText.wrap(class_2561.method_43471(this.method_7866(stack) + ".tooltip").method_27692(class_124.field_1080), 160));
   }
}
