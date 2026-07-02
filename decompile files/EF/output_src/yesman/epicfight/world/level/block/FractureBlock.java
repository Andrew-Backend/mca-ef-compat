package yesman.epicfight.world.level.block;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import yesman.epicfight.world.level.block.entity.EpicFightBlockEntities;
import yesman.epicfight.world.level.block.entity.FractureBlockEntity;

public class FractureBlock extends BaseEntityBlock {
   protected final StateDefinition<Block, BlockState> stateDefinition;
   private static FractureBlockState fractureBlockState;

   public FractureBlock(Properties properties) {
      super(properties);
      Builder<Block, BlockState> builder = new Builder(this);
      this.stateDefinition = builder.m_61101_(FractureBlock::getDefaultFractureBlockState, FractureBlockState::new);
      fractureBlockState = (FractureBlockState)this.stateDefinition.m_61090_();
   }

   public static FractureBlockState getDefaultFractureBlockState(Block block) {
      return fractureBlockState;
   }

   protected ImmutableMap<BlockState, VoxelShape> m_152458_(Function<BlockState, VoxelShape> p_152459_) {
      return this.stateDefinition.m_61056_().stream().collect(ImmutableMap.toImmutableMap(Function.identity(), p_152459_));
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
      return level.f_46443_ ? m_152132_(blockEntityType, (BlockEntityType)EpicFightBlockEntities.FRACTURE.get(), FractureBlockEntity::lifeTimeTick) : null;
   }

   public BlockEntity m_142194_(BlockPos blockPos, BlockState blockState) {
      return blockState instanceof FractureBlockState fractureBlockState ? new FractureBlockEntity(blockPos, blockState, fractureBlockState) : null;
   }

   public VoxelShape m_5940_(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
      return Shapes.m_83040_();
   }

   public RenderShape m_7514_(BlockState p_50950_) {
      return RenderShape.MODEL;
   }
}
