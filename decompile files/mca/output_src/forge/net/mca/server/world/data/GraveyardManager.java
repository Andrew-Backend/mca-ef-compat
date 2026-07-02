package forge.net.mca.server.world.data;

import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

public class GraveyardManager extends SavedData {
   private final Map<GraveyardManager.TombstoneState, Long2ObjectMap<GraveyardManager.ChunkBase>> tombstones = new EnumMap<>(
      GraveyardManager.TombstoneState.class
   );

   public static GraveyardManager get(ServerLevel world) {
      return WorldUtils.loadData(world, GraveyardManager::new, GraveyardManager::new, "mca_graveyard");
   }

   public GraveyardManager(ServerLevel world) {
   }

   public GraveyardManager(CompoundTag nbt) {
      this.tombstones.putAll(NbtHelper.toMap(nbt, GraveyardManager.TombstoneState::valueOf, v -> {
         CompoundTag vv = (CompoundTag)v;
         Long2ObjectMap<GraveyardManager.ChunkBase> map = new Long2ObjectOpenHashMap();
         vv.m_128431_().forEach(key -> map.put(Long.parseLong(key), new GraveyardManager.Chunk((ListTag)vv.m_128423_(key))));
         return map;
      }));
   }

   public CompoundTag m_7176_(CompoundTag nbt) {
      CompoundTag tag = new CompoundTag();
      synchronized (this.tombstones) {
         this.tombstones.forEach((state, chunks) -> {
            CompoundTag chunkList = new CompoundTag();
            chunks.long2ObjectEntrySet().forEach(entry -> {
               if (!((GraveyardManager.ChunkBase)entry.getValue()).isEmpty()) {
                  chunkList.m_128365_(String.valueOf(entry.getLongKey()), ((GraveyardManager.ChunkBase)entry.getValue()).toNbt());
               }
            });
            if (!chunkList.m_128456_()) {
               tag.m_128365_(state.name(), chunkList);
            }
         });
         return tag;
      }
   }

   public void setTombstoneState(BlockPos pos, GraveyardManager.TombstoneState state) {
      synchronized (this.tombstones) {
         long l = getChunkPos(pos);
         this.getChunk(state.opposite(), l, GraveyardManager.ChunkBase::empty).removePos(pos);
         this.getChunk(state, l, GraveyardManager.Chunk::new).addPos(pos);
         this.m_77762_();
      }
   }

   public void removeTombstoneState(BlockPos pos) {
      synchronized (this.tombstones) {
         long l = getChunkPos(pos);
         this.getChunk(GraveyardManager.TombstoneState.EMPTY, l, GraveyardManager.ChunkBase::empty).removePos(pos);
         this.getChunk(GraveyardManager.TombstoneState.FILLED, l, GraveyardManager.ChunkBase::empty).removePos(pos);
         this.m_77762_();
      }
   }

   public List<BlockPos> findAll(AABB box, boolean includeEmpty, boolean includeFilled) {
      List<BlockPos> positions = new ArrayList<>();
      if (includeEmpty || includeFilled) {
         int minX = Mth.m_14107_((box.f_82288_ - 2.0) / 16.0);
         int maxX = Mth.m_14165_((box.f_82291_ + 2.0) / 16.0);
         int minZ = Mth.m_14107_((box.f_82290_ - 2.0) / 16.0);
         int maxZ = Mth.m_14165_((box.f_82293_ + 2.0) / 16.0);
         MutableBlockPos mutable = new MutableBlockPos();
         synchronized (this.tombstones) {
            for (int x = minX; x < maxX; x++) {
               for (int z = minZ; z < maxZ; z++) {
                  long l = ChunkPos.m_45589_(x, z);
                  if (includeEmpty) {
                     this.getChunk(GraveyardManager.TombstoneState.EMPTY, l, GraveyardManager.ChunkBase::empty).appendAll(box, mutable, positions);
                  }

                  if (includeFilled) {
                     this.getChunk(GraveyardManager.TombstoneState.FILLED, l, GraveyardManager.ChunkBase::empty).appendAll(box, mutable, positions);
                  }
               }
            }
         }
      }

      return positions;
   }

