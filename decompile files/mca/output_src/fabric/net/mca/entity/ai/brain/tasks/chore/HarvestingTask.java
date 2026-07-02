package fabric.net.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import fabric.net.mca.entity.VillagerEntityMCA;
import fabric.net.mca.entity.ai.Chore;
import fabric.net.mca.entity.ai.TaskUtils;
import fabric.net.mca.util.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.class_1263;
import net.minecraft.class_173;
import net.minecraft.class_1747;
import net.minecraft.class_1752;
import net.minecraft.class_1794;
import net.minecraft.class_1799;
import net.minecraft.class_181;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2302;
import net.minecraft.class_2338;
import net.minecraft.class_2344;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2511;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3965;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_8567.class_8568;

public class HarvestingTask extends AbstractChoreTask {
   private static final int ITEM_READY = 0;
   private static final int ITEM_FOUND = 1;
   private static final int ITEM_MISSING = 2;
   private final List<class_2338> plantable = new ArrayList<>();
   private final List<class_2338> harvestable = new ArrayList<>();
   private final List<class_2338> bonemealable = new ArrayList<>();
   private int lastLandScan = -1200;
   private int lastCropScan = -1200;
   private int workingTick = 0;
   private class_2338 currentPos;

   public HarvestingTask() {
      super(ImmutableMap.of(class_4140.field_18446, class_4141.field_18457, class_4140.field_18445, class_4141.field_18457));
   }

