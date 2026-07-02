package forge.net.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Chore;
import forge.net.mca.entity.ai.TaskUtils;
import forge.net.mca.util.InventoryUtils;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingTask extends AbstractChoreTask {
   private BlockPos targetWater;
   private boolean hasCastRod;
   private int ticks;
   private List<ItemStack> list;

   public FishingTask() {
      super(ImmutableMap.of(MemoryModuleType.f_26371_, MemoryStatus.VALUE_ABSENT, MemoryModuleType.f_26370_, MemoryStatus.VALUE_ABSENT));
   }

   @Override
   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      return villager.getVillagerBrain().getCurrentJob() == Chore.FISH && super.shouldRun(world, villager);
   }

   protected boolean shouldKeepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   @Override
   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      super.run(world, villager, time);
      if (!villager.m_21033_(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.m_35311_(), stack -> stack.m_41720_() instanceof FishingRodItem);
         if (i == -1) {
            this.abandonJobWithMessage("chore.fishing.norod");
         } else {
            villager.m_21008_(villager.getDominantHand(), villager.m_35311_().m_8020_(i));
         }
      }

      LootTable loottable = world.m_7654_().m_278653_().m_278676_(BuiltInLootTables.f_78720_);
      Builder lootcontext$builder = new Builder(world)
         .m_287286_(LootContextParams.f_81460_, villager.m_20182_())
         .m_287286_(LootContextParams.f_81463_, new ItemStack(Items.f_42523_))
         .m_287286_(LootContextParams.f_81455_, villager)
         .m_287239_(0.0F);
      this.list = loottable.m_287195_(lootcontext$builder.m_287235_(LootContextParamSets.f_81414_));
   }

   @Override
   protected void keepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      super.keepRunning(world, villager, time);
      if (!InventoryUtils.contains(villager.m_35311_(), FishingRodItem.class) && !villager.m_21033_(villager.getDominantSlot())) {
         this.abandonJobWithMessage("chore.fishing.norod");
      } else if (!villager.m_21033_(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.m_35311_(), stackx -> stackx.m_41720_() instanceof FishingRodItem);
         ItemStack stack = villager.m_35311_().m_8020_(i);
         villager.m_21008_(villager.getDominantHand(), stack);
      }

      if (this.targetWater == null) {
         List<BlockPos> nearbyStaticLiquid = TaskUtils.getNearbyBlocks(
            villager.m_20183_(), villager.m_9236_(), blockState -> blockState.m_60713_(Blocks.f_49990_), 12, 3
         );
         this.targetWater = nearbyStaticLiquid.stream()
            .filter(p -> villager.m_9236_().m_8055_(p).m_60734_() == Blocks.f_49990_)
            .min(Comparator.comparingDouble(d -> villager.m_20275_(d.m_123341_(), d.m_123342_(), d.m_123343_())))
            .orElse(null);
         if (this.targetWater == null) {
            this.failedTicks = 100;
         }
      } else if (villager.m_20275_(this.targetWater.m_123341_(), this.targetWater.m_123342_(), this.targetWater.m_123343_()) < 5.0) {
         villager.m_21573_().m_26573_();
         villager.lookAt(this.targetWater);
         if (!this.hasCastRod) {
            villager.m_6674_(villager.getDominantHand());
            this.hasCastRod = true;
         }

         this.ticks++;
         if (this.ticks >= villager.m_9236_().f_46441_.m_188503_(200) + 200) {
            if (villager.m_9236_().f_46441_.m_188501_() >= 0.35F) {
               ItemStack stack = this.list.get(villager.m_217043_().m_188503_(this.list.size())).m_41777_();
               villager.m_6674_(villager.getDominantHand());
               villager.m_35311_().m_19173_(stack);
               villager.m_21205_().m_41622_(1, villager, e -> e.m_21166_(e.getDominantSlot()));
            }

            this.ticks = 0;
         }
      } else {
         villager.moveTowards(this.targetWater);
      }
   }

   protected void finishRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      ItemStack stack = villager.m_21120_(villager.getDominantHand());
      if (!stack.m_41619_()) {
         villager.m_21008_(villager.getDominantHand(), ItemStack.f_41583_);
      }
   }
}
