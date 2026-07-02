package forge.net.mca.entity.ai.pathfinder;

import forge.net.mca.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VillagerLandPathNodeMaker extends NodeEvaluator {
   protected float waterPathNodeTypeWeight;
   private final Long2ObjectMap<ExtendedPathNodeType> nodeTypes = new Long2ObjectOpenHashMap();
   private final Object2BooleanMap<AABB> collidedBoxes = new Object2BooleanOpenHashMap();

   public void m_6028_(PathNavigationRegion cachedWorld, Mob entity) {
      super.m_6028_(cachedWorld, entity);
      this.waterPathNodeTypeWeight = this.getPenalty(entity, ExtendedPathNodeType.WATER);
   }

   public void m_6802_() {
      this.f_77313_.m_21441_(ExtendedPathNodeType.WATER.toVanilla(), this.waterPathNodeTypeWeight);
      this.nodeTypes.clear();
      this.collidedBoxes.clear();
      super.m_6802_();
   }

   public Node m_7171_() {
      MutableBlockPos mutable = new MutableBlockPos();
      int i = this.f_77313_.m_146904_();
      BlockState blockState = this.f_77312_.m_8055_(mutable.m_122169_(this.f_77313_.m_20185_(), i, this.f_77313_.m_20189_()));
      if (!this.f_77313_.m_203441_(blockState.m_60819_())) {
         if (this.m_77361_() && this.f_77313_.m_20069_()) {
            while (true) {
               if (!blockState.m_60713_(Blocks.f_49990_) && blockState.m_60819_() != Fluids.f_76193_.m_76068_(false)) {
                  i--;
                  break;
               }

               blockState = this.f_77312_.m_8055_(mutable.m_122169_(this.f_77313_.m_20185_(), ++i, this.f_77313_.m_20189_()));
            }
         } else if (this.f_77313_.m_20096_()) {
            i = Mth.m_14107_(this.f_77313_.m_20186_() + 0.5);
         } else {
            BlockPos blockPos = this.f_77313_.m_20183_();

            while (
               (this.f_77312_.m_8055_(blockPos).m_60795_() || this.f_77312_.m_8055_(blockPos).m_60647_(this.f_77312_, blockPos, PathComputationType.LAND))
                  && blockPos.m_123342_() > this.f_77313_.m_9236_().m_141937_()
            ) {
               blockPos = blockPos.m_7495_();
            }

            i = blockPos.m_7494_().m_123342_();
         }
      } else {
         while (this.f_77313_.m_203441_(blockState.m_60819_())) {
            blockState = this.f_77312_.m_8055_(mutable.m_122169_(this.f_77313_.m_20185_(), ++i, this.f_77313_.m_20189_()));
         }

         i--;
      }

      BlockPos blockPos = this.f_77313_.m_20183_();
      ExtendedPathNodeType pathNodeType = this.getNodeType(this.f_77313_, blockPos.m_123341_(), i, blockPos.m_123343_());
      if (this.getPenalty(pathNodeType) < 0.0F) {
         AABB box = this.f_77313_.m_20191_();
         if (this.canPathThrough(mutable.m_122169_(box.f_82288_, i, box.f_82290_))
            || this.canPathThrough(mutable.m_122169_(box.f_82288_, i, box.f_82293_))
            || this.canPathThrough(mutable.m_122169_(box.f_82291_, i, box.f_82290_))
            || this.canPathThrough(mutable.m_122169_(box.f_82291_, i, box.f_82293_))) {
            Node pathNode = this.m_77349_(mutable);
            ExtendedPathNodeType type = this.getNodeType(this.f_77313_, pathNode.m_77288_());
            pathNode.f_77282_ = type.toVanilla();
            pathNode.f_77281_ = this.getPenalty(type);
            return pathNode;
         }
      }

      Node pathNode2 = this.m_5676_(blockPos.m_123341_(), i, blockPos.m_123343_());
      ExtendedPathNodeType type = this.getNodeType(this.f_77313_, pathNode2.m_77288_());
      pathNode2.f_77282_ = type.toVanilla();
      pathNode2.f_77281_ = this.getPenalty(type);
      return pathNode2;
   }

   private boolean canPathThrough(BlockPos pos) {
      ExtendedPathNodeType pathNodeType = this.getNodeType(this.f_77313_, pos);
      return this.getPenalty(pathNodeType) >= 0.0F;
   }

   public Target m_7568_(double x, double y, double z) {
      return new Target(this.m_5676_(Mth.m_14107_(x), Mth.m_14107_(y), Mth.m_14107_(z)));
   }

   public int m_6065_(Node[] successors, Node node) {
      ExtendedPathNodeType pathNodeTypeHead = this.getNodeType(this.f_77313_, node.f_77271_, node.f_77272_ + 1, node.f_77273_);
      ExtendedPathNodeType pathNodeType = this.getNodeType(this.f_77313_, node.f_77271_, node.f_77272_, node.f_77273_);
      double feetY = this.getFeetY(new BlockPos(node.f_77271_, node.f_77272_, node.f_77273_));
      int maxYStep = 0;
      if (this.getPenalty(pathNodeTypeHead) >= 0.0F && pathNodeType != ExtendedPathNodeType.STICKY_HONEY) {
         maxYStep = Mth.m_14143_(Math.max(1.0F, this.f_77313_.m_274421_()));
      }

      Node pathNode1 = this.getPathNode(node.f_77271_, node.f_77272_, node.f_77273_ + 1, maxYStep, feetY, Direction.SOUTH, pathNodeType);
      Node pathNode2 = this.getPathNode(node.f_77271_ - 1, node.f_77272_, node.f_77273_, maxYStep, feetY, Direction.WEST, pathNodeType);
      Node pathNode3 = this.getPathNode(node.f_77271_ + 1, node.f_77272_, node.f_77273_, maxYStep, feetY, Direction.EAST, pathNodeType);
      Node pathNode4 = this.getPathNode(node.f_77271_, node.f_77272_, node.f_77273_ - 1, maxYStep, feetY, Direction.NORTH, pathNodeType);
      Node pathNode5 = this.getPathNode(node.f_77271_ - 1, node.f_77272_, node.f_77273_ - 1, maxYStep, feetY, Direction.NORTH, pathNodeType);
      Node pathNode6 = this.getPathNode(node.f_77271_ + 1, node.f_77272_, node.f_77273_ - 1, maxYStep, feetY, Direction.NORTH, pathNodeType);
      Node pathNode7 = this.getPathNode(node.f_77271_ - 1, node.f_77272_, node.f_77273_ + 1, maxYStep, feetY, Direction.SOUTH, pathNodeType);
      Node pathNode8 = this.getPathNode(node.f_77271_ + 1, node.f_77272_, node.f_77273_ + 1, maxYStep, feetY, Direction.SOUTH, pathNodeType);
      int i = 0;
      if (this.isValidAdjacentSuccessor(pathNode1, node)) {
         successors[i++] = pathNode1;
      }

      if (this.isValidAdjacentSuccessor(pathNode2, node)) {
         successors[i++] = pathNode2;
      }

      if (this.isValidAdjacentSuccessor(pathNode3, node)) {
         successors[i++] = pathNode3;
      }

      if (this.isValidAdjacentSuccessor(pathNode4, node)) {
         successors[i++] = pathNode4;
      }

      if (this.isValidDiagonalSuccessor(node, pathNode2, pathNode4, pathNode5)) {
         successors[i++] = pathNode5;
      }

      if (this.isValidDiagonalSuccessor(node, pathNode3, pathNode4, pathNode6)) {
         successors[i++] = pathNode6;
      }

      if (this.isValidDiagonalSuccessor(node, pathNode2, pathNode1, pathNode7)) {
         successors[i++] = pathNode7;
      }

      if (this.isValidDiagonalSuccessor(node, pathNode3, pathNode1, pathNode8)) {
         successors[i++] = pathNode8;
      }

      return i;
   }

   protected boolean isValidAdjacentSuccessor(Node node, Node successor1) {
      return node != null && !node.f_77279_ && (node.f_77281_ >= 0.0F || successor1.f_77281_ < 0.0F);
   }

   protected boolean isValidDiagonalSuccessor(Node xNode, Node zNode, Node xDiagNode, Node zDiagNode) {
      if (zDiagNode == null || xDiagNode == null || zNode == null) {
         return false;
      }

      if (zDiagNode.f_77279_) {
         return false;
      }

      if (xDiagNode.f_77272_ <= xNode.f_77272_ && zNode.f_77272_ <= xNode.f_77272_) {
         if (zNode.f_77282_ != ExtendedPathNodeType.WALKABLE_DOOR.toVanilla()
            && xDiagNode.f_77282_ != ExtendedPathNodeType.WALKABLE_DOOR.toVanilla()
            && zDiagNode.f_77282_ != ExtendedPathNodeType.WALKABLE_DOOR.toVanilla()) {
            boolean bl = xDiagNode.f_77282_ == ExtendedPathNodeType.FENCE.toVanilla()
               && zNode.f_77282_ == ExtendedPathNodeType.FENCE.toVanilla()
               && this.f_77313_.m_20205_() < 0.5;
            return zDiagNode.f_77281_ >= 0.0F
               && (xDiagNode.f_77272_ < xNode.f_77272_ || xDiagNode.f_77281_ >= 0.0F || bl)
               && (zNode.f_77272_ < xNode.f_77272_ || zNode.f_77281_ >= 0.0F || bl);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isBlocked(Node node) {
      Vec3 vec3d = new Vec3(node.f_77271_ - this.f_77313_.m_20185_(), node.f_77272_ - this.f_77313_.m_20186_(), node.f_77273_ - this.f_77313_.m_20189_());
      AABB box = this.f_77313_.m_20191_();
      int i = Mth.m_14165_(vec3d.m_82553_() / box.m_82309_());
      vec3d = vec3d.m_82490_(1.0F / i);

      for (int j = 1; j <= i; j++) {
         if (this.checkBoxCollision(box = box.m_82383_(vec3d))) {
            return false;
         }
      }

      return true;
   }

   protected double getFeetY(BlockPos pos) {
      return getFeetY(this.f_77312_, pos);
   }

   public static double getFeetY(BlockGetter world, BlockPos pos) {
      BlockPos blockPos = pos.m_7495_();
      VoxelShape voxelShape = world.m_8055_(blockPos).m_60812_(world, blockPos);
      return blockPos.m_123342_() + (voxelShape.m_83281_() ? 0.0 : voxelShape.m_83297_(Axis.Y));
   }

   float getPenalty(ExtendedPathNodeType pathNodeType) {
      return this.getPenalty(this.f_77313_, pathNodeType);
   }

   private float getPenalty(Mob mob, ExtendedPathNodeType type) {
      return mob.m_21439_(type.toVanilla()) + type.getBonusPenalty();
   }

   protected Node getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, ExtendedPathNodeType nodeType) {
      MutableBlockPos mutable = new MutableBlockPos();
      double step = this.getFeetY(mutable.m_122178_(x, y, z));
      if (step - prevFeetY > 1.125) {
         return null;
      }

      ExtendedPathNodeType pathNodeType = this.getNodeType(this.f_77313_, x, y, z);
      float penalty = this.getPenalty(pathNodeType);
      double e = this.f_77313_.m_20205_() / 2.0;
      Node pathNode = null;
      if (penalty >= 0.0F) {
         pathNode = this.m_5676_(x, y, z);
         pathNode.f_77282_ = pathNodeType.toVanilla();
         pathNode.f_77281_ = Math.max(pathNode.f_77281_, penalty);
      }

      if (nodeType == ExtendedPathNodeType.FENCE && pathNode != null && pathNode.f_77281_ >= 0.0F && !this.isBlocked(pathNode)) {
         pathNode = null;
      }

      if (pathNodeType.isWalkable()) {
         return pathNode;
      }

      if ((pathNode == null || pathNode.f_77281_ < 0.0F)
         && maxYStep > 0
         && pathNodeType != ExtendedPathNodeType.FENCE
         && pathNodeType != ExtendedPathNodeType.UNPASSABLE_RAIL
         && pathNodeType != ExtendedPathNodeType.TRAPDOOR
         && pathNodeType != ExtendedPathNodeType.POWDER_SNOW) {
         pathNode = this.getPathNode(x, y + 1, z, maxYStep - 1, prevFeetY, direction, nodeType);
         double h;
         double g;
         if (pathNode != null
            && (pathNode.f_77282_ == ExtendedPathNodeType.OPEN.toVanilla() || pathNode.f_77282_ == ExtendedPathNodeType.WALKABLE.toVanilla())
            && this.f_77313_.m_20205_() < 1.0F
            && this.checkBoxCollision(
               new AABB(
                  (g = x - direction.m_122429_() + 0.5) - e,
                  getFeetY(this.f_77312_, mutable.m_122169_(g, y + 1, h = z - direction.m_122431_() + 0.5)) + 0.001,
                  h - e,
                  g + e,
                  this.f_77313_.m_20206_() + getFeetY(this.f_77312_, mutable.m_122169_(pathNode.f_77271_, pathNode.f_77272_, pathNode.f_77273_)) - 0.002,
                  h + e
               )
            )) {
            pathNode = null;
         }
      }

      if (pathNodeType == ExtendedPathNodeType.WATER && !this.m_77361_()) {
         if (this.getNodeType(this.f_77313_, x, y - 1, z) != ExtendedPathNodeType.WATER) {
            return pathNode;
         }

         while (y > this.f_77313_.m_9236_().m_141937_()) {
            if ((pathNodeType = this.getNodeType(this.f_77313_, x, --y, z)) != ExtendedPathNodeType.WATER) {
               return pathNode;
            }

            pathNode = this.m_5676_(x, y, z);
            pathNode.f_77282_ = pathNodeType.toVanilla();
            pathNode.f_77281_ = Math.max(pathNode.f_77281_, this.getPenalty(pathNodeType));
         }
      }

      if (pathNodeType == ExtendedPathNodeType.OPEN) {
         int i = 0;
         int j = y;

         while (pathNodeType == ExtendedPathNodeType.OPEN) {
            if (--y < this.f_77313_.m_9236_().m_141937_()) {
               Node pathNode2 = this.m_5676_(x, j, z);
               pathNode2.f_77282_ = ExtendedPathNodeType.BLOCKED.toVanilla();
               pathNode2.f_77281_ = -1.0F;
               return pathNode2;
            }

            if (i++ >= this.f_77313_.m_6056_()) {
               Node pathNode2 = this.m_5676_(x, y, z);
               pathNode2.f_77282_ = ExtendedPathNodeType.BLOCKED.toVanilla();
               pathNode2.f_77281_ = -1.0F;
               return pathNode2;
            }

            pathNodeType = this.getNodeType(this.f_77313_, x, y, z);
            penalty = this.getPenalty(pathNodeType);
            if (pathNodeType != ExtendedPathNodeType.OPEN && penalty >= 0.0F) {
               pathNode = this.m_5676_(x, y, z);
               pathNode.f_77282_ = pathNodeType.toVanilla();
               pathNode.f_77281_ = Math.max(pathNode.f_77281_, penalty);
               break;
            }

            if (penalty < 0.0F) {
               Node pathNode2 = this.m_5676_(x, y, z);
               pathNode2.f_77282_ = ExtendedPathNodeType.BLOCKED.toVanilla();
               pathNode2.f_77281_ = -1.0F;
               return pathNode2;
            }
         }
      }

      if (pathNodeType == ExtendedPathNodeType.FENCE) {
         pathNode = this.m_5676_(x, y, z);
         pathNode.f_77279_ = true;
         pathNode.f_77282_ = pathNodeType.toVanilla();
         pathNode.f_77281_ = pathNodeType.getDefaultPenalty();
      }

      return pathNode;
   }

   private boolean checkBoxCollision(AABB box) {
      return this.collidedBoxes.computeIfAbsent(box, object -> !this.f_77312_.m_45756_(this.f_77313_, box));
   }

   public BlockPathTypes m_7209_(BlockGetter world, int x, int y, int z, Mob mob) {
      return this.getExtendedNodeType(world, x, y, z, mob, this.f_77315_, this.f_77316_, this.f_77317_, this.f_77319_, this.f_77318_).toVanilla();
   }

   public ExtendedPathNodeType getExtendedNodeType(
      BlockGetter world, int x, int y, int z, Mob mob, int sizeX, int sizeY, int sizeZ, boolean canOpenDoors, boolean canEnterOpenDoors
   ) {
      EnumSet<ExtendedPathNodeType> enumSet = EnumSet.noneOf(ExtendedPathNodeType.class);
      ExtendedPathNodeType centerPathNodeType = this.findNearbyNodeTypes(
         world, x, y, z, sizeX, sizeY, sizeZ, canOpenDoors, canEnterOpenDoors, enumSet, mob.m_20183_()
      );
      if (enumSet.contains(ExtendedPathNodeType.FENCE)) {
         return ExtendedPathNodeType.FENCE;
      }

      if (enumSet.contains(ExtendedPathNodeType.UNPASSABLE_RAIL)) {
         return ExtendedPathNodeType.UNPASSABLE_RAIL;
      }

      ExtendedPathNodeType worstPathNode = ExtendedPathNodeType.BLOCKED;

      for (ExtendedPathNodeType touchedPathNodeType : enumSet) {
         if (this.getPenalty(mob, touchedPathNodeType) < 0.0F) {
            return touchedPathNodeType;
         }

         if (this.getPenalty(mob, touchedPathNodeType) >= this.getPenalty(mob, worstPathNode)) {
            worstPathNode = touchedPathNodeType;
         }
      }

      return sizeX <= 1 && centerPathNodeType == ExtendedPathNodeType.OPEN && this.getPenalty(mob, worstPathNode) == 0.0F
         ? ExtendedPathNodeType.OPEN
         : worstPathNode;
   }

   public ExtendedPathNodeType findNearbyNodeTypes(
      BlockGetter world,
      int x,
      int y,
      int z,
      int sizeX,
      int sizeY,
      int sizeZ,
      boolean canOpenDoors,
      boolean canEnterOpenDoors,
      EnumSet<ExtendedPathNodeType> nearbyTypes,
      BlockPos pos
   ) {
      MutableBlockPos p = new MutableBlockPos(x, y, z);
      ExtendedPathNodeType type = ExtendedPathNodeType.BLOCKED;

      for (int i = 0; i < sizeX; i++) {
         for (int j = 0; j < sizeY; j++) {
            for (int k = 0; k < sizeZ; k++) {
               int l = i + x;
               int m = j + y;
               int n = k + z;
               p.m_122178_(l, m, n);
               BlockState blockState = world.m_8055_(p);
               ExtendedPathNodeType pathNodeType = this.getExtendedDefaultNodeType(world, l, m, n);
               pathNodeType = this.adjustNodeType(world, canOpenDoors, canEnterOpenDoors, pos, pathNodeType);
               if (Config.getInstance().useSmarterDoorAI && blockState.m_204338_(BlockTags.f_13055_, state -> state.m_60734_() instanceof FenceGateBlock)) {
                  pathNodeType = ExtendedPathNodeType.WALKABLE_DOOR;
               }

               if (pathNodeType != ExtendedPathNodeType.DOOR_OPEN && blockState.m_60734_() instanceof DoorBlock) {
                  for (BlockPos adjacent : BlockPos.m_121976_(l - 1, m - 1, n - 1, l + 1, m + 1, n + 1)) {
                     if (world.m_8055_(adjacent).m_204336_(BlockTags.f_13099_)) {
                        pathNodeType = ExtendedPathNodeType.DOOR_OPEN;
                        break;
                     }
                  }
               }

               if (i == 0 && j == 0 && k == 0) {
                  type = pathNodeType;
               }

               nearbyTypes.add(pathNodeType);
            }
         }
      }

      return type;
   }

   protected ExtendedPathNodeType adjustNodeType(BlockGetter world, boolean canOpenDoors, boolean canEnterOpenDoors, BlockPos pos, ExtendedPathNodeType type) {
      if (type == ExtendedPathNodeType.DOOR_WOOD_CLOSED && canOpenDoors && canEnterOpenDoors) {
         type = ExtendedPathNodeType.WALKABLE_DOOR;
      }

      if (type == ExtendedPathNodeType.DOOR_OPEN && !canEnterOpenDoors) {
         type = ExtendedPathNodeType.BLOCKED;
      }

      if (type == ExtendedPathNodeType.RAIL
         && !(world.m_8055_(pos).m_60734_() instanceof BaseRailBlock)
         && !(world.m_8055_(pos.m_7495_()).m_60734_() instanceof BaseRailBlock)) {
         type = ExtendedPathNodeType.UNPASSABLE_RAIL;
      }

      if (type == ExtendedPathNodeType.LEAVES) {
         type = ExtendedPathNodeType.BLOCKED;
      }

      return type;
   }

   private ExtendedPathNodeType getNodeType(Mob entity, BlockPos pos) {
      return this.getNodeType(entity, pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
   }

   protected ExtendedPathNodeType getNodeType(Mob entity, int x, int y, int z) {
      return (ExtendedPathNodeType)this.nodeTypes
         .computeIfAbsent(
            BlockPos.m_121882_(x, y, z),
            l -> this.getExtendedNodeType(this.f_77312_, x, y, z, entity, this.f_77315_, this.f_77316_, this.f_77317_, this.m_77360_(), this.m_77357_())
         );
   }

   public BlockPathTypes m_8086_(BlockGetter world, int x, int y, int z) {
      return this.getExtendedDefaultNodeType(world, x, y, z).toVanilla();
   }

   public ExtendedPathNodeType getExtendedDefaultNodeType(BlockGetter world, int x, int y, int z) {
      return getLandNodeType(world, new MutableBlockPos(x, y, z));
   }

   public static ExtendedPathNodeType getLandNodeType(BlockGetter world, MutableBlockPos pos) {
      int x = pos.m_123341_();
      int y = pos.m_123342_();
      int z = pos.m_123343_();
      ExtendedPathNodeType pathNodeType = getCommonNodeType(world, pos);
      if (pathNodeType == ExtendedPathNodeType.OPEN && y >= world.m_141937_() + 1) {
         ExtendedPathNodeType floorType = getCommonNodeType(world, pos.m_122178_(x, y - 1, z));
         pathNodeType = !floorType.isWalkable()
               && floorType != ExtendedPathNodeType.OPEN
               && floorType != ExtendedPathNodeType.WATER
               && floorType != ExtendedPathNodeType.LAVA
            ? ExtendedPathNodeType.WALKABLE
            : ExtendedPathNodeType.OPEN;
         if (floorType == ExtendedPathNodeType.PATH) {
            pathNodeType = ExtendedPathNodeType.WALKABLE_PATH;
         }

         if (floorType == ExtendedPathNodeType.GRASS) {
            pathNodeType = ExtendedPathNodeType.WALKABLE_GRASS;
         }

         if (floorType == ExtendedPathNodeType.DAMAGE_FIRE) {
            pathNodeType = ExtendedPathNodeType.DAMAGE_FIRE;
         }

         if (floorType == ExtendedPathNodeType.DAMAGE_OTHER) {
            pathNodeType = ExtendedPathNodeType.DAMAGE_OTHER;
         }

         if (floorType == ExtendedPathNodeType.STICKY_HONEY) {
            pathNodeType = ExtendedPathNodeType.STICKY_HONEY;
         }

         if (floorType == ExtendedPathNodeType.POWDER_SNOW) {
            pathNodeType = ExtendedPathNodeType.DANGER_POWDER_SNOW;
         }
      }

      if (pathNodeType.isWalkable()) {
         pathNodeType = getNodeTypeFromNeighbors(world, pos.m_122178_(x, y, z), pathNodeType);
      }

      return pathNodeType;
   }

   public static ExtendedPathNodeType getNodeTypeFromNeighbors(BlockGetter world, MutableBlockPos pos, ExtendedPathNodeType nodeType) {
      int x = pos.m_123341_();
      int y = pos.m_123342_();
      int z = pos.m_123343_();

      for (int l = -1; l <= 1; l++) {
         for (int m = -1; m <= 1; m++) {
            for (int n = -1; n <= 1; n++) {
               if (l != 0 || n != 0) {
                  pos.m_122178_(x + l, y + m, z + n);
                  BlockState blockState = world.m_8055_(pos);
                  if (blockState.m_60713_(Blocks.f_50128_) || blockState.m_60713_(Blocks.f_50685_)) {
                     return ExtendedPathNodeType.DANGER_OTHER;
                  }

                  if (inflictsFireDamage(blockState)) {
                     return ExtendedPathNodeType.DANGER_FIRE;
                  }

                  if (world.m_6425_(pos).m_205070_(FluidTags.f_13131_)) {
                     return ExtendedPathNodeType.WATER_BORDER;
                  }
               }
            }
         }
      }

      return nodeType;
   }

   protected static ExtendedPathNodeType getCommonNodeType(BlockGetter world, BlockPos pos) {
      BlockState blockState = world.m_8055_(pos);
      Block block = blockState.m_60734_();
      if (block instanceof DirtPathBlock) {
         return ExtendedPathNodeType.PATH;
      }

      if (block instanceof GrassBlock) {
         return ExtendedPathNodeType.GRASS;
      }

      if (blockState.m_60795_()) {
         return ExtendedPathNodeType.OPEN;
      }

      if (blockState.m_204336_(BlockTags.f_13036_) || blockState.m_60713_(Blocks.f_50196_) || blockState.m_60713_(Blocks.f_152545_)) {
         return ExtendedPathNodeType.TRAPDOOR;
      }

      if (blockState.m_60713_(Blocks.f_152499_)) {
         return ExtendedPathNodeType.POWDER_SNOW;
      }

      if (blockState.m_60713_(Blocks.f_50128_) || blockState.m_60713_(Blocks.f_50685_)) {
         return ExtendedPathNodeType.DAMAGE_OTHER;
      }

      if (blockState.m_60713_(Blocks.f_50719_)) {
         return ExtendedPathNodeType.STICKY_HONEY;
      }

      if (blockState.m_60713_(Blocks.f_50262_)) {
         return ExtendedPathNodeType.COCOA;
      }

      FluidState fluidState = world.m_6425_(pos);
      if (fluidState.m_205070_(FluidTags.f_13132_)) {
         return ExtendedPathNodeType.LAVA;
      }

      if (inflictsFireDamage(blockState)) {
         return ExtendedPathNodeType.DAMAGE_FIRE;
      }

      if (block instanceof DoorBlock doorBlock) {
         if ((Boolean)blockState.m_61143_(DoorBlock.f_52727_)) {
            return ExtendedPathNodeType.DOOR_OPEN;
         } else {
            return doorBlock.m_278711_().f_278463_() ? ExtendedPathNodeType.DOOR_WOOD_CLOSED : ExtendedPathNodeType.DOOR_IRON_CLOSED;
         }
      } else {
         if (block instanceof BaseRailBlock) {
            return ExtendedPathNodeType.RAIL;
         }

         if (block instanceof LeavesBlock) {
            return ExtendedPathNodeType.LEAVES;
         }

         if (!blockState.m_204336_(BlockTags.f_13039_)
            && !blockState.m_204336_(BlockTags.f_13032_)
            && (!Config.getInstance().useSmarterDoorAI || !(block instanceof FenceGateBlock) || (Boolean)blockState.m_61143_(FenceGateBlock.f_53341_))) {
            if (!blockState.m_60647_(world, pos, PathComputationType.LAND)) {
               return ExtendedPathNodeType.BLOCKED;
            } else {
               return fluidState.m_205070_(FluidTags.f_13131_) ? ExtendedPathNodeType.WATER : ExtendedPathNodeType.OPEN;
            }
         } else {
            return ExtendedPathNodeType.FENCE;
         }
      }
   }

   public static boolean inflictsFireDamage(BlockState state) {
      return WalkNodeEvaluator.m_77622_(state);
   }
}
