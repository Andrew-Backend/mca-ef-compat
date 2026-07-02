package fabric.net.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.Chore;
import fabric.net.mca.util.InventoryUtils;
import java.util.Comparator;
import net.minecraft.class_1321;
import net.minecraft.class_1429;
import net.minecraft.class_1542;
import net.minecraft.class_1799;
import net.minecraft.class_1829;
import net.minecraft.class_3218;
import net.minecraft.class_4140;
import net.minecraft.class_4141;

public class HuntingTask extends AbstractChoreTask {
   private int ticks = 0;
   private int nextAction = 0;
   private class_1429 target = null;

   public HuntingTask() {
      super(ImmutableMap.of(class_4140.field_18446, class_4141.field_18457, class_4140.field_18445, class_4141.field_18457));
   }

   @Override
   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      return villager.getVillagerBrain().getCurrentJob() == Chore.HUNT && super.shouldRun(world, villager);
   }

   protected boolean shouldKeepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void finishRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      class_1799 stack = villager.method_5998(villager.getDominantHand());
      if (!stack.method_7960()) {
         villager.method_6122(villager.getDominantHand(), class_1799.field_8037);
      }
   }

   @Override
   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      super.run(world, villager, time);
      if (!villager.method_6084(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.method_35199(), stackx -> stackx.method_7909() instanceof class_1829);
         if (i == -1) {
            this.abandonJobWithMessage("chore.hunting.nosword");
         } else {
            class_1799 stack = villager.method_35199().method_5438(i);
            villager.method_6122(villager.getDominantHand(), stack);
         }
      }
   }

   @Override
   protected void keepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      super.keepRunning(world, villager, time);
      if (!InventoryUtils.contains(villager.method_35199(), class_1829.class) && !villager.method_6084(villager.getDominantSlot())) {
         this.abandonJobWithMessage("chore.hunting.nosword");
      } else if (!villager.method_6084(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.method_35199(), stackx -> stackx.method_7909() instanceof class_1829);
         class_1799 stack = villager.method_35199().method_5438(i);
         villager.method_6122(villager.getDominantHand(), stack);
      }

      if (this.target == null) {
         this.ticks++;
         if (this.ticks >= this.nextAction) {
            this.ticks = 0;
            if (villager.method_37908().field_9229.method_43057() >= 0.0) {
               villager.method_37908()
                  .method_18467(class_1429.class, villager.method_5829().method_1009(15.0, 3.0, 15.0))
                  .stream()
                  .filter(a -> !(a instanceof class_1321))
                  .filter(a -> !a.method_6109())
                  .min(Comparator.comparingDouble(villager::method_5858))
                  .ifPresent(animal -> {
                     this.target = animal;
                     villager.moveTowards(this.target.method_24515(), 1.0F);
                  });
            }

            this.nextAction = 50;
            if (this.target == null) {
               this.failedTicks = 100;
            }
         }
      } else {
         villager.moveTowards(this.target.method_24515());
         if (this.target.method_29504()) {
            villager.method_37908().method_18467(class_1542.class, villager.method_5829().method_1009(15.0, 3.0, 15.0)).forEach(item -> {
               villager.method_35199().method_5491(item.method_6983());
               item.method_31472();
            });
            this.target = null;
         } else if (villager.method_5858(this.target) <= 12.25) {
            villager.moveTowards(this.target.method_24515());
            villager.method_6104(villager.getDominantHand());
            this.target.method_5643(world.method_48963().method_48812(villager), 6.0F);
            villager.method_6047().method_7956(1, villager, e -> e.method_20235(e.getDominantSlot()));
         }
      }
   }
}
