package forge.net.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;
import forge.net.mca.Config;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Chore;
import forge.net.mca.entity.ai.TaskUtils;
import forge.net.mca.util.InventoryUtils;
import forge.net.mca.util.RegistryHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChoppingTask extends AbstractChoreTask {
   private int chopTicks;
   private int targetTreeTicks;
   private BlockPos targetTree;

   public ChoppingTask() {
      super(ImmutableMap.of(MemoryModuleType.f_26371_, MemoryStatus.VALUE_ABSENT, MemoryModuleType.f_26370_, MemoryStatus.VALUE_ABSENT));
   }

   @Override
   protected boolean shouldRun(ServerLevel world, VillagerEntityMCA villager) {
      return villager.getVillagerBrain().getCurrentJob() == Chore.CHOP && super.shouldRun(world, villager);
   }

   protected boolean shouldKeepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      return this.shouldRun(world, villager);
   }

   protected void finishRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      ItemStack stack = villager.m_21120_(villager.getDominantHand());
      if (!stack.m_41619_()) {
         villager.m_21008_(villager.getDominantHand(), ItemStack.f_41583_);
      }
   }

   @Override
   protected void run(ServerLevel world, VillagerEntityMCA villager, long time) {
      super.run(world, villager, time);
      if (!villager.m_21033_(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.m_35311_(), stack -> stack.m_41720_() instanceof AxeItem);
         if (i == -1) {
            this.abandonJobWithMessage("chore.chopping.noaxe");
         } else {
            villager.m_21008_(villager.getDominantHand(), villager.m_35311_().m_8020_(i));
         }
      }
   }

   @Override
   protected void keepRunning(ServerLevel world, VillagerEntityMCA villager, long time) {
      if (this.villager == null) {
         this.villager = villager;
      }

      if (!InventoryUtils.contains(villager.m_35311_(), AxeItem.class) && !villager.m_21033_(villager.getDominantSlot())) {
         this.abandonJobWithMessage("chore.chopping.noaxe");
      } else if (!villager.m_21033_(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.m_35311_(), stackx -> stackx.m_41720_() instanceof AxeItem);
         ItemStack stack = villager.m_35311_().m_8020_(i);
         villager.m_21008_(villager.getDominantHand(), stack);
      }

      if (this.targetTree != null) {
         villager.moveTowards(this.targetTree);
         BlockState state = world.m_8055_(this.targetTree);
         if (state.m_204336_(BlockTags.f_13106_)) {
            villager.m_6674_(villager.getDominantHand());
            this.chopTicks++;
            if (this.chopTicks >= this.targetTreeTicks) {
               this.chopTicks = 0;
               this.destroyTree(world, this.targetTree);
            }
         } else {
            this.targetTree = null;
            this.targetTreeTicks = 0;
         }

         super.keepRunning(world, villager, time);
      } else {
         List<BlockPos> nearbyLogs = TaskUtils.getNearbyBlocks(villager.m_20183_(), world, blockState -> blockState.m_204336_(BlockTags.f_13106_), 15, 5);
         List<BlockPos> nearbyTrees = new ArrayList<>();
         nearbyLogs.stream().filter(log -> this.isTreeStartLog(world, log)).forEach(nearbyTrees::add);
         this.targetTree = TaskUtils.getNearestPoint(villager.m_20183_(), nearbyTrees);
         if (this.targetTree != null) {
            ItemStack stack = villager.m_21120_(villager.getDominantHand());

            BlockState state;
            for (BlockPos pos = this.targetTree; (state = world.m_8055_(pos)).m_204336_(BlockTags.f_13106_); pos = pos.m_7918_(0, 1, 0)) {
               this.targetTreeTicks = (int)(this.targetTreeTicks + this.getTicksFor(state, 60) / stack.m_41691_(state));
            }
         }

         this.failedTicks = 100;
      }
   }

   private boolean isTreeStartLog(ServerLevel world, BlockPos origin) {
      if (!world.m_8055_(origin).m_204336_(BlockTags.f_13106_)) {
         return false;
      }

      if (!this.isValidTree(world, origin.m_7495_())) {
         return false;
      }

      MutableBlockPos posUp = origin.m_122032_();

      for (int y = 0; y < Config.getInstance().maxTreeHeight; y++) {
         BlockState up = world.m_8055_(posUp.m_142448_(posUp.m_123342_() + 1));
         if (!up.m_204336_(BlockTags.f_13106_)) {
            return up.m_204336_(BlockTags.f_13035_);
         }
      }

      return false;
   }

   private void destroyTree(ServerLevel world, BlockPos origin) {
      ItemStack stack = this.villager.m_21120_(this.villager.getDominantHand());
      BlockPos pos = origin;

      BlockState state;
      while ((state = world.m_8055_(pos)).m_204336_(BlockTags.f_13106_) && world.m_46953_(pos, false, this.villager)) {
         pos = pos.m_7918_(0, 1, 0);
         this.villager.m_35311_().m_19173_(new ItemStack(state.m_60734_(), 1));
         stack.m_41622_(1, this.villager, e -> e.m_21166_(e.getDominantSlot()));
      }
   }

   private boolean isValidTree(ServerLevel world, Vec3 pos) {
      return this.isValidTree(world, BlockPos.m_274446_(pos));
   }

   private boolean isValidTree(ServerLevel world, BlockPos pos) {
      BlockState state = world.m_8055_(pos);
      ResourceLocation stateId = BuiltInRegistries.f_256975_.m_7981_(state.m_60734_());

      for (String blockId : Config.getInstance().validTreeSources) {
         if (blockId.equals(stateId.toString())) {
            return true;
         }

         if (blockId.charAt(0) == '#') {
            ResourceLocation identifier = new ResourceLocation(blockId.substring(1));
            TagKey<Block> tag = TagKey.m_203882_(Registries.f_256747_, identifier);
            if (tag == null || RegistryHelper.isTagEmpty(tag)) {
               throw new JsonSyntaxException("Unknown block tag in validTreeSources '" + identifier + "'");
            }

            if (state.m_204336_(tag)) {
               return true;
            }
         }
      }

      return false;
   }

   private int getTicksFor(BlockState state, int fallback) {
      Map<String, Integer> sources = Config.getInstance().maxTreeTicks;
      ResourceLocation stateId = BuiltInRegistries.f_256975_.m_7981_(state.m_60734_());

      for (String blockId : sources.keySet()) {
         if (blockId.equals(stateId.toString())) {
            return sources.get(blockId);
         }

         if (blockId.charAt(0) == '#') {
            ResourceLocation identifier = new ResourceLocation(blockId.substring(1));
            TagKey<Block> tag = TagKey.m_203882_(Registries.f_256747_, identifier);
            if (tag == null || RegistryHelper.isTagEmpty(tag)) {
               throw new JsonSyntaxException("Unknown block tag in maxTreeTicks '" + identifier + "'");
            }

            if (state.m_204336_(tag)) {
               return sources.get(blockId);
            }
         }
      }

      return fallback;
   }
}
