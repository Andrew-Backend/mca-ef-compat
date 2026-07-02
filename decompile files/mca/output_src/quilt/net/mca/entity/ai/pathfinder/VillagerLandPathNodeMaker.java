package quilt.net.mca.entity.ai.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import net.minecraft.class_10;
import net.minecraft.class_1308;
import net.minecraft.class_14;
import net.minecraft.class_1922;
import net.minecraft.class_1950;
import net.minecraft.class_2241;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2323;
import net.minecraft.class_2338;
import net.minecraft.class_2349;
import net.minecraft.class_2350;
import net.minecraft.class_2369;
import net.minecraft.class_2372;
import net.minecraft.class_238;
import net.minecraft.class_2397;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3612;
import net.minecraft.class_4459;
import net.minecraft.class_7;
import net.minecraft.class_8;
import net.minecraft.class_9;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_2350.class_2351;
import quilt.net.mca.Config;

public class VillagerLandPathNodeMaker extends class_8 {
   protected float waterPathNodeTypeWeight;
   private final Long2ObjectMap<ExtendedPathNodeType> nodeTypes = new Long2ObjectOpenHashMap();
   private final Object2BooleanMap<class_238> collidedBoxes = new Object2BooleanOpenHashMap();

   public void method_12(class_1950 cachedWorld, class_1308 entity) {
      super.method_12(cachedWorld, entity);
      this.waterPathNodeTypeWeight = this.getPenalty(entity, ExtendedPathNodeType.WATER);
   }

   public void method_19() {
      this.field_33.method_5941(ExtendedPathNodeType.WATER.toVanilla(), this.waterPathNodeTypeWeight);
      this.nodeTypes.clear();
      this.collidedBoxes.clear();
      super.method_19();
   }

   public class_9 method_21() {
      class_2339 mutable = new class_2339();
      int i = this.field_33.method_31478();
      class_2680 blockState = this.field_20622.method_8320(mutable.method_10102(this.field_33.method_23317(), i, this.field_33.method_23321()));
      if (!this.field_33.method_26319(blockState.method_26227())) {
         if (this.method_22() && this.field_33.method_5799()) {
            while (true) {
               if (!blockState.method_27852(class_2246.field_10382) && blockState.method_26227() != class_3612.field_15910.method_15729(false)) {
                  i--;
                  break;
               }

               blockState = this.field_20622.method_8320(mutable.method_10102(this.field_33.method_23317(), ++i, this.field_33.method_23321()));
            }
         } else if (this.field_33.method_24828()) {
            i = class_3532.method_15357(this.field_33.method_23318() + 0.5);
         } else {
            class_2338 blockPos = this.field_33.method_24515();

            while (
               (
                     this.field_20622.method_8320(blockPos).method_26215()
                        || this.field_20622.method_8320(blockPos).method_26171(this.field_20622, blockPos, class_10.field_50)
                  )
                  && blockPos.method_10264() > this.field_33.method_37908().method_31607()
            ) {
               blockPos = blockPos.method_10074();
            }

            i = blockPos.method_10084().method_10264();
         }
      } else {
         while (this.field_33.method_26319(blockState.method_26227())) {
            blockState = this.field_20622.method_8320(mutable.method_10102(this.field_33.method_23317(), ++i, this.field_33.method_23321()));
         }

         i--;
      }

      class_2338 blockPos = this.field_33.method_24515();
      ExtendedPathNodeType pathNodeType = this.getNodeType(this.field_33, blockPos.method_10263(), i, blockPos.method_10260());
      if (this.getPenalty(pathNodeType) < 0.0F) {
         class_238 box = this.field_33.method_5829();
         if (this.canPathThrough(mutable.method_10102(box.field_1323, i, box.field_1321))
            || this.canPathThrough(mutable.method_10102(box.field_1323, i, box.field_1324))
            || this.canPathThrough(mutable.method_10102(box.field_1320, i, box.field_1321))
            || this.canPathThrough(mutable.method_10102(box.field_1320, i, box.field_1324))) {
            class_9 pathNode = this.method_27137(mutable);
            ExtendedPathNodeType type = this.getNodeType(this.field_33, pathNode.method_22879());
            pathNode.field_41 = type.toVanilla();
            pathNode.field_43 = this.getPenalty(type);
            return pathNode;
         }
      }

      class_9 pathNode2 = this.method_13(blockPos.method_10263(), i, blockPos.method_10260());
      ExtendedPathNodeType type = this.getNodeType(this.field_33, pathNode2.method_22879());
      pathNode2.field_41 = type.toVanilla();
      pathNode2.field_43 = this.getPenalty(type);
      return pathNode2;
   }

