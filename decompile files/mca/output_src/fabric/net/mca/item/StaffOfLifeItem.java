package fabric.net.mca.item;

import java.util.List;
import net.minecraft.class_1269;
import net.minecraft.class_1799;
import net.minecraft.class_1814;
import net.minecraft.class_1836;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_1792.class_1793;
import org.jetbrains.annotations.Nullable;

public class StaffOfLifeItem extends TooltippedItem {
   public StaffOfLifeItem(class_1793 properties) {
      super(properties);
   }

   public class_1269 method_7884(class_1838 context) {
      class_1269 result = ScytheItem.use(context, true);
      if (result == class_1269.field_5812) {
         context.method_8041().method_7956(1, context.method_8036(), x -> {});
         return result;
      } else {
         return result;
      }
   }

   @Override
   public void method_7851(class_1799 stack, @Nullable class_1937 world, List<class_2561> tooltip, class_1836 context) {
      tooltip.add(class_2561.method_43469(this.method_7866(stack) + ".uses", new Object[]{stack.method_7936() - stack.method_7919()}));
      tooltip.add(class_2561.method_43470(""));
      super.method_7851(stack, world, tooltip, context);
   }

   public boolean method_7886(class_1799 stack) {
      return true;
   }

   public class_1814 method_7862(class_1799 stack) {
      return class_1814.field_8903;
   }

   public boolean method_7870(class_1799 stack) {
      return false;
   }
}
