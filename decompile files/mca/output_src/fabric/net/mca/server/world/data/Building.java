package fabric.net.mca.server.world.data;

import fabric.net.mca.Config;
import fabric.net.mca.resources.BuildingTypes;
import fabric.net.mca.resources.data.BuildingType;
import fabric.net.mca.util.NbtHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.class_1937;
import net.minecraft.class_2244;
import net.minecraft.class_2248;
import net.minecraft.class_2323;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_2487;
import net.minecraft.class_2680;
import net.minecraft.class_2742;
import net.minecraft.class_2960;
import net.minecraft.class_3481;
import net.minecraft.class_7923;

public class Building implements Serializable {
   private static final long serialVersionUID = -1106627083469687307L;
   public static final long SCAN_COOLDOWN = 4800L;
   private static final class_2350[] directions = new class_2350[]{
      class_2350.field_11036, class_2350.field_11033, class_2350.field_11043, class_2350.field_11034, class_2350.field_11035, class_2350.field_11039
   };
   private final Map<class_2960, List<class_2338>> blocks = new HashMap<>();
   private String type = "building";
   private boolean isTypeForced = false;
   private int size;
   private int pos0X;
   private int pos0Y;
   private int pos0Z;
   private int pos1X;
   private int pos1Y;
   private int pos1Z;
   private int posX;
   private int posY;
   private int posZ;
   private int id;
   private boolean strictScan;
   private long lastScan;

   public Building() {
   }

   public Building(class_2338 pos) {
      this(pos, false);
   }

   public Building(class_2338 pos, boolean strictScan) {
      this();
      this.pos0X = pos.method_10263();
      this.pos0Y = pos.method_10264();
      this.pos0Z = pos.method_10260();
      this.pos1X = this.pos0X;
      this.pos1Y = this.pos0Y;
      this.pos1Z = this.pos0Z;
      this.posX = this.pos0X;
      this.posY = this.pos0Y;
      this.posZ = this.pos0Z;
      this.strictScan = strictScan;
   }

   public Building(class_2487 v) {
      this.id = v.method_10550("id");
      this.size = v.method_10550("size");
      this.pos0X = v.method_10550("pos0X");
      this.pos0Y = v.method_10550("pos0Y");
      this.pos0Z = v.method_10550("pos0Z");
      this.pos1X = v.method_10550("pos1X");
      this.pos1Y = v.method_10550("pos1Y");
      this.pos1Z = v.method_10550("pos1Z");
      if (v.method_10545("posX")) {
         this.posX = v.method_10550("posX");
         this.posY = v.method_10550("posY");
         this.posZ = v.method_10550("posZ");
      } else {
         class_2338 center = this.getCenter();
         this.posX = center.method_10263();
         this.posY = center.method_10264();
         this.posZ = center.method_10260();
      }

      this.isTypeForced = v.method_10577("isTypeForced");
      this.type = v.method_10558("type");
      this.strictScan = v.method_10577("strictScan");
      this.blocks.putAll(NbtHelper.toMap(v.method_10562("blocks2"), class_2960::new, l -> NbtHelper.toList(l, e -> {
         class_2487 c = (class_2487)e;
         return new class_2338(c.method_10550("x"), c.method_10550("y"), c.method_10550("z"));
      })));
   }

   public class_2487 save() {
      class_2487 v = new class_2487();
      v.method_10569("id", this.id);
      v.method_10569("size", this.size);
      v.method_10569("pos0X", this.pos0X);
      v.method_10569("pos0Y", this.pos0Y);
      v.method_10569("pos0Z", this.pos0Z);
      v.method_10569("pos1X", this.pos1X);
      v.method_10569("pos1Y", this.pos1Y);
      v.method_10569("pos1Z", this.pos1Z);
      v.method_10569("posX", this.posX);
      v.method_10569("posY", this.posY);
      v.method_10569("posZ", this.posZ);
      v.method_10556("isTypeForced", this.isTypeForced);
      v.method_10582("type", this.type);
      v.method_10556("strictScan", this.strictScan);
      class_2487 b = new class_2487();
      NbtHelper.fromMap(b, this.blocks, class_2960::toString, e -> NbtHelper.fromList(e, p -> {
         class_2487 entry = new class_2487();
         entry.method_10569("x", p.method_10263());
         entry.method_10569("y", p.method_10264());
         entry.method_10569("z", p.method_10260());
         return entry;
      }));
      v.method_10566("blocks2", b);
      return v;
   }

   public class_2338 getPos0() {
      int margin = this.getBuildingType().getMargin();
      return new class_2338(this.pos0X, this.pos0Y, this.pos0Z).method_10059(new class_2382(margin, margin, margin));
   }

   public class_2338 getPos1() {
      int margin = this.getBuildingType().getMargin();
      return new class_2338(this.pos1X, this.pos1Y, this.pos1Z).method_10081(new class_2382(margin, margin, margin));
   }

