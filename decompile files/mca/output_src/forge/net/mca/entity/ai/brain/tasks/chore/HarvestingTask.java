package forge.net.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Chore;
import forge.net.mca.entity.ai.TaskUtils;
import forge.net.mca.util.InventoryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.StemGrownBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class HarvestingTask extends AbstractChoreTask {
   private static final int ITEM_READY = 0;
   private static final int ITEM_FOUND = 1;
   private static final int ITEM_MISSING = 2;
   private final List<BlockPos> plantable = new ArrayList<>();
   private final List<BlockPos> harvestable = new ArrayList<>();
   private final List<BlockPos> bonemealable = new ArrayList<>();
   private int lastLandScan = -1200;
   private int lastCropScan = -1200;
   private int workingTick = 0;
   private BlockPos currentPos;

   public HarvestingTask() {
      super(ImmutableMap.of(MemoryModuleType.f_26371_, MemoryStatus.VALUE_ABSENT, MemoryModuleType.f_26370_, MemoryStatus.VALUE_ABSENT));
   }

   @Override
   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      return villager.getVillagerBrain().getCurrentJob() == Chore.HARVEST && super.shouldRun(world, villager);
   }

   protected boolean shouldKeepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      return this.currentPos != null && this.shouldRun(world, villager);
   }

   protected void finishRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      ItemStack stack = villager.m_21120_(villager.getDominantHand());
      if (!stack.m_41619_()) {
         villager.m_21008_(villager.getDominantHand(), ItemStack.f_41583_);
      }

      if (this.currentPos != null) {
         this.plantable.remove(this.currentPos);
         this.harvestable.remove(this.currentPos);
         this.bonemealable.remove(this.currentPos);
         this.currentPos = null;
      }
   }

   @Override
   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      super.run(world, villager, time);
      if (!villager.m_21033_(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.m_35311_(), stackx -> stackx.m_41720_() instanceof HoeItem);
         if (i == -1) {
            this.abandonJobWithMessage("chore.harvesting.nohoe");
         } else {
            ItemStack stack = villager.m_35311_().m_8020_(i);
            villager.m_21008_(villager.getDominantHand(), stack);
         }
      }

      if (this.villager == null) {
         this.villager = villager;
      }

      if (!InventoryUtils.contains(villager.m_35311_(), HoeItem.class) && !villager.m_21033_(villager.getDominantSlot())) {
         this.abandonJobWithMessage("chore.harvesting.nohoe");
      } else if (!villager.m_21033_(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.m_35311_(), stackx -> stackx.m_41720_() instanceof HoeItem);
         ItemStack stack = villager.m_35311_().m_8020_(i);
         villager.m_21008_(villager.getDominantHand(), stack);
      }

      if (this.plantable.isEmpty() && villager.f_19797_ - this.lastLandScan > 1200) {
         this.searchUnusedFarmLand(32, 8);
         this.lastLandScan = villager.f_19797_;
      }

      if ((this.harvestable.isEmpty() || this.bonemealable.isEmpty()) && villager.f_19797_ - this.lastCropScan > 1207) {
         this.searchCrop(32, 8);
         this.lastCropScan = villager.f_19797_;
      }

      this.currentPos = TaskUtils.getNearestPoint(villager.m_20183_(), this.plantable);
      if (this.currentPos == null) {
         this.currentPos = TaskUtils.getNearestPoint(villager.m_20183_(), this.harvestable);
         if (this.currentPos == null) {
            this.currentPos = TaskUtils.getNearestPoint(villager.m_20183_(), this.bonemealable);
            if (this.currentPos != null) {
               this.swapItem(stackx -> stackx.m_41720_() instanceof BoneMealItem);
            }
         }
      }
   }

   private boolean isValidFarmland(BlockPos pos) {
      BlockState state = this.villager.m_9236_().m_8055_(pos);
      return state.m_60734_() instanceof FarmBlock && state.m_60710_(this.villager.m_9236_(), pos) && this.villager.m_9236_().m_8055_(pos.m_7494_()).m_60795_();
   }

   private boolean isValidMature(BlockPos pos) {
      BlockState state = this.villager.m_9236_().m_8055_(pos);
      return state.m_60734_() instanceof CropBlock crop && crop.m_52307_(state) || state.m_60734_() instanceof StemGrownBlock;
   }

   private boolean isValidImmature(BlockPos pos) {
      BlockState state = this.villager.m_9236_().m_8055_(pos);
      return state.m_60734_() instanceof CropBlock crop && !crop.m_52307_(state);
   }

   private void searchCrop(int rangeX, int rangeY) {
      List<BlockPos> nearbyCrops = TaskUtils.getNearbyBlocks(
         this.villager.m_20183_(),
         this.villager.m_9236_(),
         blockState -> blockState.m_60734_() instanceof CropBlock || blockState.m_60734_() instanceof StemGrownBlock,
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
      return InventoryUtils.contains(this.villager.m_35311_(), BoneMealItem.class);
   }

   private void searchUnusedFarmLand(int rangeX, int rangeY) {
      this.plantable
         .addAll(
            TaskUtils.getNearbyBlocks(this.villager.m_20183_(), this.villager.m_9236_(), blockState -> blockState.m_60713_(Blocks.f_50093_), rangeX, rangeY)
               .stream()
               .filter(this::isValidFarmland)
               .toList()
         );
   }

   @Override
   protected void keepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      villager.moveTowards(this.currentPos);
      if (villager.m_20238_(Vec3.m_82539_(this.currentPos)) <= 6.0) {
         this.workingTick++;
         if (this.workingTick % 5 == 0) {
            villager.m_6674_(villager.getDominantHand());
         }

         if (this.workingTick > 40) {
            this.plantable.remove(this.currentPos);
            this.harvestable.remove(this.currentPos);
            this.bonemealable.remove(this.currentPos);
            if (this.isValidFarmland(this.currentPos)) {
               this.plantSeeds(world, villager, this.currentPos.m_7494_(), null);
            } else if (this.isValidMature(this.currentPos)) {
               BlockState state = world.m_8055_(this.currentPos);
               if (state.m_60734_() instanceof StemGrownBlock) {
                  this.harvestCrops(world, this.currentPos);
               } else {
                  this.harvestCrops(world, this.currentPos);
                  this.plantSeeds(world, villager, this.currentPos, state.m_60734_());
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

   private int swapItem(Predicate<ItemStack> find) {
      ItemStack stack = this.villager.m_21205_();
      if (find.test(stack)) {
         return 0;
      }

      Container inventory = this.villager.m_35311_();
      int slot = InventoryUtils.getFirstSlotContainingItem(inventory, find);
      if (slot < 0) {
         return 2;
      }

      this.villager.m_21008_(this.villager.getDominantHand(), inventory.m_8020_(slot));
      return 1;
   }

   private void plantSeeds(ServerLevel world, VillagerEntityMCA villager, BlockPos target, Block block) {
      BlockHitResult hitResult = new BlockHitResult(Vec3.m_82539_(target), Direction.DOWN, target, true);
      Optional<ItemStack> stack = InventoryUtils.stream(villager.m_35311_())
         .filter(s -> !s.m_41619_() && s.m_41720_() instanceof BlockItem blockItem && blockItem.m_40614_() == block)
         .findAny();
      if (stack.isEmpty()) {
         stack = InventoryUtils.stream(villager.m_35311_())
            .filter(s -> !s.m_41619_() && s.m_41720_() instanceof BlockItem blockItem && blockItem.m_40614_() instanceof CropBlock)
            .findAny();
      }

      stack.ifPresentOrElse(s -> {
         world.m_7731_(hitResult.m_82425_(), ((BlockItem)s.m_41720_()).m_40614_().m_49966_(), 3);
         s.m_41774_(1);
         villager.m_6674_(villager.getDominantHand());
         this.bonemealable.add(target);
      }, () -> this.getAssigningPlayer().ifPresent(p -> villager.sendChatMessage(p, "chore.harvesting.noseed")));
   }

   private void bonemealCrop(ServerLevel world, VillagerEntityMCA villager, BlockPos pos) {
      if (this.swapItem(stack -> stack.m_41720_() instanceof BoneMealItem) == 0
         && BoneMealItem.m_40627_(villager.m_6844_(villager.getDominantSlot()), world, pos)) {
         villager.m_6674_(villager.getDominantHand());
      }
   }

   private void harvestCrops(ServerLevel world, BlockPos pos) {
      BlockState state = world.m_8055_(pos);
      if (world.m_46953_(pos, false, this.villager)) {
         Builder builder = new Builder(world)
            .m_287286_(LootContextParams.f_81460_, this.villager.m_20182_())
            .m_287286_(LootContextParams.f_81463_, ItemStack.f_41583_)
            .m_287286_(LootContextParams.f_81455_, this.villager)
            .m_287286_(LootContextParams.f_81461_, state)
            .m_287239_(0.0F);

         for (ItemStack stack : world.m_7654_().m_278653_().m_278676_(state.m_60734_().m_60589_()).m_287195_(builder.m_287235_(LootContextParamSets.f_81421_))) {
            this.villager.m_35311_().m_19173_(stack);
         }
      }
   }
}
