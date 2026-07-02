package forge.net.mca.server.world.data;

import forge.net.mca.Config;
import forge.net.mca.resources.BuildingTypes;
import forge.net.mca.resources.data.BuildingType;
import forge.net.mca.util.NbtHelper;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class Building implements Serializable {
   private static final long serialVersionUID = -1106627083469687307L;
   public static final long SCAN_COOLDOWN = 4800L;
   private static final Direction[] directions = new Direction[]{
      Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
   };
   private final Map<ResourceLocation, List<BlockPos>> blocks = new HashMap<>();
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

   public Building(BlockPos pos) {
      this(pos, false);
   }

   public Building(BlockPos pos, boolean strictScan) {
      this();
      this.pos0X = pos.m_123341_();
      this.pos0Y = pos.m_123342_();
      this.pos0Z = pos.m_123343_();
      this.pos1X = this.pos0X;
      this.pos1Y = this.pos0Y;
      this.pos1Z = this.pos0Z;
      this.posX = this.pos0X;
      this.posY = this.pos0Y;
      this.posZ = this.pos0Z;
      this.strictScan = strictScan;
   }

   public Building(CompoundTag v) {
      this.id = v.m_128451_("id");
      this.size = v.m_128451_("size");
      this.pos0X = v.m_128451_("pos0X");
      this.pos0Y = v.m_128451_("pos0Y");
      this.pos0Z = v.m_128451_("pos0Z");
      this.pos1X = v.m_128451_("pos1X");
      this.pos1Y = v.m_128451_("pos1Y");
      this.pos1Z = v.m_128451_("pos1Z");
      if (v.m_128441_("posX")) {
         this.posX = v.m_128451_("posX");
         this.posY = v.m_128451_("posY");
         this.posZ = v.m_128451_("posZ");
      } else {
         BlockPos center = this.getCenter();
         this.posX = center.m_123341_();
         this.posY = center.m_123342_();
         this.posZ = center.m_123343_();
      }

      this.isTypeForced = v.m_128471_("isTypeForced");
      this.type = v.m_128461_("type");
      this.strictScan = v.m_128471_("strictScan");
      this.blocks.putAll(NbtHelper.toMap(v.m_128469_("blocks2"), ResourceLocation::new, l -> NbtHelper.toList(l, e -> {
         CompoundTag c = (CompoundTag)e;
         return new BlockPos(c.m_128451_("x"), c.m_128451_("y"), c.m_128451_("z"));
      })));
   }

   public CompoundTag save() {
      CompoundTag v = new CompoundTag();
      v.m_128405_("id", this.id);
      v.m_128405_("size", this.size);
      v.m_128405_("pos0X", this.pos0X);
      v.m_128405_("pos0Y", this.pos0Y);
      v.m_128405_("pos0Z", this.pos0Z);
      v.m_128405_("pos1X", this.pos1X);
      v.m_128405_("pos1Y", this.pos1Y);
      v.m_128405_("pos1Z", this.pos1Z);
      v.m_128405_("posX", this.posX);
      v.m_128405_("posY", this.posY);
      v.m_128405_("posZ", this.posZ);
      v.m_128379_("isTypeForced", this.isTypeForced);
      v.m_128359_("type", this.type);
      v.m_128379_("strictScan", this.strictScan);
      CompoundTag b = new CompoundTag();
      NbtHelper.fromMap(b, this.blocks, ResourceLocation::toString, e -> NbtHelper.fromList(e, p -> {
         CompoundTag entry = new CompoundTag();
         entry.m_128405_("x", p.m_123341_());
         entry.m_128405_("y", p.m_123342_());
         entry.m_128405_("z", p.m_123343_());
         return entry;
      }));
      v.m_128365_("blocks2", b);
      return v;
   }

   public BlockPos getPos0() {
      int margin = this.getBuildingType().getMargin();
      return new BlockPos(this.pos0X, this.pos0Y, this.pos0Z).m_121996_(new Vec3i(margin, margin, margin));
   }

   public BlockPos getPos1() {
      int margin = this.getBuildingType().getMargin();
      return new BlockPos(this.pos1X, this.pos1Y, this.pos1Z).m_121955_(new Vec3i(margin, margin, margin));
   }

   public BlockPos getCenter() {
      return new BlockPos((this.pos0X + this.pos1X) / 2, (this.pos0Y + this.pos1Y) / 2, (this.pos0Z + this.pos1Z) / 2);
   }

   public BlockPos getSourceBlock() {
      return new BlockPos(this.posX, this.posY, this.posZ);
   }

   public void validateBlocks(Level world) {
      this.setLastScan(world.m_46467_());

      for (Entry<ResourceLocation, List<BlockPos>> positions : this.blocks.entrySet()) {
         List<BlockPos> mask = positions.getValue()
            .stream()
            .filter(p -> !BuiltInRegistries.f_256975_.m_7981_(world.m_8055_(p).m_60734_()).equals(positions.getKey()))
            .toList();
         positions.getValue().removeAll(mask);
      }
   }

   public Stream<BlockPos> getBlockPosStream() {
      return this.blocks.values().stream().flatMap(Collection::stream);
   }

   public void addPOI(Level world, BlockPos pos) {
      Block block = world.m_8055_(pos).m_60734_();
      this.removeBlock(block, pos);
      this.addBlock(block, pos);
      this.validateBlocks(world);
      int n = (int)this.getBlockPosStream().count();
      if (n > 0) {
         BlockPos center = this.getBlockPosStream().reduce(BlockPos.f_121853_, BlockPos::m_121955_);
         this.pos0X = center.m_123341_() / n;
         this.pos0Y = center.m_123342_() / n;
         this.pos0Z = center.m_123343_() / n;
         this.pos1X = this.pos0X;
         this.pos1Y = this.pos0Y;
         this.pos1Z = this.pos0Z;
      }
   }

   public Building.validationResult validateBuilding(Level world, Set<BlockPos> blocked) {
      if (this.getBuildingType().grouped()) {
         this.validateBlocks(world);
         return this.getBlockPosStream().findAny().isEmpty() ? Building.validationResult.TOO_SMALL : Building.validationResult.SUCCESS;
      }

      this.blocks.clear();
      this.size = 0;
      this.setLastScan(world.m_46467_());
      Set<BlockPos> done = new HashSet<>();
      LinkedList<BlockPos> queue = new LinkedList<>();
      BlockPos center = this.getSourceBlock();
      queue.add(center);
      done.add(center);
      int minSize = Config.getInstance().minBuildingSize;
      int maxSize = Config.getInstance().maxBuildingSize;
      int maxRadius = Config.getInstance().maxBuildingRadius;
      int scanSize = 0;
      int interiorSize = 0;
      boolean hasDoor = false;
      Map<BlockPos, Boolean> roofCache = new HashMap<>();

      while (!queue.isEmpty() && scanSize < maxSize) {
         BlockPos p = queue.removeLast();
         if (blocked.contains(p) && scanSize > 0) {
            return Building.validationResult.OVERLAP;
         }

         if (p.m_123333_(center) >= maxRadius) {
            return Building.validationResult.SIZE_LIMIT;
         }

         for (Direction d : directions) {
            BlockPos n = p.m_121945_(d);
            if (!done.contains(n)) {
               BlockState state = world.m_8055_(n);
               done.add(n);
               if (!state.m_60795_()) {
                  if (state.m_60734_() instanceof DoorBlock) {
                     if (!this.strictScan) {
                        queue.add(n);
                     }

                     hasDoor = true;
                  }
               } else {
                  if (!roofCache.containsKey(n)) {
                     BlockPos n2 = n;
                     int maxScanHeight = 16;

                     for (int i = 0; i < maxScanHeight; i++) {
                        roofCache.put(n2, false);
                        n2 = n2.m_7494_();
                        BlockState block = world.m_8055_(n2);
                        if (!block.m_60795_() || roofCache.containsKey(n2)) {
                           if ((!roofCache.containsKey(n2) || roofCache.get(n2)) && !block.m_204336_(BlockTags.f_13035_)) {
                              for (int i2 = i; i2 >= 0; i2--) {
                                 n2 = n2.m_7495_();
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

      int sx = center.m_123341_();
      int sy = center.m_123342_();
      int sz = center.m_123343_();
      int ex = sx;
      int ey = sy;
      int ez = sz;

      for (BlockPos p : done) {
         sx = Math.min(sx, p.m_123341_());
         sy = Math.min(sy, p.m_123342_());
         sz = Math.min(sz, p.m_123343_());
         ex = Math.max(ex, p.m_123341_());
         ey = Math.max(ey, p.m_123342_());
         ez = Math.max(ez, p.m_123343_());
         BlockState blockState = world.m_8055_(p);
         Block block = blockState.m_60734_();
         if (this.isBuildingBlock(BuiltInRegistries.f_256975_.m_7981_(block))) {
            if (block instanceof BedBlock) {
               if (blockState.m_61143_(BedBlock.f_49440_) == BedPart.HEAD) {
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

   private boolean isBuildingBlock(ResourceLocation blockId) {
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
            Map<ResourceLocation, List<BlockPos>> available = bt.getGroups(this.blocks);
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

   public Map<ResourceLocation, List<BlockPos>> getBlocks() {
      return this.blocks;
   }

   public void addBlock(Block block, BlockPos p) {
      ResourceLocation key = BuiltInRegistries.f_256975_.m_7981_(block);
      this.blocks.computeIfAbsent(key, k -> new ArrayList<>());
      this.blocks.get(key).add(p);
   }

   public void removeBlock(Block block, BlockPos p) {
      ResourceLocation key = BuiltInRegistries.f_256975_.m_7981_(block);
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

   public boolean containsPos(Vec3i pos) {
      return this.getBuildingType().grouped()
         ? pos.m_123314_(this.getCenter(), this.getBuildingType().getMargin())
         : pos.m_123341_() >= this.pos0X
            && pos.m_123341_() <= this.pos1X
            && pos.m_123342_() >= this.pos0Y
            && pos.m_123342_() <= this.pos1Y
            && pos.m_123343_() >= this.pos0Z
            && pos.m_123343_() <= this.pos1Z;
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