   public class_2338 getCenter() {
      return new class_2338((this.pos0X + this.pos1X) / 2, (this.pos0Y + this.pos1Y) / 2, (this.pos0Z + this.pos1Z) / 2);
   }

   public class_2338 getSourceBlock() {
      return new class_2338(this.posX, this.posY, this.posZ);
   }

   public void validateBlocks(class_1937 world) {
      this.setLastScan(world.method_8510());

      for (Entry<class_2960, List<class_2338>> positions : this.blocks.entrySet()) {
         List<class_2338> mask = positions.getValue()
            .stream()
            .filter(p -> !class_7923.field_41175.method_10221(world.method_8320(p).method_26204()).equals(positions.getKey()))
            .toList();
         positions.getValue().removeAll(mask);
      }
   }

   public Stream<class_2338> getBlockPosStream() {
      return this.blocks.values().stream().flatMap(Collection::stream);
   }

   public void addPOI(class_1937 world, class_2338 pos) {
      class_2248 block = world.method_8320(pos).method_26204();
      this.removeBlock(block, pos);
      this.addBlock(block, pos);
      this.validateBlocks(world);
      int n = (int)this.getBlockPosStream().count();
      if (n > 0) {
         class_2338 center = this.getBlockPosStream().reduce(class_2338.field_10980, class_2338::method_10081);
         this.pos0X = center.method_10263() / n;
         this.pos0Y = center.method_10264() / n;
         this.pos0Z = center.method_10260() / n;
         this.pos1X = this.pos0X;
         this.pos1Y = this.pos0Y;
         this.pos1Z = this.pos0Z;
      }
   }

   public Building.validationResult validateBuilding(class_1937 world, Set<class_2338> blocked) {
      if (this.getBuildingType().grouped()) {
         this.validateBlocks(world);
         return this.getBlockPosStream().findAny().isEmpty() ? Building.validationResult.TOO_SMALL : Building.validationResult.SUCCESS;
      }

      this.blocks.clear();
      this.size = 0;
      this.setLastScan(world.method_8510());
      Set<class_2338> done = new HashSet<>();
      LinkedList<class_2338> queue = new LinkedList<>();
      class_2338 center = this.getSourceBlock();
      queue.add(center);
      done.add(center);
      int minSize = Config.getInstance().minBuildingSize;
      int maxSize = Config.getInstance().maxBuildingSize;
      int maxRadius = Config.getInstance().maxBuildingRadius;
      int scanSize = 0;
      int interiorSize = 0;
      boolean hasDoor = false;
      Map<class_2338, Boolean> roofCache = new HashMap<>();

      while (!queue.isEmpty() && scanSize < maxSize) {
         class_2338 p = queue.removeLast();
         if (blocked.contains(p) && scanSize > 0) {
            return Building.validationResult.OVERLAP;
         }

         if (p.method_19455(center) >= maxRadius) {
            return Building.validationResult.SIZE_LIMIT;
         }

         for (class_2350 d : directions) {
            class_2338 n = p.method_10093(d);
            if (!done.contains(n)) {
               class_2680 state = world.method_8320(n);
               done.add(n);
               if (!state.method_26215()) {
                  if (state.method_26204() instanceof class_2323) {
                     if (!this.strictScan) {
                        queue.add(n);
                     }

                     hasDoor = true;
                  }
               } else {
                  if (!roofCache.containsKey(n)) {
                     class_2338 n2 = n;
                     int maxScanHeight = 16;

                     for (int i = 0; i < maxScanHeight; i++) {
                        roofCache.put(n2, false);
                        n2 = n2.method_10084();
                        class_2680 block = world.method_8320(n2);
                        if (!block.method_26215() || roofCache.containsKey(n2)) {
                           if ((!roofCache.containsKey(n2) || roofCache.get(n2)) && !block.method_26164(class_3481.field_15503)) {
                              for (int i2 = i; i2 >= 0; i2--) {
                                 n2 = n2.method_10074();
                                 roofCache.put(n2, true);
                              }
                           }
                           break;
                        }
                     }
                  }

                  if (roofCache.get(n)) {
                     interiorSize++;
                     queue.add(n);
                  }
               }
            }
         }

         scanSize++;
      }

      if (!queue.isEmpty()) {
         return Building.validationResult.BLOCK_LIMIT;
      }

      if (done.size() <= minSize) {
         return Building.validationResult.TOO_SMALL;
      }

      if (!hasDoor) {
         return Building.validationResult.NO_DOOR;
      }

      int sx = center.method_10263();
      int sy = center.method_10264();
      int sz = center.method_10260();
      int ex = sx;
      int ey = sy;
      int ez = sz;

      for (class_2338 p : done) {
         sx = Math.min(sx, p.method_10263());
         sy = Math.min(sy, p.method_10264());
         sz = Math.min(sz, p.method_10260());
         ex = Math.max(ex, p.method_10263());
         ey = Math.max(ey, p.method_10264());
         ez = Math.max(ez, p.method_10260());
         class_2680 blockState = world.method_8320(p);
         class_2248 block = blockState.method_26204();
         if (this.isBuildingBlock(class_7923.field_41175.method_10221(block))) {
            if (block instanceof class_2244) {
               if (blockState.method_11654(class_2244.field_9967) == class_2742.field_12560) {
                  this.addBlock(block, p);
               }
            } else {
               this.addBlock(block, p);
            }
         }
      }

      this.pos0X = sx;
      this.pos0Y = sy;
      this.pos0Z = sz;
      this.pos1X = ex;
      this.pos1Y = ey;
      this.pos1Z = ez;
      this.size = interiorSize;
      return !this.isTypeForced() && !this.determineType() ? Building.validationResult.INVALID_TYPE : Building.validationResult.SUCCESS;
   }

