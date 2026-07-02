package fabric.net.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.Chore;
import fabric.net.mca.entity.ai.TaskUtils;
import fabric.net.mca.util.InventoryUtils;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_173;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_181;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import net.minecraft.class_39;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_52;
import net.minecraft.class_8567.class_8568;

public class FishingTask extends AbstractChoreTask {
   private class_2338 targetWater;
   private boolean hasCastRod;
   private int ticks;
   private List<class_1799> list;

   public FishingTask() {
      super(ImmutableMap.of(class_4140.field_18446, class_4141.field_18457, class_4140.field_18445, class_4141.field_18457));
   }

   @Override
   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      return villager.getVillagerBrain().getCurrentJob() == Chore.FISH && super.shouldRun(world, villager);
   }

   protected boolean shouldKeepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   @Override
   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      super.run(world, villager, time);
      if (!villager.method_6084(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.method_35199(), stack -> stack.method_7909() instanceof class_1787);
         if (i == -1) {
            this.abandonJobWithMessage("chore.fishing.norod");
         } else {
            villager.method_6122(villager.getDominantHand(), villager.method_35199().method_5438(i));
         }
      }

      class_52 loottable = world.method_8503().method_3857().getLootTable(class_39.field_353);
      class_8568 lootcontext$builder = new class_8568(world)
         .method_51874(class_181.field_24424, villager.method_19538())
         .method_51874(class_181.field_1229, new class_1799(class_1802.field_8378))
         .method_51874(class_181.field_1226, villager)
         .method_51871(0.0F);
      this.list = loottable.method_51878(lootcontext$builder.method_51875(class_173.field_1176));
   }

   @Override
   protected void keepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      super.keepRunning(world, villager, time);
      if (!InventoryUtils.contains(villager.method_35199(), class_1787.class) && !villager.method_6084(villager.getDominantSlot())) {
         this.abandonJobWithMessage("chore.fishing.norod");
      } else if (!villager.method_6084(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.method_35199(), stackx -> stackx.method_7909() instanceof class_1787);
         class_1799 stack = villager.method_35199().method_5438(i);
         villager.method_6122(villager.getDominantHand(), stack);
      }

      if (this.targetWater == null) {
         List<class_2338> nearbyStaticLiquid = TaskUtils.getNearbyBlocks(
            villager.method_24515(), villager.method_37908(), blockState -> blockState.method_27852(class_2246.field_10382), 12, 3
         );
         this.targetWater = nearbyStaticLiquid.stream()
            .filter(p -> villager.method_37908().method_8320(p).method_26204() == class_2246.field_10382)
            .min(Comparator.comparingDouble(d -> villager.method_5649(d.method_10263(), d.method_10264(), d.method_10260())))
            .orElse(null);
         if (this.targetWater == null) {
            this.failedTicks = 100;
         }
      } else if (villager.method_5649(this.targetWater.method_10263(), this.targetWater.method_10264(), this.targetWater.method_10260()) < 5.0) {
         villager.method_5942().method_6340();
         villager.lookAt(this.targetWater);
         if (!this.hasCastRod) {
            villager.method_6104(villager.getDominantHand());
            this.hasCastRod = true;
         }

         this.ticks++;
         if (this.ticks >= villager.method_37908().field_9229.method_43048(200) + 200) {
            if (villager.method_37908().field_9229.method_43057() >= 0.35F) {
               class_1799 stack = this.list.get(villager.method_6051().method_43048(this.list.size())).method_7972();
               villager.method_6104(villager.getDominantHand());
               villager.method_35199().method_5491(stack);
               villager.method_6047().method_7956(1, villager, e -> e.method_20235(e.getDominantSlot()));
            }

            this.ticks = 0;
         }
      } else {
         villager.moveTowards(this.targetWater);
      }
   }

   protected void finishRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      class_1799 stack = villager.method_5998(villager.getDominantHand());
      if (!stack.method_7960()) {
         villager.method_6122(villager.getDominantHand(), class_1799.field_8037);
      }
   }
}
