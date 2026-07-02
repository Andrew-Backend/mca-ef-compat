package yesman.epicfight.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FractureBlockState extends BlockState {
   private Vector3f translate;
   private Quaternionf rotation;
   private double bouncing;
   private int maxLifeTime;
   private static final Int2ObjectMap<BlockState> ORIGINAL_BLOCK_STATE_CACHE = new Int2ObjectOpenHashMap();

   public static void remove(BlockPos blockPos) {
      ORIGINAL_BLOCK_STATE_CACHE.remove(blockPos.hashCode());
   }

   public static void reset() {
      ORIGINAL_BLOCK_STATE_CACHE.clear();
   }

   public FractureBlockState(Block block, ImmutableMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> mapCodec) {
      super(block, propertyMap, mapCodec);
   }

   public void setFractureInfo(BlockPos bp, BlockState originalState, Vector3f translate, Quaternionf rotation, double bouncing, int maxLifeTime) {
      ORIGINAL_BLOCK_STATE_CACHE.put(bp.hashCode(), originalState);
      this.translate = translate;
      this.rotation = rotation;
      this.bouncing = bouncing;
      this.maxLifeTime = maxLifeTime;
   }

   public Vector3f getTranslate() {
      return this.translate;
   }

   public Quaternionf getRotation() {
      return this.rotation;
   }

   public BlockState getOriginalBlockState(BlockPos blockPos) {
      return (BlockState)ORIGINAL_BLOCK_STATE_CACHE.get(blockPos.hashCode());
   }

   public double getBouncing() {
      return this.bouncing;
   }

   public int getLifeTime() {
      return this.maxLifeTime;
   }

   public boolean m_155947_() {
      return true;
   }

   public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState neighborState, Direction dir) {
      return false;
   }

   public boolean supportsExternalFaceHiding() {
      return false;
   }

   public int getLightEmission(BlockGetter level, BlockPos blockPos) {
      return ORIGINAL_BLOCK_STATE_CACHE.containsKey(blockPos.hashCode())
         ? ((BlockState)ORIGINAL_BLOCK_STATE_CACHE.get(blockPos.hashCode())).getLightEmission(level, blockPos)
         : ((Block)this.f_61112_).getLightEmission(this, level, blockPos);
   }

   public VoxelShape m_60808_(BlockGetter level, BlockPos blockPos) {
      return Shapes.m_83040_();
   }

   public VoxelShape m_60651_(BlockGetter level, BlockPos blockPos, CollisionContext collisionContext) {
      return Shapes.m_83040_();
   }

   public VoxelShape m_60812_(BlockGetter level, BlockPos blockPos) {
      BlockState blockState = this.getOriginalBlockState(blockPos);
      return blockState == null ? Shapes.m_83040_() : blockState.m_60742_(level, blockPos, CollisionContext.m_82749_());
   }

   public VoxelShape m_60742_(BlockGetter level, BlockPos blockPos, CollisionContext collisionContext) {
      BlockState blockState = this.getOriginalBlockState(blockPos);
      return blockState == null ? Shapes.m_83040_() : blockState.m_60742_(level, blockPos, collisionContext);
   }

   public VoxelShape m_60771_(BlockGetter level, BlockPos blockPos, CollisionContext collisionContext) {
      return Shapes.m_83040_();
   }

   public RenderShape m_60799_() {
      return RenderShape.INVISIBLE;
   }
}
