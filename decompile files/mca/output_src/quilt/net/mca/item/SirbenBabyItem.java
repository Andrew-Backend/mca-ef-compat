package quilt.net.mca.item;

import net.minecraft.class_1799;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_1792.class_1793;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Traits;
import quilt.net.mca.entity.ai.relationship.Gender;

public class SirbenBabyItem extends BabyItem {
   public SirbenBabyItem(Gender gender, class_1793 properties) {
      super(gender, properties);
   }

   public boolean method_7886(class_1799 stack) {
      return true;
   }

   @Override
   protected VillagerEntityMCA birthChild(class_1799 stack, class_3218 world, class_3222 player) {
      VillagerEntityMCA child = super.birthChild(stack, world, player);
      child.getTraits().addTrait(Traits.SIRBEN);
      return child;
   }
}