   private boolean isBuildingBlock(class_2960 blockId) {
      for (BuildingType bt : BuildingTypes.getInstance()) {
         if (bt.matchesBlock(blockId)) {
            return true;
         }
      }

      return false;
   }

   public boolean determineType() {
      int bestPriority = -1;
      boolean assignedType = false;

      for (BuildingType bt : BuildingTypes.getInstance()) {
         if (bt.priority() > bestPriority) {
            Map<class_2960, List<class_2338>> available = bt.getGroups(this.blocks);
            boolean valid = bt.getGroups()
               .entrySet()
               .stream()
               .noneMatch(e -> !available.containsKey(e.getKey()) || available.get(e.getKey()).size() < e.getValue());
            if (valid) {
               bestPriority = bt.priority();
               this.type = bt.name();
               assignedType = true;
            }
         }
      }

      return assignedType;
   }

   public String getType() {
      return this.type;
   }

   public boolean isTypeForced() {
      return this.isTypeForced;
   }

   public BuildingType getBuildingType() {
      return BuildingTypes.getInstance().getBuildingType(this.type);
   }

   public void setType(String type) {
      this.type = type;
   }

   public void setTypeForced(boolean forced) {
      this.isTypeForced = forced;
   }

   public Map<class_2960, List<class_2338>> getBlocks() {
      return this.blocks;
   }

   public void addBlock(class_2248 block, class_2338 p) {
      class_2960 key = class_7923.field_41175.method_10221(block);
      this.blocks.computeIfAbsent(key, k -> new ArrayList<>());
      this.blocks.get(key).add(p);
   }

   public void removeBlock(class_2248 block, class_2338 p) {
      class_2960 key = class_7923.field_41175.method_10221(block);
      if (this.blocks.containsKey(key)) {
         this.blocks.get(key).remove(p);
      }
   }

   public int getBlockCount() {
      return this.blocks.values().stream().mapToInt(List::size).sum();
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public boolean overlaps(Building b) {
      return this.pos1X > b.pos0X && this.pos0X < b.pos1X && this.pos1Y > b.pos0Y && this.pos0Y < b.pos1Y && this.pos1Z > b.pos0Z && this.pos0Z < b.pos1Z;
   }

   public boolean containsPos(class_2382 pos) {
      return this.getBuildingType().grouped()
         ? pos.method_19771(this.getCenter(), this.getBuildingType().getMargin())
         : pos.method_10263() >= this.pos0X
            && pos.method_10263() <= this.pos1X
            && pos.method_10264() >= this.pos0Y
            && pos.method_10264() <= this.pos1Y
            && pos.method_10260() >= this.pos0Z
            && pos.method_10260() <= this.pos1Z;
   }

   public boolean isIdentical(Building b) {
      return this.pos0X == b.pos0X && this.pos1X == b.pos1X && this.pos0Y == b.pos0Y && this.pos1Y == b.pos1Y && this.pos0Z == b.pos0Z && this.pos1Z == b.pos1Z;
   }

   public int getSize() {
      return this.size;
   }

   public long getLastScan() {
      return this.lastScan;
   }

   public void setLastScan(long lastScan) {
      this.lastScan = lastScan;
   }

   public boolean isStrictScan() {
      return this.strictScan;
   }

   public boolean isComplete() {
      BuildingType bt = this.getBuildingType();
      int minBlocks = bt.getMinBlocks();
      return minBlocks == 0 || this.getBlockCount() >= minBlocks;
   }

   public enum validationResult {
      OVERLAP,
      BLOCK_LIMIT,
      SIZE_LIMIT,
      NO_DOOR,
      TOO_SMALL,
      IDENTICAL,
      SUCCESS,
      INVALID_TYPE;
   }
}
