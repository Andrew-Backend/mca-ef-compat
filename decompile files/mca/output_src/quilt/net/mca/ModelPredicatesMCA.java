package quilt.net.mca;

import net.minecraft.class_1792;
import net.minecraft.class_2960;
import net.minecraft.class_6395;
import net.minecraft.class_7391;
import quilt.net.mca.item.BabyItem;
import quilt.net.mca.item.ItemsMCA;
import quilt.net.mca.item.SirbenBabyItem;
import quilt.net.mca.item.VillagerTrackerItem;
import quilt.net.mca.util.network.datasync.CDataParameter;

public interface ModelPredicatesMCA {
   static void setup(CDataParameter.TriConsumer<class_1792, class_2960, class_6395> register) {
      register.accept(
         (class_1792)ItemsMCA.BABY_BOY.get(), new class_2960("invalidated"), (stack, world, entity, i) -> BabyItem.hasBeenInvalidated(stack) ? 1.0F : 0.0F
      );
      register.accept(
         (class_1792)ItemsMCA.BABY_GIRL.get(), new class_2960("invalidated"), (stack, world, entity, i) -> BabyItem.hasBeenInvalidated(stack) ? 1.0F : 0.0F
      );
      register.accept(
         (class_1792)ItemsMCA.SIRBEN_BABY_BOY.get(),
         new class_2960("invalidated"),
         (stack, world, entity, i) -> SirbenBabyItem.hasBeenInvalidated(stack) ? 1.0F : 0.0F
      );
      register.accept(
         (class_1792)ItemsMCA.SIRBEN_BABY_GIRL.get(),
         new class_2960("invalidated"),
         (stack, world, entity, i) -> SirbenBabyItem.hasBeenInvalidated(stack) ? 1.0F : 0.0F
      );
      register.accept(
         (class_1792)ItemsMCA.VILLAGER_TRACKER.get(),
         new class_2960("angle"),
         new class_7391((world, stack, entity) -> VillagerTrackerItem.getTargetPos(stack))
      );
   }
}
