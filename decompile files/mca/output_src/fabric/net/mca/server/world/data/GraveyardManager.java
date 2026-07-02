package fabric.net.mca.server.world.data;

import fabric.net.mca.util.NbtHelper;
import fabric.net.mca.util.WorldUtils;
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
import net.minecraft.class_1297;
import net.minecraft.class_18;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2503;
import net.minecraft.class_2514;
import net.minecraft.class_3218;
import net.minecraft.class_3532;
import net.minecraft.class_4076;
import net.minecraft.class_2338.class_2339;

public class GraveyardManager extends class_18 {
   private final Map<GraveyardManager.TombstoneState, Long2ObjectMap<GraveyardManager.ChunkBase>> tombstones = new EnumMap<>(
      GraveyardManager.TombstoneState.class
   );

   public static GraveyardManager get(class_3218 world) {
      return WorldUtils.loadData(world, GraveyardManager::new, GraveyardManager::new, "mca_graveyard");
   }

   public GraveyardManager(class_3218 world) {
   }

   public GraveyardManager(class_2487 nbt) {
      this.tombstones.putAll(NbtHelper.toMap(nbt, GraveyardManager.TombstoneState::valueOf, v -> {
         class_2487 vv = (class_2487)v;
         Long2ObjectMap<GraveyardManager.ChunkBase> map = new Long2ObjectOpenHashMap();
         vv.method_10541().forEach(key -> map.put(Long.parseLong(key), new GraveyardManager.Chunk((class_2499)vv.method_10580(key))));
         return map;
      }));
   }

   public class_2487 method_75(class_2487 nbt) {
      class_2487 tag = new class_2487();
      synchronized (this.tombstones) {
         this.tombstones.forEach((state, chunks) -> {
            class_2487 chunkList = new class_2487();
            chunks.long2ObjectEntrySet().forEach(entry -> {
               if (!((GraveyardManager.ChunkBase)entry.getValue()).isEmpty()) {
                  chunkList.method_10566(String.valueOf(entry.getLongKey()), ((GraveyardManager.ChunkBase)entry.getValue()).toNbt());
               }
            });
            if (!chunkList.method_33133()) {
               tag.method_10566(state.name(), chunkList);
            }
         });
         return tag;
      }
   }

   public void setTombstoneState(class_2338 pos, GraveyardManager.TombstoneState state) {
      synchronized (this.tombstones) {
         long l = getChunkPos(pos);
         this.getChunk(state.opposite(), l, GraveyardManager.ChunkBase::empty).removePos(pos);
         this.getChunk(state, l, GraveyardManager.Chunk::new).addPos(pos);
         this.method_80();
      }
   }

   public void removeTombstoneState(class_2338 pos) {
      synchronized (this.tombstones) {
         long l = getChunkPos(pos);
         this.getChunk(GraveyardManager.TombstoneState.EMPTY, l, GraveyardManager.ChunkBase::empty).removePos(pos);
         this.getChunk(GraveyardManager.TombstoneState.FILLED, l, GraveyardManager.ChunkBase::empty).removePos(pos);
         this.method_80();
      }
   }

   public List<class_2338> findAll(class_238 box, boolean includeEmpty, boolean includeFilled) {
      List<class_2338> positions = new ArrayList<>();
      if (includeEmpty || includeFilled) {
         int minX = class_3532.method_15357((box.field_1323 - 2.0) / 16.0);
         int maxX = class_3532.method_15384((box.field_1320 + 2.0) / 16.0);
         int minZ = class_3532.method_15357((box.field_1321 - 2.0) / 16.0);
         int maxZ = class_3532.method_15384((box.field_1324 + 2.0) / 16.0);
         class_2339 mutable = new class_2339();
         synchronized (this.tombstones) {
            for (int x = minX; x < maxX; x++) {
               for (int z = minZ; z < maxZ; z++) {
                  long l = class_1923.method_8331(x, z);
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

   public Optional<class_2338> findNearest(class_2338 pos, GraveyardManager.TombstoneState state, int maxChunkRange) {
      synchronized (this.tombstones) {
         class_2339 mutable = new class_2339();
         return this.getChunk(state, getChunkPos(pos), GraveyardManager.ChunkBase::empty)
            .findNearest(pos, mutable)
            .or(
               () -> {
                  class_2338 center = new class_2338(class_4076.method_18675(pos.method_10263()), 0, class_4076.method_18675(pos.method_10260()));
                  return class_2338.method_25998(center, maxChunkRange, 0, maxChunkRange)
                     .map(p -> class_1923.method_8331(p.method_10263(), p.method_10260()))
                     .map(l -> this.getChunk(state, l, GraveyardManager.ChunkBase::empty).findNearest(pos, mutable))
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .min(Comparator.comparing(a -> a.method_10262(pos)));
               }
            );
      }
   }

   private static long getChunkPos(class_2338 pos) {
      return class_1923.method_8331(class_4076.method_18675(pos.method_10263()), class_4076.method_18675(pos.method_10260()));
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

   public void reportToVillageManager(class_1297 entity) {
      VillageManager manager = VillageManager.get((class_3218)entity.method_37908());
      get((class_3218)entity.method_37908())
         .findAll(entity.method_5829().method_1014(24.0), true, true)
         .stream()
         .filter(p -> !manager.cache.contains(p))
         .forEach(manager::processBuilding);
   }

   private static class Chunk extends GraveyardManager.ChunkBase {
      private final LongSet tombstones = new LongArraySet();

      Chunk(long l) {
      }

      Chunk(class_2499 list) {
         list.forEach(l -> this.tombstones.add(((class_2514)l).method_10699()));
      }

      @Override
      public boolean isEmpty() {
         return this.tombstones.isEmpty();
      }

      @Override
      public class_2499 toNbt() {
         class_2499 list = new class_2499();
         this.tombstones.forEach(l -> list.add(class_2503.method_23251(l)));
         return list;
      }

      @Override
      public void removePos(class_2338 pos) {
         this.tombstones.remove(pos.method_10063());
      }

      @Override
      public void addPos(class_2338 pos) {
         this.tombstones.add(pos.method_10063());
      }

      @Override
      public Optional<class_2338> findNearest(class_2338 pos, class_2339 mutable) {
         double distance = Double.MAX_VALUE;
         long nearest = -1L;
         boolean found = false;
         LongIterator var8 = this.tombstones.iterator();

         while (var8.hasNext()) {
            long l = (Long)var8.next();
            mutable.method_16363(l);
            double d = pos.method_10262(mutable);
            if (d < distance) {
               distance = d;
               nearest = l;
               found = true;
            }
         }

         return found ? Optional.of(class_2338.method_10092(nearest)) : Optional.empty();
      }

      @Override
      public void appendAll(class_238 box, class_2339 mutable, List<class_2338> positions) {
         LongIterator var4 = this.tombstones.iterator();

         while (var4.hasNext()) {
            long l = (Long)var4.next();
            mutable.method_16363(l);
            if (box.method_1008(mutable.method_10263(), mutable.method_10264(), mutable.method_10260())) {
               positions.add(mutable.method_10062());
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

      public class_2499 toNbt() {
         return new class_2499();
      }

      public void removePos(class_2338 pos) {
      }

      public void addPos(class_2338 pos) {
      }

      public Optional<class_2338> findNearest(class_2338 pos, class_2339 mutable) {
         return Optional.empty();
      }

      public void appendAll(class_238 box, class_2339 mutable, List<class_2338> positions) {
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
