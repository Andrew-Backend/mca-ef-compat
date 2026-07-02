package forge.net.mca.server;

import forge.net.mca.Config;
import forge.net.mca.MCA;
import forge.net.mca.SoundsMCA;
import forge.net.mca.block.BlocksMCA;
import forge.net.mca.entity.EntitiesMCA;
import forge.net.mca.entity.GrimReaperEntity;
import forge.net.mca.server.world.data.VillageManager;
import forge.net.mca.util.NbtHelper;
import forge.net.mca.util.WorldUtils;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ReaperSpawner {
   private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
   private final Object lock = new Object();
   private final Map<Long, ReaperSpawner.ActiveSummon> activeSummons = new ConcurrentHashMap<>();
   private final VillageManager manager;

   public ReaperSpawner(VillageManager manager) {
      this.manager = manager;
   }

   public ReaperSpawner(VillageManager manager, CompoundTag nbt) {
      this.manager = manager;
      NbtHelper.toList(nbt.m_128437_("summons", 10), n -> new ReaperSpawner.ActiveSummon((CompoundTag)n))
         .forEach(summon -> this.activeSummons.put(summon.position.spawnPosition.m_121878_(), summon));
   }

   private void warn(Level world, BlockPos pos, String phrase) {
      world.m_6907_()
         .stream()
         .min(Comparator.comparingInt(a -> a.m_20183_().m_123333_(pos)))
         .ifPresent(p -> p.m_5661_(Component.m_237115_(phrase).m_130940_(ChatFormatting.RED), true));
   }

   public void trySpawnReaper(ServerLevel world, BlockPos pos) {
      if (Config.getInstance().allowGrimReaper) {
         ChunkPos chunkPos = new ChunkPos(pos);
         if (WorldUtils.isAreaLoaded(world, chunkPos, 1)) {
            if (world.m_8055_(pos).m_60734_() == Blocks.f_50268_) {
               MCA.LOGGER.info("Attempting to spawn reaper at {} in {}", pos, world.m_46472_().m_135782_());
               if (!this.isNightTime(world)) {
                  this.warn(world, pos, "reaper.day");
               } else {
                  Set<BlockPos> totems = this.getTotemsFires(world, pos);
                  MCA.LOGGER.info("It is night time, found {} totems", totems.size());
                  if (totems.size() < 3) {
                     this.warn(world, pos, "reaper.totems");
                  } else {
                     this.start(new ReaperSpawner.SummonPosition(pos.m_7494_(), totems));
                     EntityType.f_20465_.m_262496_(world, pos, MobSpawnType.TRIGGERED);
                     world.m_7731_(pos, Blocks.f_50136_.m_49966_(), 3);
                     world.m_7731_(pos.m_7494_(), ((Block)BlocksMCA.INFERNAL_FLAME.get()).m_49966_(), 3);
                     totems.forEach(totem -> world.m_7731_(totem, ((Block)BlocksMCA.INFERNAL_FLAME.get()).m_49966_(), 18));
                  }
               }
            }
         }
      }
   }

   private void start(ReaperSpawner.SummonPosition pos) {
      this.activeSummons.computeIfAbsent(pos.spawnPosition.m_121878_(), ReaperSpawner.ActiveSummon::new).start(pos);
      this.manager.m_77762_();
   }

   public void tick(ServerLevel world) {
      boolean empty = this.activeSummons.isEmpty();
      this.activeSummons.values().removeIf(summon -> {
         try {
            return summon.tick(world);
         } catch (Exception e) {
            MCA.LOGGER.error("Exception ticking summon", e);
            return true;
         }
      });
      if (!empty) {
         this.manager.m_77762_();
      }
   }

   private boolean isNightTime(Level world) {
      long time = world.m_46468_() % 24000L;
      MCA.LOGGER.info("Current time is {}", time);
      return time >= 13000L && time <= 23000L;
   }

   private Set<BlockPos> getTotemsFires(Level world, BlockPos pos) {
      int groundY = pos.m_123342_() - 1;
      int leftSkyHeight = world.m_151558_() - groundY;
      int minPillarHeight = Math.min(Config.getInstance().minPillarHeight, leftSkyHeight);
      MutableBlockPos target = new MutableBlockPos();
      return Stream.of(HORIZONTALS).map(d -> target.m_122190_(pos).m_142448_(groundY).m_122175_(d, 3)).filter(pillarPos -> {
         for (int height = 1; height <= leftSkyHeight; height++) {
            pillarPos.m_142448_(groundY + height);
            if (!world.m_8055_(pillarPos).m_60713_(Blocks.f_50080_)) {
               if (world.m_8055_(pillarPos).m_204336_(BlockTags.f_13076_)) {
                  return height - 1 >= minPillarHeight;
               }

               return false;
            }
         }

         return false;
      }).<BlockPos>map(BlockPos::m_7949_).collect(Collectors.toSet());
   }

   public CompoundTag writeNbt() {
      synchronized (this.lock) {
         CompoundTag nbt = new CompoundTag();
         nbt.m_128365_("summons", NbtHelper.fromList(this.activeSummons.values(), ReaperSpawner.ActiveSummon::write));
         return nbt;
      }
   }

   static class ActiveSummon {
      private int ticks;
      private ReaperSpawner.SummonPosition position;

      ActiveSummon(long l) {
      }

      ActiveSummon(CompoundTag nbt) {
         this.ticks = nbt.m_128451_("ticks");
         this.position = new ReaperSpawner.SummonPosition(nbt.m_128469_("position"));
      }

      public void start(ReaperSpawner.SummonPosition pos) {
         if (this.ticks <= 0) {
            this.position = pos;
            this.ticks = 100;
         }
      }

      public boolean tick(ServerLevel world) {
         if (this.ticks <= 0 || this.position == null) {
            return true;
         }

         if (this.position.isCancelled(world)) {
            this.position.totems.forEach(totem -> {
               if (this.position.check(totem, world)) {
                  world.m_46597_(totem, Blocks.f_50083_.m_49966_());
               }
            });
            this.position = null;
            this.ticks = 0;
            return true;
         }

         if (--this.ticks % 20 == 0) {
            EntityType.f_20465_.m_262496_(world, this.position.spawnPosition, MobSpawnType.TRIGGERED);
         }

         if (this.ticks == 0) {
            GrimReaperEntity reaper = (GrimReaperEntity)((EntityType)EntitiesMCA.GRIM_REAPER.get())
               .m_262496_(world, this.position.spawnPosition, MobSpawnType.TRIGGERED);
            if (reaper != null) {
               reaper.m_5496_((SoundEvent)SoundsMCA.REAPER_SUMMON.get(), 1.0F, 1.0F);
            }

            return true;
         } else {
            return false;
         }
      }

      public CompoundTag write() {
         CompoundTag nbt = new CompoundTag();
         nbt.m_128405_("ticks", this.ticks);
         nbt.m_128365_("position", this.position.toNbt());
         return nbt;
      }
   }

   static class SummonPosition {
      public final BlockPos spawnPosition;
      public final BlockPos fire;
      public final Set<BlockPos> totems;

      public SummonPosition(CompoundTag tag) {
         if (!tag.m_128441_("fire") && !tag.m_128441_("totems") && !tag.m_128441_("spawnPosition")) {
            this.totems = new HashSet<>();
            this.spawnPosition = NbtUtils.m_129239_(tag);
            this.fire = this.spawnPosition.m_6625_(10);
         } else {
            this.fire = NbtUtils.m_129239_(tag.m_128469_("fire"));
            this.totems = new HashSet<>(NbtHelper.toList(tag.m_128469_("totems"), v -> NbtUtils.m_129239_((CompoundTag)v)));
            this.spawnPosition = NbtUtils.m_129239_(tag.m_128469_("spawnPosition"));
         }
      }

      public SummonPosition(BlockPos fire, Set<BlockPos> totems) {
         this.fire = fire;
         this.spawnPosition = fire.m_6630_(10);
         this.totems = totems;
      }

      public boolean isCancelled(Level world) {
         return !this.check(this.fire, world);
      }

      private boolean check(BlockPos pos, Level world) {
         return world.m_8055_(pos).m_60713_((Block)BlocksMCA.INFERNAL_FLAME.get());
      }

      public CompoundTag toNbt() {
         CompoundTag tag = new CompoundTag();
         tag.m_128365_("fire", NbtUtils.m_129224_(this.fire));
         tag.m_128365_("totems", NbtHelper.fromList(this.totems, NbtUtils::m_129224_));
         tag.m_128365_("spawnPosition", NbtUtils.m_129224_(this.spawnPosition));
         return tag;
      }
   }
}