   private boolean canPathThrough(class_2338 pos) {
      ExtendedPathNodeType pathNodeType = this.getNodeType(this.field_33, pos);
      return this.getPenalty(pathNodeType) >= 0.0F;
   }

   public class_4459 method_16(double x, double y, double z) {
      return new class_4459(this.method_13(class_3532.method_15357(x), class_3532.method_15357(y), class_3532.method_15357(z)));
   }

   public int method_18(class_9[] successors, class_9 node) {
      ExtendedPathNodeType pathNodeTypeHead = this.getNodeType(this.field_33, node.field_40, node.field_39 + 1, node.field_38);
      ExtendedPathNodeType pathNodeType = this.getNodeType(this.field_33, node.field_40, node.field_39, node.field_38);
      double feetY = this.getFeetY(new class_2338(node.field_40, node.field_39, node.field_38));
      int maxYStep = 0;
      if (this.getPenalty(pathNodeTypeHead) >= 0.0F && pathNodeType != ExtendedPathNodeType.STICKY_HONEY) {
         maxYStep = class_3532.method_15375(Math.max(1.0F, this.field_33.method_49476()));
      }

      class_9 pathNode1 = this.getPathNode(node.field_40, node.field_39, node.field_38 + 1, maxYStep, feetY, class_2350.field_11035, pathNodeType);
      class_9 pathNode2 = this.getPathNode(node.field_40 - 1, node.field_39, node.field_38, maxYStep, feetY, class_2350.field_11039, pathNodeType);
      class_9 pathNode3 = this.getPathNode(node.field_40 + 1, node.field_39, node.field_38, maxYStep, feetY, class_2350.field_11034, pathNodeType);
      class_9 pathNode4 = this.getPathNode(node.field_40, node.field_39, node.field_38 - 1, maxYStep, feetY, class_2350.field_11043, pathNodeType);
      class_9 pathNode5 = this.getPathNode(node.field_40 - 1, node.field_39, node.field_38 - 1, maxYStep, feetY, class_2350.field_11043, pathNodeType);
      class_9 pathNode6 = this.getPathNode(node.field_40 + 1, node.field_39, node.field_38 - 1, maxYStep, feetY, class_2350.field_11043, pathNodeType);
      class_9 pathNode7 = this.getPathNode(node.field_40 - 1, node.field_39, node.field_38 + 1, maxYStep, feetY, class_2350.field_11035, pathNodeType);
      class_9 pathNode8 = this.getPathNode(node.field_40 + 1, node.field_39, node.field_38 + 1, maxYStep, feetY, class_2350.field_11035, pathNodeType);
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

   protected boolean isValidAdjacentSuccessor(class_9 node, class_9 successor1) {
      return node != null && !node.field_42 && (node.field_43 >= 0.0F || successor1.field_43 < 0.0F);
   }

   protected boolean isValidDiagonalSuccessor(class_9 xNode, class_9 zNode, class_9 xDiagNode, class_9 zDiagNode) {
      if (zDiagNode == null || xDiagNode == null || zNode == null) {
         return false;
      }

      if (zDiagNode.field_42) {
         return false;
      }

      if (xDiagNode.field_39 <= xNode.field_39 && zNode.field_39 <= xNode.field_39) {
         if (zNode.field_41 != ExtendedPathNodeType.WALKABLE_DOOR.toVanilla()
            && xDiagNode.field_41 != ExtendedPathNodeType.WALKABLE_DOOR.toVanilla()
            && zDiagNode.field_41 != ExtendedPathNodeType.WALKABLE_DOOR.toVanilla()) {
            boolean bl = xDiagNode.field_41 == ExtendedPathNodeType.FENCE.toVanilla()
               && zNode.field_41 == ExtendedPathNodeType.FENCE.toVanilla()
               && this.field_33.method_17681() < 0.5;
            return zDiagNode.field_43 >= 0.0F
               && (xDiagNode.field_39 < xNode.field_39 || xDiagNode.field_43 >= 0.0F || bl)
               && (zNode.field_39 < xNode.field_39 || zNode.field_43 >= 0.0F || bl);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isBlocked(class_9 node) {
      class_243 vec3d = new class_243(
         node.field_40 - this.field_33.method_23317(), node.field_39 - this.field_33.method_23318(), node.field_38 - this.field_33.method_23321()
      );
      class_238 box = this.field_33.method_5829();
      int i = class_3532.method_15384(vec3d.method_1033() / box.method_995());
      vec3d = vec3d.method_1021(1.0F / i);

      for (int j = 1; j <= i; j++) {
         if (this.checkBoxCollision(box = box.method_997(vec3d))) {
            return false;
         }
      }

      return true;
   }

   protected double getFeetY(class_2338 pos) {
      return getFeetY(this.field_20622, pos);
   }

   public static double getFeetY(class_1922 world, class_2338 pos) {
      class_2338 blockPos = pos.method_10074();
      class_265 voxelShape = world.method_8320(blockPos).method_26220(world, blockPos);
      return blockPos.method_10264() + (voxelShape.method_1110() ? 0.0 : voxelShape.method_1105(class_2351.field_11052));
   }

   float getPenalty(ExtendedPathNodeType pathNodeType) {
      return this.getPenalty(this.field_33, pathNodeType);
   }

   private float getPenalty(class_1308 mob, ExtendedPathNodeType type) {
      return mob.method_5944(type.toVanilla()) + type.getBonusPenalty();
   }

   protected class_9 getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, class_2350 direction, ExtendedPathNodeType nodeType) {
      class_2339 mutable = new class_2339();
      double step = this.getFeetY(mutable.method_10103(x, y, z));
      if (step - prevFeetY > 1.125) {
         return null;
      }

      ExtendedPathNodeType pathNodeType = this.getNodeType(this.field_33, x, y, z);
      float penalty = this.getPenalty(pathNodeType);
      double e = this.field_33.method_17681() / 2.0;
      class_9 pathNode = null;
      if (penalty >= 0.0F) {
         pathNode = this.method_13(x, y, z);
         pathNode.field_41 = pathNodeType.toVanilla();
         pathNode.field_43 = Math.max(pathNode.field_43, penalty);
      }

      if (nodeType == ExtendedPathNodeType.FENCE && pathNode != null && pathNode.field_43 >= 0.0F && !this.isBlocked(pathNode)) {
         pathNode = null;
      }

      if (pathNodeType.isWalkable()) {
         return pathNode;
      }

      if ((pathNode == null || pathNode.field_43 < 0.0F)
         && maxYStep > 0
         && pathNodeType != ExtendedPathNodeType.FENCE
         && pathNodeType != ExtendedPathNodeType.UNPASSABLE_RAIL
         && pathNodeType != ExtendedPathNodeType.TRAPDOOR
         && pathNodeType != ExtendedPathNodeType.POWDER_SNOW) {
         pathNode = this.getPathNode(x, y + 1, z, maxYStep - 1, prevFeetY, direction, nodeType);
         double h;
         double g;
         if (pathNode != null
            && (pathNode.field_41 == ExtendedPathNodeType.OPEN.toVanilla() || pathNode.field_41 == ExtendedPathNodeType.WALKABLE.toVanilla())
            && this.field_33.method_17681() < 1.0F
            && this.checkBoxCollision(
               new class_238(
                  (g = x - direction.method_10148() + 0.5) - e,
                  getFeetY(this.field_20622, mutable.method_10102(g, y + 1, h = z - direction.method_10165() + 0.5)) + 0.001,
                  h - e,
                  g + e,
                  this.field_33.method_17682()
                     + getFeetY(this.field_20622, mutable.method_10102(pathNode.field_40, pathNode.field_39, pathNode.field_38))
                     - 0.002,
                  h + e
               )
            )) {
            pathNode = null;
         }
      }

      if (pathNodeType == ExtendedPathNodeType.WATER && !this.method_22()) {
         if (this.getNodeType(this.field_33, x, y - 1, z) != ExtendedPathNodeType.WATER) {
            return pathNode;
         }

         while (y > this.field_33.method_37908().method_31607()) {
            if ((pathNodeType = this.getNodeType(this.field_33, x, --y, z)) != ExtendedPathNodeType.WATER) {
               return pathNode;
            }

            pathNode = this.method_13(x, y, z);
            pathNode.field_41 = pathNodeType.toVanilla();
            pathNode.field_43 = Math.max(pathNode.field_43, this.getPenalty(pathNodeType));
         }
      }

      if (pathNodeType == ExtendedPathNodeType.OPEN) {
         int i = 0;
         int j = y;

         while (pathNodeType == ExtendedPathNodeType.OPEN) {
            if (--y < this.field_33.method_37908().method_31607()) {
               class_9 pathNode2 = this.method_13(x, j, z);
               pathNode2.field_41 = ExtendedPathNodeType.BLOCKED.toVanilla();
               pathNode2.field_43 = -1.0F;
               return pathNode2;
            }

            if (i++ >= this.field_33.method_5850()) {
               class_9 pathNode2 = this.method_13(x, y, z);
               pathNode2.field_41 = ExtendedPathNodeType.BLOCKED.toVanilla();
               pathNode2.field_43 = -1.0F;
               return pathNode2;
            }

            pathNodeType = this.getNodeType(this.field_33, x, y, z);
            penalty = this.getPenalty(pathNodeType);
            if (pathNodeType != ExtendedPathNodeType.OPEN && penalty >= 0.0F) {
               pathNode = this.method_13(x, y, z);
               pathNode.field_41 = pathNodeType.toVanilla();
               pathNode.field_43 = Math.max(pathNode.field_43, penalty);
               break;
            }

            if (penalty < 0.0F) {
               class_9 pathNode2 = this.method_13(x, y, z);
               pathNode2.field_41 = ExtendedPathNodeType.BLOCKED.toVanilla();
               pathNode2.field_43 = -1.0F;
               return pathNode2;
            }
         }
      }

      if (pathNodeType == ExtendedPathNodeType.FENCE) {
         pathNode = this.method_13(x, y, z);
         pathNode.field_42 = true;
         pathNode.field_41 = pathNodeType.toVanilla();
         pathNode.field_43 = pathNodeType.getDefaultPenalty();
      }

      return pathNode;
   }

   private boolean checkBoxCollision(class_238 box) {
      return this.collidedBoxes.computeIfAbsent(box, object -> !this.field_20622.method_8587(this.field_33, box));
   }

   public class_7 method_25(class_1922 world, int x, int y, int z, class_1308 mob) {
      return this.getExtendedNodeType(world, x, y, z, mob, this.field_31, this.field_30, this.field_28, this.field_27, this.field_29).toVanilla();
   }

   public ExtendedPathNodeType getExtendedNodeType(
      class_1922 world, int x, int y, int z, class_1308 mob, int sizeX, int sizeY, int sizeZ, boolean canOpenDoors, boolean canEnterOpenDoors
   ) {
      EnumSet<ExtendedPathNodeType> enumSet = EnumSet.noneOf(ExtendedPathNodeType.class);
      ExtendedPathNodeType centerPathNodeType = this.findNearbyNodeTypes(
         world, x, y, z, sizeX, sizeY, sizeZ, canOpenDoors, canEnterOpenDoors, enumSet, mob.method_24515()
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
      class_1922 world,
      int x,
      int y,
      int z,
      int sizeX,
      int sizeY,
      int sizeZ,
      boolean canOpenDoors,
      boolean canEnterOpenDoors,
      EnumSet<ExtendedPathNodeType> nearbyTypes,
      class_2338 pos
   ) {
      class_2339 p = new class_2339(x, y, z);
      ExtendedPathNodeType type = ExtendedPathNodeType.BLOCKED;

      for (int i = 0; i < sizeX; i++) {
         for (int j = 0; j < sizeY; j++) {
            for (int k = 0; k < sizeZ; k++) {
               int l = i + x;
               int m = j + y;
               int n = k + z;
               p.method_10103(l, m, n);
               class_2680 blockState = world.method_8320(p);
               ExtendedPathNodeType pathNodeType = this.getExtendedDefaultNodeType(world, l, m, n);
               pathNodeType = this.adjustNodeType(world, canOpenDoors, canEnterOpenDoors, pos, pathNodeType);
               if (Config.getInstance().useSmarterDoorAI
                  && blockState.method_27851(class_3481.field_25147, state -> state.method_26204() instanceof class_2349)) {
                  pathNodeType = ExtendedPathNodeType.WALKABLE_DOOR;
               }

               if (pathNodeType != ExtendedPathNodeType.DOOR_OPEN && blockState.method_26204() instanceof class_2323) {
                  for (class_2338 adjacent : class_2338.method_10094(l - 1, m - 1, n - 1, l + 1, m + 1, n + 1)) {
                     if (world.method_8320(adjacent).method_26164(class_3481.field_24076)) {
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

   protected ExtendedPathNodeType adjustNodeType(class_1922 world, boolean canOpenDoors, boolean canEnterOpenDoors, class_2338 pos, ExtendedPathNodeType type) {
      if (type == ExtendedPathNodeType.DOOR_WOOD_CLOSED && canOpenDoors && canEnterOpenDoors) {
         type = ExtendedPathNodeType.WALKABLE_DOOR;
      }

      if (type == ExtendedPathNodeType.DOOR_OPEN && !canEnterOpenDoors) {
         type = ExtendedPathNodeType.BLOCKED;
      }

      if (type == ExtendedPathNodeType.RAIL
         && !(world.method_8320(pos).method_26204() instanceof class_2241)
         && !(world.method_8320(pos.method_10074()).method_26204() instanceof class_2241)) {
         type = ExtendedPathNodeType.UNPASSABLE_RAIL;
      }

      if (type == ExtendedPathNodeType.LEAVES) {
         type = ExtendedPathNodeType.BLOCKED;
      }

      return type;
   }

   private ExtendedPathNodeType getNodeType(class_1308 entity, class_2338 pos) {
      return this.getNodeType(entity, pos.method_10263(), pos.method_10264(), pos.method_10260());
   }

   protected ExtendedPathNodeType getNodeType(class_1308 entity, int x, int y, int z) {
      return (ExtendedPathNodeType)this.nodeTypes
         .computeIfAbsent(
            class_2338.method_10064(x, y, z),
            l -> this.getExtendedNodeType(this.field_20622, x, y, z, entity, this.field_31, this.field_30, this.field_28, this.method_24(), this.method_23())
         );
   }

   public class_7 method_17(class_1922 world, int x, int y, int z) {
      return this.getExtendedDefaultNodeType(world, x, y, z).toVanilla();
   }

   public ExtendedPathNodeType getExtendedDefaultNodeType(class_1922 world, int x, int y, int z) {
      return getLandNodeType(world, new class_2339(x, y, z));
   }

   public static ExtendedPathNodeType getLandNodeType(class_1922 world, class_2339 pos) {
      int x = pos.method_10263();
      int y = pos.method_10264();
      int z = pos.method_10260();
      ExtendedPathNodeType pathNodeType = getCommonNodeType(world, pos);
      if (pathNodeType == ExtendedPathNodeType.OPEN && y >= world.method_31607() + 1) {
         ExtendedPathNodeType floorType = getCommonNodeType(world, pos.method_10103(x, y - 1, z));
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
         pathNodeType = getNodeTypeFromNeighbors(world, pos.method_10103(x, y, z), pathNodeType);
      }

      return pathNodeType;
   }

   public static ExtendedPathNodeType getNodeTypeFromNeighbors(class_1922 world, class_2339 pos, ExtendedPathNodeType nodeType) {
      int x = pos.method_10263();
      int y = pos.method_10264();
      int z = pos.method_10260();

      for (int l = -1; l <= 1; l++) {
         for (int m = -1; m <= 1; m++) {
            for (int n = -1; n <= 1; n++) {
               if (l != 0 || n != 0) {
                  pos.method_10103(x + l, y + m, z + n);
                  class_2680 blockState = world.method_8320(pos);
                  if (blockState.method_27852(class_2246.field_10029) || blockState.method_27852(class_2246.field_16999)) {
                     return ExtendedPathNodeType.DANGER_OTHER;
                  }

                  if (inflictsFireDamage(blockState)) {
                     return ExtendedPathNodeType.DANGER_FIRE;
                  }

                  if (world.method_8316(pos).method_15767(class_3486.field_15517)) {
                     return ExtendedPathNodeType.WATER_BORDER;
                  }
               }
            }
         }
      }

      return nodeType;
   }

   protected static ExtendedPathNodeType getCommonNodeType(class_1922 world, class_2338 pos) {
      class_2680 blockState = world.method_8320(pos);
      class_2248 block = blockState.method_26204();
      if (block instanceof class_2369) {
         return ExtendedPathNodeType.PATH;
      }

      if (block instanceof class_2372) {
         return ExtendedPathNodeType.GRASS;
      }

      if (blockState.method_26215()) {
         return ExtendedPathNodeType.OPEN;
      }

      if (blockState.method_26164(class_3481.field_15487) || blockState.method_27852(class_2246.field_10588) || blockState.method_27852(class_2246.field_28682)
         )
       {
         return ExtendedPathNodeType.TRAPDOOR;
      }

      if (blockState.method_27852(class_2246.field_27879)) {
         return ExtendedPathNodeType.POWDER_SNOW;
      }

      if (blockState.method_27852(class_2246.field_10029) || blockState.method_27852(class_2246.field_16999)) {
         return ExtendedPathNodeType.DAMAGE_OTHER;
      }

      if (blockState.method_27852(class_2246.field_21211)) {
         return ExtendedPathNodeType.STICKY_HONEY;
      }

      if (blockState.method_27852(class_2246.field_10302)) {
         return ExtendedPathNodeType.COCOA;
      }

      class_3610 fluidState = world.method_8316(pos);
      if (fluidState.method_15767(class_3486.field_15518)) {
         return ExtendedPathNodeType.LAVA;
      }

      if (inflictsFireDamage(blockState)) {
         return ExtendedPathNodeType.DAMAGE_FIRE;
      }

      if (block instanceof class_2323 doorBlock) {
         if ((Boolean)blockState.method_11654(class_2323.field_10945)) {
            return ExtendedPathNodeType.DOOR_OPEN;
         } else {
            return doorBlock.method_51169().comp_1471() ? ExtendedPathNodeType.DOOR_WOOD_CLOSED : ExtendedPathNodeType.DOOR_IRON_CLOSED;
         }
      } else {
         if (block instanceof class_2241) {
            return ExtendedPathNodeType.RAIL;
         }

         if (block instanceof class_2397) {
            return ExtendedPathNodeType.LEAVES;
         }

         if (!blockState.method_26164(class_3481.field_16584)
            && !blockState.method_26164(class_3481.field_15504)
            && (!Config.getInstance().useSmarterDoorAI || !(block instanceof class_2349) || (Boolean)blockState.method_11654(class_2349.field_11026))) {
            if (!blockState.method_26171(world, pos, class_10.field_50)) {
               return ExtendedPathNodeType.BLOCKED;
            } else {
               return fluidState.method_15767(class_3486.field_15517) ? ExtendedPathNodeType.WATER : ExtendedPathNodeType.OPEN;
            }
         } else {
            return ExtendedPathNodeType.FENCE;
         }
      }
   }

   public static boolean inflictsFireDamage(class_2680 state) {
      return class_14.method_27138(state);
   }
}
