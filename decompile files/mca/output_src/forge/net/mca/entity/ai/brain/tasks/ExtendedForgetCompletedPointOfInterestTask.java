package forge.net.mca.entity.ai.brain.tasks;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ExtendedForgetCompletedPointOfInterestTask {
   private static final int MAX_RANGE = 16;

   public static OneShot<LivingEntity> create(
      Predicate<Holder<PoiType>> poiTypePredicate, MemoryModuleType<GlobalPos> poiPosModule, Consumer<LivingEntity> onFinish
   ) {
      return BehaviorBuilder.m_258034_(context -> context.group(context.m_257495_(poiPosModule)).apply(context, poiPos -> (world, entity, time) -> {
         GlobalPos globalPos = (GlobalPos)context.m_258051_(poiPos);
         BlockPos blockPos = globalPos.m_122646_();
         if (world.m_46472_() == globalPos.m_122640_() && blockPos.m_203195_(entity.m_20182_(), 16.0)) {
            ServerLevel serverWorld = world.m_7654_().m_129880_(globalPos.m_122640_());
            if (serverWorld == null || !serverWorld.m_8904_().m_27091_(blockPos, poiTypePredicate)) {
               poiPos.m_257971_();
            } else if (isBedOccupiedByOthers(serverWorld, blockPos, entity)) {
               poiPos.m_257971_();
               world.m_8904_().m_27154_(blockPos);
               DebugPackets.m_133719_(world, blockPos);
            }

            return true;
         } else {
            if (entity.m_6274_().m_21876_(poiPosModule, MemoryStatus.VALUE_ABSENT)) {
               onFinish.accept(entity);
            }

            return false;
         }
      }));
   }

   private static boolean isBedOccupiedByOthers(ServerLevel world, BlockPos pos, LivingEntity entity) {
      BlockState blockState = world.m_8055_(pos);
      return blockState.m_204336_(BlockTags.f_13038_) && (Boolean)blockState.m_61143_(BedBlock.f_49441_) && !entity.m_5803_();
   }
}