   @Override
   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      return villager.getVillagerBrain().getCurrentJob() == Chore.HARVEST && super.shouldRun(world, villager);
   }

   protected boolean shouldKeepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      return this.currentPos != null && this.shouldRun(world, villager);
   }

   protected void finishRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      class_1799 stack = villager.method_5998(villager.getDominantHand());
      if (!stack.method_7960()) {
         villager.method_6122(villager.getDominantHand(), class_1799.field_8037);
      }

      if (this.currentPos != null) {
         this.plantable.remove(this.currentPos);
         this.harvestable.remove(this.currentPos);
         this.bonemealable.remove(this.currentPos);
         this.currentPos = null;
      }
   }

   @Override
   protected void run(class_3218 world, VillagerEntityMCA villager, long time) {
      super.run(world, villager, time);
      if (!villager.method_6084(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.method_35199(), stackx -> stackx.method_7909() instanceof class_1794);
         if (i == -1) {
            this.abandonJobWithMessage("chore.harvesting.nohoe");
         } else {
            class_1799 stack = villager.method_35199().method_5438(i);
            villager.method_6122(villager.getDominantHand(), stack);
         }
      }

      if (this.villager == null) {
         this.villager = villager;
      }

      if (!InventoryUtils.contains(villager.method_35199(), class_1794.class) && !villager.method_6084(villager.getDominantSlot())) {
         this.abandonJobWithMessage("chore.harvesting.nohoe");
      } else if (!villager.method_6084(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.method_35199(), stackx -> stackx.method_7909() instanceof class_1794);
         class_1799 stack = villager.method_35199().method_5438(i);
         villager.method_6122(villager.getDominantHand(), stack);
      }

      if (this.plantable.isEmpty() && villager.field_6012 - this.lastLandScan > 1200) {
         this.searchUnusedFarmLand(32, 8);
         this.lastLandScan = villager.field_6012;
      }

      if ((this.harvestable.isEmpty() || this.bonemealable.isEmpty()) && villager.field_6012 - this.lastCropScan > 1207) {
         this.searchCrop(32, 8);
         this.lastCropScan = villager.field_6012;
      }

      this.currentPos = TaskUtils.getNearestPoint(villager.method_24515(), this.plantable);
      if (this.currentPos == null) {
         this.currentPos = TaskUtils.getNearestPoint(villager.method_24515(), this.harvestable);
         if (this.currentPos == null) {
            this.currentPos = TaskUtils.getNearestPoint(villager.method_24515(), this.bonemealable);
            if (this.currentPos != null) {
               this.swapItem(stackx -> stackx.method_7909() instanceof class_1752);
            }
         }
      }
   }

   private boolean isValidFarmland(class_2338 pos) {
      class_2680 state = this.villager.method_37908().method_8320(pos);
      return state.method_26204() instanceof class_2344
         && state.method_26184(this.villager.method_37908(), pos)
         && this.villager.method_37908().method_8320(pos.method_10084()).method_26215();
   }

   private boolean isValidMature(class_2338 pos) {
      class_2680 state = this.villager.method_37908().method_8320(pos);
      return state.method_26204() instanceof class_2302 crop && crop.method_9825(state) || state.method_26204() instanceof class_2511;
   }

   private boolean isValidImmature(class_2338 pos) {
      class_2680 state = this.villager.method_37908().method_8320(pos);
      return state.method_26204() instanceof class_2302 crop && !crop.method_9825(state);
   }

   private void searchCrop(int rangeX, int rangeY) {
      List<class_2338> nearbyCrops = TaskUtils.getNearbyBlocks(
         this.villager.method_24515(),
         this.villager.method_37908(),
         blockState -> blockState.method_26204() instanceof class_2302 || blockState.method_26204() instanceof class_2511,
         rangeX,
         rangeY
      );
      this.harvestable.addAll(nearbyCrops.stream().filter(this::isValidMature).toList());
      if (this.hasBoneMeal()) {
         this.bonemealable.addAll(nearbyCrops.stream().filter(this::isValidImmature).toList());
      } else {
         this.bonemealable.clear();
      }
   }

   private boolean hasBoneMeal() {
      return InventoryUtils.contains(this.villager.method_35199(), class_1752.class);
   }

   private void searchUnusedFarmLand(int rangeX, int rangeY) {
      this.plantable
         .addAll(
            TaskUtils.getNearbyBlocks(
                  this.villager.method_24515(), this.villager.method_37908(), blockState -> blockState.method_27852(class_2246.field_10362), rangeX, rangeY
               )
               .stream()
               .filter(this::isValidFarmland)
               .toList()
         );
   }

   @Override
   protected void keepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      villager.moveTowards(this.currentPos);
      if (villager.method_5707(class_243.method_24955(this.currentPos)) <= 6.0) {
         this.workingTick++;
         if (this.workingTick % 5 == 0) {
            villager.method_6104(villager.getDominantHand());
         }

         if (this.workingTick > 40) {
            this.plantable.remove(this.currentPos);
            this.harvestable.remove(this.currentPos);
            this.bonemealable.remove(this.currentPos);
            if (this.isValidFarmland(this.currentPos)) {
               this.plantSeeds(world, villager, this.currentPos.method_10084(), null);
            } else if (this.isValidMature(this.currentPos)) {
               class_2680 state = world.method_8320(this.currentPos);
               if (state.method_26204() instanceof class_2511) {
                  this.harvestCrops(world, this.currentPos);
               } else {
                  this.harvestCrops(world, this.currentPos);
                  this.plantSeeds(world, villager, this.currentPos, state.method_26204());
               }
            } else if (this.isValidImmature(this.currentPos)) {
               this.bonemealCrop(world, villager, this.currentPos);
               if (!this.hasBoneMeal()) {
                  this.bonemealable.clear();
               }
            }

            this.workingTick = 0;
            this.currentPos = null;
         }
      }
   }

   private int swapItem(Predicate<class_1799> find) {
      class_1799 stack = this.villager.method_6047();
      if (find.test(stack)) {
         return 0;
      }

      class_1263 inventory = this.villager.method_35199();
      int slot = InventoryUtils.getFirstSlotContainingItem(inventory, find);
      if (slot < 0) {
         return 2;
      }

      this.villager.method_6122(this.villager.getDominantHand(), inventory.method_5438(slot));
      return 1;
   }

   private void plantSeeds(class_3218 world, VillagerEntityMCA villager, class_2338 target, class_2248 block) {
      class_3965 hitResult = new class_3965(class_243.method_24955(target), class_2350.field_11033, target, true);
      Optional<class_1799> stack = InventoryUtils.stream(villager.method_35199())
         .filter(s -> !s.method_7960() && s.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() == block)
         .findAny();
      if (stack.isEmpty()) {
         stack = InventoryUtils.stream(villager.method_35199())
            .filter(s -> !s.method_7960() && s.method_7909() instanceof class_1747 blockItem && blockItem.method_7711() instanceof class_2302)
            .findAny();
      }

      stack.ifPresentOrElse(s -> {
         world.method_8652(hitResult.method_17777(), ((class_1747)s.method_7909()).method_7711().method_9564(), 3);
         s.method_7934(1);
         villager.method_6104(villager.getDominantHand());
         this.bonemealable.add(target);
      }, () -> this.getAssigningPlayer().ifPresent(p -> villager.sendChatMessage(p, "chore.harvesting.noseed")));
   }

   private void bonemealCrop(class_3218 world, VillagerEntityMCA villager, class_2338 pos) {
      if (this.swapItem(stack -> stack.method_7909() instanceof class_1752) == 0
         && class_1752.method_7720(villager.method_6118(villager.getDominantSlot()), world, pos)) {
         villager.method_6104(villager.getDominantHand());
      }
   }

   private void harvestCrops(class_3218 world, class_2338 pos) {
      class_2680 state = world.method_8320(pos);
      if (world.method_8651(pos, false, this.villager)) {
         class_8568 builder = new class_8568(world)
            .method_51874(class_181.field_24424, this.villager.method_19538())
            .method_51874(class_181.field_1229, class_1799.field_8037)
            .method_51874(class_181.field_1226, this.villager)
            .method_51874(class_181.field_1224, state)
            .method_51871(0.0F);

         for (class_1799 stack : world.method_8503()
            .method_3857()
            .getLootTable(state.method_26204().method_26162())
            .method_51878(builder.method_51875(class_173.field_1172))) {
            this.villager.method_35199().method_5491(stack);
         }
      }
   }
}
