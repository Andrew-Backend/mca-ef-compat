package forge.net.mca.entity.ai.brain.tasks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public class SmarterOpenDoorsTask extends Behavior<LivingEntity> {
   private static final int RUN_TIME = 20;
   private static final double PATHING_DISTANCE = 2.0;
   private static final double REACH_DISTANCE = 2.0;
   @Nullable
   private Node pathNode;
   private int ticks;

   public SmarterOpenDoorsTask() {
      super(ImmutableMap.of(MemoryModuleType.f_26377_, MemoryStatus.VALUE_PRESENT, MemoryModuleType.f_26379_, MemoryStatus.REGISTERED));
   }

   protected boolean m_6114_(ServerLevel world, LivingEntity entity) {
      Optional<Path> optionalMemory = entity.m_6274_().m_257414_(MemoryModuleType.f_26377_);
      if (optionalMemory.isEmpty()) {
         return false;
      }

      Path path = optionalMemory.get();
      if (path.m_77392_()) {
         return false;
      }

      if (!Objects.equals(this.pathNode, path.m_77401_())) {
         this.ticks = 20;
         return true;
      }

      if (this.ticks > 0) {
         this.ticks--;
      }

      return this.ticks == 0;
   }

   public static boolean setOpen(@Nullable Entity entity, Level world, BlockState state, BlockPos pos, boolean open) {
      if (state.m_61138_(BlockStateProperties.f_61446_) && (Boolean)state.m_61143_(BlockStateProperties.f_61446_) != open) {
         world.m_7731_(pos, (BlockState)state.m_61124_(BlockStateProperties.f_61446_, open), 10);
         world.m_142346_(entity, open ? GameEvent.f_157796_ : GameEvent.f_157793_, pos);
         playOpenCloseSound(entity, world, pos, open);
         return true;
      } else {
         return false;
      }
   }

   private static void playOpenCloseSound(@Nullable Entity entity, Level world, BlockPos pos, boolean open) {
      world.m_245803_(entity, pos, open ? SoundEvents.f_12627_ : SoundEvents.f_12626_, SoundSource.BLOCKS, 0.75F, world.m_213780_().m_188501_() * 0.1F + 0.9F);
   }

   private void openDoor(ServerLevel world, LivingEntity entity, Node pathNode) {
      if (pathNode != null) {
         BlockPos blockPos = pathNode.m_77288_();
         BlockState blockState = world.m_8055_(blockPos);
         if (isDoor(blockState)) {
            boolean wasOpen = blockState.m_61138_(BlockStateProperties.f_61446_) && (Boolean)blockState.m_61143_(BlockStateProperties.f_61446_);
            if (setOpen(entity, world, blockState, blockPos, true) || wasOpen) {
               this.rememberToCloseDoor(world, entity, blockPos);
            }
         }
      }
   }

   private static boolean isDoor(BlockState blockState) {
      return blockState.m_204338_(BlockTags.f_13095_, state -> state.m_60734_() instanceof DoorBlock)
         || blockState.m_204338_(BlockTags.f_13055_, state -> state.m_60734_() instanceof FenceGateBlock);
   }

   protected void m_6735_(ServerLevel world, LivingEntity entity, long time) {
      Path path = (Path)entity.m_6274_().m_257414_(MemoryModuleType.f_26377_).get();
      this.pathNode = path.m_77401_();
      this.openDoor(world, entity, path.m_77402_());
      this.openDoor(world, entity, path.m_77401_());
      closeDoors(world, entity, path.m_77402_(), path.m_77401_());
   }

   public static void closeDoors(ServerLevel world, LivingEntity entity, @Nullable Node lastNode, @Nullable Node currentNode) {
      Brain<?> brain = entity.m_6274_();
      if (brain.m_21874_(MemoryModuleType.f_26379_)) {
         Iterator<GlobalPos> iterator = ((Set)brain.m_257414_(MemoryModuleType.f_26379_).get()).iterator();

         while (iterator.hasNext()) {
            GlobalPos globalPos = iterator.next();
            BlockPos blockPos = globalPos.m_122646_();
            if ((lastNode == null || !lastNode.m_77288_().equals(blockPos)) && (currentNode == null || !currentNode.m_77288_().equals(blockPos))) {
               if (cannotReachDoor(world, entity, globalPos)) {
                  iterator.remove();
               } else {
                  BlockState blockState = world.m_8055_(blockPos);
                  if (!isDoor(blockState)) {
                     iterator.remove();
                  } else if (blockState.m_61138_(BlockStateProperties.f_61446_) && !(Boolean)blockState.m_61143_(BlockStateProperties.f_61446_)) {
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

   private static boolean hasOtherMobReachedDoor(LivingEntity entity, BlockPos pos) {
      Brain<?> brain = entity.m_6274_();
      return !brain.m_21874_(MemoryModuleType.f_148204_)
         ? false
         : ((List)brain.m_257414_(MemoryModuleType.f_148204_).get())
            .stream()
            .filter(livingEntity2 -> livingEntity2.m_6095_() == entity.m_6095_())
            .filter(livingEntity -> pos.m_203195_(livingEntity.m_20182_(), 2.0))
            .anyMatch(livingEntity -> hasReached(livingEntity, pos));
   }

   private static boolean hasReached(LivingEntity entity, BlockPos pos) {
      if (!entity.m_6274_().m_21874_(MemoryModuleType.f_26377_)) {
         return false;
      }

      Path path = (Path)entity.m_6274_().m_257414_(MemoryModuleType.f_26377_).get();
      if (path.m_77392_()) {
         return false;
      }

      Node pathNode = path.m_77402_();
      return pathNode == null ? false : pos.equals(pathNode.m_77288_()) || pos.equals(path.m_77401_().m_77288_());
   }

   private static boolean cannotReachDoor(ServerLevel world, LivingEntity entity, GlobalPos doorPos) {
      return doorPos.m_122640_() != world.m_46472_() || !doorPos.m_122646_().m_203195_(entity.m_20182_(), 2.0);
   }

   private void rememberToCloseDoor(ServerLevel world, LivingEntity entity, BlockPos pos) {
      Brain<?> brain = entity.m_6274_();
      GlobalPos globalPos = GlobalPos.m_122643_(world.m_46472_(), pos);
      if (brain.m_257414_(MemoryModuleType.f_26379_).isPresent()) {
         ((Set)brain.m_257414_(MemoryModuleType.f_26379_).get()).add(globalPos);
      } else {
         brain.m_21879_(MemoryModuleType.f_26379_, Sets.newHashSet(new GlobalPos[]{globalPos}));
      }
   }
}
