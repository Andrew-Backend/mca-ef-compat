package quilt.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_11;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2323;
import net.minecraft.class_2338;
import net.minecraft.class_2349;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_3218;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3481;
import net.minecraft.class_4095;
import net.minecraft.class_4097;
import net.minecraft.class_4140;
import net.minecraft.class_4141;
import net.minecraft.class_4208;
import net.minecraft.class_5712;
import net.minecraft.class_9;
import org.jetbrains.annotations.Nullable;

public class SmarterOpenDoorsTask extends class_4097<class_1309> {
   private static final int RUN_TIME = 20;
   private static final double PATHING_DISTANCE = 2.0;
   private static final double REACH_DISTANCE = 2.0;
   @Nullable
   private class_9 pathNode;
   private int ticks;

   public SmarterOpenDoorsTask() {
      super(ImmutableMap.of(class_4140.field_18449, class_4141.field_18456, class_4140.field_26389, class_4141.field_18458));
   }

   protected boolean method_18919(class_3218 world, class_1309 entity) {
      Optional<class_11> optionalMemory = entity.method_18868().method_46873(class_4140.field_18449);
      if (optionalMemory.isEmpty()) {
         return false;
      }

      class_11 path = optionalMemory.get();
      if (path.method_46()) {
         return false;
      }

      if (!Objects.equals(this.pathNode, path.method_29301())) {
         this.ticks = 20;
         return true;
      }

      if (this.ticks > 0) {
         this.ticks--;
      }

      return this.ticks == 0;
   }

   public static boolean setOpen(@Nullable class_1297 entity, class_1937 world, class_2680 state, class_2338 pos, boolean open) {
      if (state.method_28498(class_2741.field_12537) && (Boolean)state.method_11654(class_2741.field_12537) != open) {
         world.method_8652(pos, (class_2680)state.method_11657(class_2741.field_12537, open), 10);
         world.method_33596(entity, open ? class_5712.field_28168 : class_5712.field_28169, pos);
         playOpenCloseSound(entity, world, pos, open);
         return true;
      } else {
         return false;
      }
   }

   private static void playOpenCloseSound(@Nullable class_1297 entity, class_1937 world, class_2338 pos, boolean open) {
      world.method_45445(
         entity, pos, open ? class_3417.field_14664 : class_3417.field_14541, class_3419.field_15245, 0.75F, world.method_8409().method_43057() * 0.1F + 0.9F
      );
   }

   private void openDoor(class_3218 world, class_1309 entity, class_9 pathNode) {
      if (pathNode != null) {
         class_2338 blockPos = pathNode.method_22879();
         class_2680 blockState = world.method_8320(blockPos);
         if (isDoor(blockState)) {
            boolean wasOpen = blockState.method_28498(class_2741.field_12537) && (Boolean)blockState.method_11654(class_2741.field_12537);
            if (setOpen(entity, world, blockState, blockPos, true) || wasOpen) {
               this.rememberToCloseDoor(world, entity, blockPos);
            }
         }
      }
   }

   private static boolean isDoor(class_2680 blockState) {
      return blockState.method_27851(class_3481.field_15494, state -> state.method_26204() instanceof class_2323)
         || blockState.method_27851(class_3481.field_25147, state -> state.method_26204() instanceof class_2349);
   }

   protected void method_18920(class_3218 world, class_1309 entity, long time) {
      class_11 path = (class_11)entity.method_18868().method_46873(class_4140.field_18449).get();
      this.pathNode = path.method_29301();
      this.openDoor(world, entity, path.method_30850());
      this.openDoor(world, entity, path.method_29301());
      closeDoors(world, entity, path.method_30850(), path.method_29301());
   }

   public static void closeDoors(class_3218 world, class_1309 entity, @Nullable class_9 lastNode, @Nullable class_9 currentNode) {
      class_4095<?> brain = entity.method_18868();
      if (brain.method_18896(class_4140.field_26389)) {
         Iterator<class_4208> iterator = ((Set)brain.method_46873(class_4140.field_26389).get()).iterator();

         while (iterator.hasNext()) {
            class_4208 globalPos = iterator.next();
            class_2338 blockPos = globalPos.method_19446();
            if ((lastNode == null || !lastNode.method_22879().equals(blockPos)) && (currentNode == null || !currentNode.method_22879().equals(blockPos))) {
               if (cannotReachDoor(world, entity, globalPos)) {
                  iterator.remove();
               } else {
                  class_2680 blockState = world.method_8320(blockPos);
                  if (!isDoor(blockState)) {
                     iterator.remove();
                  } else if (blockState.method_28498(class_2741.field_12537) && !(Boolean)blockState.method_11654(class_2741.field_12537)) {
                     iterator.remove();
                  } else if (!hasOtherMobReachedDoor(entity, blockPos)) {
                     setOpen(entity, world, blockState, blockPos, false);
                     iterator.remove();
                  }
               }
            }
         }
      }
   }

   private static boolean hasOtherMobReachedDoor(class_1309 entity, class_2338 pos) {
      class_4095<?> brain = entity.method_18868();
      return !brain.method_18896(class_4140.field_18441)
         ? false
         : ((List)brain.method_46873(class_4140.field_18441).get())
            .stream()
            .filter(livingEntity2 -> livingEntity2.method_5864() == entity.method_5864())
            .filter(livingEntity -> pos.method_19769(livingEntity.method_19538(), 2.0))
            .anyMatch(livingEntity -> hasReached(livingEntity, pos));
   }

   private static boolean hasReached(class_1309 entity, class_2338 pos) {
      if (!entity.method_18868().method_18896(class_4140.field_18449)) {
         return false;
      }

      class_11 path = (class_11)entity.method_18868().method_46873(class_4140.field_18449).get();
      if (path.method_46()) {
         return false;
      }

      class_9 pathNode = path.method_30850();
      return pathNode == null ? false : pos.equals(pathNode.method_22879()) || pos.equals(path.method_29301().method_22879());
   }

   private static boolean cannotReachDoor(class_3218 world, class_1309 entity, class_4208 doorPos) {
      return doorPos.method_19442() != world.method_27983() || !doorPos.method_19446().method_19769(entity.method_19538(), 2.0);
   }

   private void rememberToCloseDoor(class_3218 world, class_1309 entity, class_2338 pos) {
      class_4095<?> brain = entity.method_18868();
      class_4208 globalPos = class_4208.method_19443(world.method_27983(), pos);
      if (brain.method_46873(class_4140.field_26389).isPresent()) {
         ((Set)brain.method_46873(class_4140.field_26389).get()).add(globalPos);
      } else {
         brain.method_18878(class_4140.field_26389, Sets.newHashSet(new class_4208[]{globalPos}));
      }
   }
}
