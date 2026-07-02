package forge.net.mca.util;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet.Direct;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface WorldUtils {
   static List<Entity> getCloseEntities(Level world, Entity e, double range) {
      Vec3 pos = e.m_20182_();
      return world.m_45933_(e, new AABB(pos, pos).m_82400_(range));
   }

   static <T extends Entity> List<T> getCloseEntities(Level world, Entity e, double range, Class<T> c) {
      return getCloseEntities(world, e.m_20182_(), range, c);
   }

   static <T extends Entity> List<T> getCloseEntities(Level world, Vec3 pos, double range, Class<T> c) {
      return world.m_45976_(c, new AABB(pos, pos).m_82400_(range));
   }

   static <T extends SavedData> T loadData(ServerLevel world, Function<CompoundTag, T> loader, Function<ServerLevel, T> factory, String dataId) {
      return (T)world.m_8895_().m_164861_(loader, () -> factory.apply(world), dataId);
   }

   static void spawnEntity(Level world, Mob entity, MobSpawnType reason) {
      entity.m_6518_((ServerLevelAccessor)world, world.m_6436_(entity.m_20183_()), reason, null, null);
      world.m_7967_(entity);
   }

   static Optional<BlockPos> getClosestStructurePosition(ServerLevel world, BlockPos center, ResourceLocation structure, int radius) {
      Registry<Structure> registry = world.m_9598_().m_175515_(Registries.f_256944_);
      Structure feature = (Structure)registry.m_7745_(structure);
      Optional<Reference<Structure>> entry = registry.m_203300_(registry.m_7447_(feature));
      if (entry.isPresent()) {
         Direct<Structure> of = HolderSet.m_205809_(new Holder[]{(Holder)entry.get()});
         Pair<BlockPos, Holder<Structure>> pair = world.m_7726_().m_8481_().m_223037_(world, of, center, radius, false);
         return pair == null ? Optional.empty() : Optional.ofNullable((BlockPos)pair.getFirst());
      } else {
         return Optional.empty();
      }
   }

   static Optional<BlockPos> getClosestStructurePosition(ServerLevel world, BlockPos center, TagKey<Structure> tag, int radius) {
      Registry<Structure> registry = world.m_9598_().m_175515_(Registries.f_256944_);
      Optional<Named<Structure>> entryList = registry.m_203431_(tag);
      if (entryList.isPresent()) {
         ChunkGenerator chunkGenerator = world.m_7726_().m_8481_();
         Pair<BlockPos, Holder<Structure>> pair = chunkGenerator.m_223037_(world, (HolderSet)entryList.get(), center, radius, false);
         return pair == null ? Optional.empty() : Optional.ofNullable((BlockPos)pair.getFirst());
      } else {
         return Optional.empty();
      }
   }

   static boolean isChunkLoaded(Level world, Vec3i pos) {
      return world instanceof ServerLevel serverWorld ? isChunkLoaded(serverWorld, pos) : false;
   }

   static boolean isChunkLoaded(ServerLevel world, Vec3i pos) {
      return isChunkLoaded(world, new BlockPos(pos));
   }

   static boolean isChunkLoaded(ServerLevel world, BlockPos pos) {
      ChunkPos chunkPos = new ChunkPos(pos);
      LevelChunk worldChunk = world.m_7726_().m_7131_(chunkPos.f_45578_, chunkPos.f_45579_);
      return worldChunk == null ? false : worldChunk.m_287138_() == FullChunkStatus.ENTITY_TICKING && world.m_143319_(chunkPos.m_45588_());
   }

   static boolean isAreaLoaded(ServerLevel world, ChunkPos pos, int radius) {
      ServerChunkCache chunkManager = world.m_7726_();

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            if (!chunkManager.m_5563_(pos.f_45578_ + x, pos.f_45579_ + z)) {
               return false;
            }
         }
      }

      return true;
   }
}
