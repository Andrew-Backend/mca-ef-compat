package fabric.net.mca.advancement.criterion;

import fabric.net.mca.mixin.MixinCriteria;
import net.minecraft.class_179;

public interface CriterionMCA {
   BabyCriterion BABY_CRITERION = register(new BabyCriterion());
   BabyDroppedCriterion BABY_DROPPED_CRITERION = register(new BabyDroppedCriterion());
   BabySmeltedCriterion BABY_SMELTED_CRITERION = register(new BabySmeltedCriterion());
   BabySirbenSmeltedCriterion BABY_SIRBEN_SMELTED_CRITERION = register(new BabySirbenSmeltedCriterion());
   HeartsCriterion HEARTS_CRITERION = register(new HeartsCriterion());
   GenericEventCriterion GENERIC_EVENT_CRITERION = register(new GenericEventCriterion());
   ChildAgeStateChangeCriterion CHILD_AGE_STATE_CHANGE = register(new ChildAgeStateChangeCriterion());
   FamilyCriterion FAMILY = register(new FamilyCriterion());
   RankCriterion RANK = register(new RankCriterion());
   VillagerFateCriterion FATE = register(new VillagerFateCriterion());

   static <T extends class_179<?>> T register(T obj) {
      return MixinCriteria.register(obj);
   }

   static void bootstrap() {
   }
}