   public Optional<BlockPos> findNearest(BlockPos pos, GraveyardManager.TombstoneState state, int maxChunkRange) {
      synchronized (this.tombstones) {
         MutableBlockPos mutable = new MutableBlockPos();
         return this.getChunk(state, getChunkPos(pos), GraveyardManager.ChunkBase::empty)
            .findNearest(pos, mutable)
            .or(
               () -> {
                  BlockPos center = new BlockPos(SectionPos.m_123171_(pos.m_123341_()), 0, SectionPos.m_123171_(pos.m_123343_()));
                  return BlockPos.m_121985_(center, maxChunkRange, 0, maxChunkRange)
                     .map(p -> ChunkPos.m_45589_(p.m_123341_(), p.m_123343_()))
                     .map(l -> this.getChunk(state, l, GraveyardManager.ChunkBase::empty).findNearest(pos, mutable))
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .min(Comparator.comparing(a -> a.m_123331_(pos)));
               }
            );
      }
   }

   private static long getChunkPos(BlockPos pos) {
      return ChunkPos.m_45589_(SectionPos.m_123171_(pos.m_123341_()), SectionPos.m_123171_(pos.m_123343_()));
   }

   private GraveyardManager.ChunkBase getChunk(GraveyardManager.TombstoneState state, long pos, LongFunction<GraveyardManager.ChunkBase> fallback) {
      Long2ObjectMap<GraveyardManager.ChunkBase> chunks = this.tombstones.computeIfAbsent(state, n -> new Long2ObjectOpenHashMap());
      GraveyardManager.ChunkBase chunk = (GraveyardManager.ChunkBase)chunks.get(pos);
      if (chunk == null) {
         chunk = fallback.apply(pos);
         if (chunk != GraveyardManager.ChunkBase.EMPTY) {
            chunks.put(pos, chunk);
         }
      }

      return chunk;
   }

   public void reportToVillageManager(Entity entity) {
      VillageManager manager = VillageManager.get((ServerLevel)entity.m_9236_());
      get((ServerLevel)entity.m_9236_())
         .findAll(entity.m_20191_().m_82400_(24.0), true, true)
         .stream()
         .filter(p -> !manager.cache.contains(p))
         .forEach(manager::processBuilding);
   }

   private static class Chunk extends GraveyardManager.ChunkBase {
      private final LongSet tombstones = new LongArraySet();

      Chunk(long l) {
      }

      Chunk(ListTag list) {
         list.forEach(l -> this.tombstones.add(((NumericTag)l).m_7046_()));
      }

      @Override
      public boolean isEmpty() {
         return this.tombstones.isEmpty();
      }

      @Override
      public ListTag toNbt() {
         ListTag list = new ListTag();
         this.tombstones.forEach(l -> list.add(LongTag.m_128882_(l)));
         return list;
      }

      @Override
      public void removePos(BlockPos pos) {
         this.tombstones.remove(pos.m_121878_());
      }

      @Override
      public void addPos(BlockPos pos) {
         this.tombstones.add(pos.m_121878_());
      }

      @Override
      public Optional<BlockPos> findNearest(BlockPos pos, MutableBlockPos mutable) {
         double distance = Double.MAX_VALUE;
         long nearest = -1L;
         boolean found = false;
         LongIterator var8 = this.tombstones.iterator();

         while (var8.hasNext()) {
            long l = (Long)var8.next();
            mutable.m_122188_(l);
            double d = pos.m_123331_(mutable);
            if (d < distance) {
               distance = d;
               nearest = l;
               found = true;
            }
         }

         return found ? Optional.of(BlockPos.m_122022_(nearest)) : Optional.empty();
      }

      @Override
      public void appendAll(AABB box, MutableBlockPos mutable, List<BlockPos> positions) {
         LongIterator var4 = this.tombstones.iterator();

         while (var4.hasNext()) {
            long l = (Long)var4.next();
            mutable.m_122188_(l);
            if (box.m_82393_(mutable.m_123341_(), mutable.m_123342_(), mutable.m_123343_())) {
               positions.add(mutable.m_7949_());
            }
         }
      }
   }

   private static class ChunkBase {
      static final GraveyardManager.ChunkBase EMPTY = new GraveyardManager.ChunkBase();

      static GraveyardManager.ChunkBase empty(long l) {
         return EMPTY;
      }

      public boolean isEmpty() {
         return true;
      }

      public ListTag toNbt() {
         return new ListTag();
      }

      public void removePos(BlockPos pos) {
      }

      public void addPos(BlockPos pos) {
      }

      public Optional<BlockPos> findNearest(BlockPos pos, MutableBlockPos mutable) {
         return Optional.empty();
      }

      public void appendAll(AABB box, MutableBlockPos mutable, List<BlockPos> positions) {
      }
   }

   public enum TombstoneState {
      EMPTY,
      FILLED;

      GraveyardManager.TombstoneState opposite() {
         return this == EMPTY ? FILLED : EMPTY;
      }
   }
}
