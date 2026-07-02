package quilt.net.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3481;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_6862;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_2338.class_2339;
import quilt.net.mca.Config;
import quilt.net.mca.entity.VillagerEntityMCA;
import quilt.net.mca.entity.ai.Chore;
import quilt.net.mca.entity.ai.TaskUtils;
import quilt.net.mca.util.InventoryUtils;
import quilt.net.mca.util.RegistryHelper;

public class ChoppingTask extends AbstractChoreTask {
   private int chopTicks;
   private int targetTreeTicks;
   private class_2338 targetTree;

   public ChoppingTask() {
      super(ImmutableMap.of(class_4140.field_18446, class_4141.field_18457, class_4140.field_18445, class_4141.field_18457));
   }

   @Override
   protected boolean shouldRun(class_3218 world, VillagerEntityMCA villager) {
      return villager.getVillagerBrain().getCurrentJob() == Chore.CHOP && super.shouldRun(world, villager);
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
         int i = InventoryUtils.getFirstSlotContainingItem(villager.method_35199(), stack -> stack.method_7909() instanceof class_1743);
         if (i == -1) {
            this.abandonJobWithMessage("chore.chopping.noaxe");
         } else {
            villager.method_6122(villager.getDominantHand(), villager.method_35199().method_5438(i));
         }
      }
   }

   @Override
   protected void keepRunning(class_3218 world, VillagerEntityMCA villager, long time) {
      if (this.villager == null) {
         this.villager = villager;
      }

      if (!InventoryUtils.contains(villager.method_35199(), class_1743.class) && !villager.method_6084(villager.getDominantSlot())) {
         this.abandonJobWithMessage("chore.chopping.noaxe");
      } else if (!villager.method_6084(villager.getDominantSlot())) {
         int i = InventoryUtils.getFirstSlotContainingItem(villager.method_35199(), stackx -> stackx.method_7909() instanceof class_1743);
         class_1799 stack = villager.method_35199().method_5438(i);
         villager.method_6122(villager.getDominantHand(), stack);
      }

      if (this.targetTree != null) {
         villager.moveTowards(this.targetTree);
         class_2680 state = world.method_8320(this.targetTree);
         if (state.method_26164(class_3481.field_15475)) {
            villager.method_6104(villager.getDominantHand());
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
         List<class_2338> nearbyLogs = TaskUtils.getNearbyBlocks(
            villager.method_24515(), world, blockState -> blockState.method_26164(class_3481.field_15475), 15, 5
         );
         List<class_2338> nearbyTrees = new ArrayList<>();
         nearbyLogs.stream().filter(log -> this.isTreeStartLog(world, log)).forEach(nearbyTrees::add);
         this.targetTree = TaskUtils.getNearestPoint(villager.method_24515(), nearbyTrees);
         if (this.targetTree != null) {
            class_1799 stack = villager.method_5998(villager.getDominantHand());

            class_2680 state;
            for (class_2338 pos = this.targetTree; (state = world.method_8320(pos)).method_26164(class_3481.field_15475); pos = pos.method_10069(0, 1, 0)) {
               this.targetTreeTicks = (int)(this.targetTreeTicks + this.getTicksFor(state, 60) / stack.method_7924(state));
            }
         }

         this.failedTicks = 100;
      }
   }

   private boolean isTreeStartLog(class_3218 world, class_2338 origin) {
      if (!world.method_8320(origin).method_26164(class_3481.field_15475)) {
         return false;
      }

      if (!this.isValidTree(world, origin.method_10074())) {
         return false;
      }

      class_2339 posUp = origin.method_25503();

      for (int y = 0; y < Config.getInstance().maxTreeHeight; y++) {
         class_2680 up = world.method_8320(posUp.method_33098(posUp.method_10264() + 1));
         if (!up.method_26164(class_3481.field_15475)) {
            return up.method_26164(class_3481.field_15503);
         }
      }

      return false;
   }

   private void destroyTree(class_3218 world, class_2338 origin) {
      class_1799 stack = this.villager.method_5998(this.villager.getDominantHand());
      class_2338 pos = origin;

      class_2680 state;
      while ((state = world.method_8320(pos)).method_26164(class_3481.field_15475) && world.method_8651(pos, false, this.villager)) {
         pos = pos.method_10069(0, 1, 0);
         this.villager.method_35199().method_5491(new class_1799(state.method_26204(), 1));
         stack.method_7956(1, this.villager, e -> e.method_20235(e.getDominantSlot()));
      }
   }

   private boolean isValidTree(class_3218 world, class_243 pos) {
      return this.isValidTree(world, class_2338.method_49638(pos));
   }

   private boolean isValidTree(class_3218 world, class_2338 pos) {
      class_2680 state = world.method_8320(pos);
      class_2960 stateId = class_7923.field_41175.method_10221(state.method_26204());

      for (String blockId : Config.getInstance().validTreeSources) {
         if (blockId.equals(stateId.toString())) {
            return true;
         }

         if (blockId.charAt(0) == '#') {
            class_2960 identifier = new class_2960(blockId.substring(1));
            class_6862<class_2248> tag = class_6862.method_40092(class_7924.field_41254, identifier);
            if (tag == null || RegistryHelper.isTagEmpty(tag)) {
               throw new JsonSyntaxException("Unknown block tag in validTreeSources '" + identifier + "'");
            }

            if (state.method_26164(tag)) {
               return true;
            }
         }
      }

      return false;
   }

   private int getTicksFor(class_2680 state, int fallback) {
      Map<String, Integer> sources = Config.getInstance().maxTreeTicks;
      class_2960 stateId = class_7923.field_41175.method_10221(state.method_26204());

      for (String blockId : sources.keySet()) {
         if (blockId.equals(stateId.toString())) {
            return sources.get(blockId);
         }

         if (blockId.charAt(0) == '#') {
            class_2960 identifier = new class_2960(blockId.substring(1));
            class_6862<class_2248> tag = class_6862.method_40092(class_7924.field_41254, identifier);
            if (tag == null || RegistryHelper.isTagEmpty(tag)) {
               throw new JsonSyntaxException("Unknown block tag in maxTreeTicks '" + identifier + "'");
            }

            if (state.method_26164(tag)) {
               return sources.get(blockId);
            }
         }
      }

      return fallback;
   }
}
